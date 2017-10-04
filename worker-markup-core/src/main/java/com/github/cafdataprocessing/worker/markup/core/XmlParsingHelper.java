/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.worker.markup.core;

import org.jdom2.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlParsingHelper
{
    private static final Logger LOG = LoggerFactory.getLogger(XmlParsingHelper.class);

    private XmlParsingHelper()
    {
        // Private Constructor
    }

    /**
     * Takes in a String and validates the name is XML Production [4] and XML Production [5] valid. Invalid characters are stripped out.
     * In addition colons are stripped out. If all characters are invalid the provided "defaultElementName" will be returned as the XML
     * Element name.
     *
     * @param name XML Element name to be checked invalid characters
     * @param defaultElementName XML Element name to be used should the name provided contain only invalid characters
     * @return String representation of a valid XML Element name
     */
    public static String removeInvalidXmlElementNameChars(String name, String defaultElementName)
    {
        String elementName;
        // Check if current name is valid,
        // if valid the current name is used, without any further processing.
        if (null != Verifier.checkElementName(name)) {
            LOG.debug("Removing invalid characters from Header Element Name: [" + name + "]");
            char[] nameCharArray = name.toCharArray();
            boolean startCharSet = false;
            StringBuilder sb = new StringBuilder(name.length());
            // Parse name for invalid character and remove invalid characters
            for (char c : nameCharArray) {
                if (!startCharSet && Verifier.isXMLNameStartCharacter(c)) {
                    startCharSet = true;
                    sb.append(c);
                } else if (Verifier.isXMLNameCharacter(c) && c != ':') {
                    sb.append(c);
                }
            }
            // Check if elementName is empty. If empty the parsed header contained only invalid
            // characters. In this case return the default header name, "UnreadableHeader".
            elementName = sb.toString();
            if (elementName.isEmpty()) {
                elementName = defaultElementName;
            }
        } else {
            elementName = name;
        }

        return elementName;
    }

    /**
     * Takes in a String and validates the text is XML Production [2] valid. Invalid characters are stripped out.
     *
     * @param text XML Element text to be checked for invalid characters
     * @return String representation of valid XML Element text
     */
    public static String removeInvalidXmlElementTextChars(String text)
    {
        String elementName;
        // Check if current text is valid,
        // if valid the current text is used, without any further processing.
        if (null != Verifier.checkCharacterData(text)) {

            LOG.debug("Removing invalid characters from Element Text: [" + text + "]");
            char[] nameCharArray = text.toCharArray();
            StringBuilder sb = new StringBuilder(text.length());
            // Parse text for invalid characters and remove invalid characters
            for (char c : nameCharArray) {
                if (Verifier.isXMLCharacter(c)) {
                    sb.append(c);
                }
            }
            elementName = sb.toString();
        } else {
            elementName = text;
        }
        return elementName;
    }
}
