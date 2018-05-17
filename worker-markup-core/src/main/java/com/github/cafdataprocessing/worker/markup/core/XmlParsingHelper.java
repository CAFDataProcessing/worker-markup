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

import com.google.common.base.Strings;
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
        /* 
        * Check if current name is valid,
        * if valid the current name is used, without any further processing.
        * if the name is not valid it is then sanitized by removing an illegal characters
        * if the name cannot be santized it is then discarded and the default name is returned
        */
        if (null != Verifier.checkElementName(name)) {
            final String elementName = sanitizeElementName(name);
            return Strings.isNullOrEmpty(elementName) ? defaultElementName : elementName;
        }
        return name;
    }

    private static String sanitizeElementName(final String name)
    {
        LOG.debug("Removing invalid characters from Header Element Name: [" + name + "]");
        final char[] nameCharArray = name.toCharArray();
        final StringBuilder sb = new StringBuilder(name.length());

        for (final char c : nameCharArray) {
            if (Verifier.isXMLNameStartCharacter(c) && c != ':') {
                LOG.debug("Identified valid start character for element name: " + c);
                sb.append(c);
                break;
            }
        }
        // Check that an acceptable start character was found, if not return null
        if (sb.toString().isEmpty()) {
            return null;
        }
        boolean skippedStartChar = false;
        // Parse name for invalid character and remove invalid characters
        for (final char c : nameCharArray) {
            //Skip the first instance of the starting character of the element name as it has already been added
            if (String.valueOf(c).equals(sb.toString()) && !skippedStartChar) {
                skippedStartChar = true;
                continue;
            }
            // append each valid character to the element name
            if (Verifier.isXMLNameCharacter(c) && c != ':') {
                sb.append(c);
            }
        }
        return sb.toString();
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
