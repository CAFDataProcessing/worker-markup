/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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

import java.util.ArrayList;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;

public final class XmlVerifier
{
    private XmlVerifier()
    {
    }

    /**
     * Checks that the fields and values in the specified xml document match the specified expected list.
     *
     * @param xmlDocument the xml document to be checked
     * @param expectedFieldEntries the list of entries which are expected to be in the xml document
     * @throws RuntimeException if the xml document does not contain the expected content
     */
    public static void verifyXmlDocument(final Document xmlDocument, final List<XmlFieldEntry> expectedFieldEntries)
    {
        // Check that the first field is not called 'hash'
        final int expectedNumberOfFields = expectedFieldEntries.size();
        if (expectedNumberOfFields > 0 && expectedFieldEntries.get(0).getName().equals("hash")) {
            // This is a pretty unusual circumstance but since the getFieldElements() method can't currently distinguish a field named
            // 'hash' from a hash that was added to the xml document, we're just not going to bother with the verification if the first
            // field expected is named 'hash'.
            return;
        }

        // Get the list of field elements
        final List<Element> fieldElements = getFieldElements(xmlDocument);
        final int actualNumberOfFields = fieldElements.size();

        // Check that the number of fields is as expected
        if (expectedNumberOfFields != actualNumberOfFields) {
            throw new RuntimeException("Xml verification failed.  The number of fields is not as expected!");
        }

        // Cycle through the fields and check that the names and values match what is expected
        for (int i = 0; i < actualNumberOfFields; i++) {
            final XmlFieldEntry expectedFieldEntry = expectedFieldEntries.get(i);
            final Element actualElement = fieldElements.get(i);

            if (!expectedFieldEntry.getName().equals(actualElement.getName())) {
                throw new RuntimeException("Xml verification failed.  The name of the field element has changed!");
            }

            final String expectedFieldValue = expectedFieldEntry.getText();
            final String actualFieldValue = actualElement.getValue();

            if (!expectedFieldValue.equals(actualFieldValue)) {
                throw new RuntimeException("Xml verification failed.  The value of the field element has changed!");
            }
        }
    }

    /**
     * In truth the field elements should have been kept separately in their own fields node at the top level, but at the minute they are
     * just directly under the root elements, after the hash elements.
     *
     * This function does not work correctly if the first field happens to also be called 'hash'.
     *
     * @param xmlDocument the xml document that contains the field elements
     * @return a list that contains the field elements
     */
    private static List<Element> getFieldElements(final Document xmlDocument)
    {
        // Get the children of the root node
        final Element rootElement = xmlDocument.getRootElement();
        final List<Element> rootElementChildren = rootElement.getChildren();

        final int numberOfRootElementChildren = rootElementChildren.size();

        // Count the number of elements at the start which have the name 'hash'
        int hashElementCount = 0;
        while (hashElementCount < numberOfRootElementChildren && rootElementChildren.get(hashElementCount).getName().equals("hash")) {
            hashElementCount++;
        }

        // Create a list to contain the field elements and put the rest of the elements into it
        final List<Element> fieldElements = new ArrayList<>(numberOfRootElementChildren - hashElementCount);

        for (int i = hashElementCount; i < numberOfRootElementChildren; i++) {
            fieldElements.add(rootElementChildren.get(i));
        }

        // Return the field elements list
        return fieldElements;
    }
}
