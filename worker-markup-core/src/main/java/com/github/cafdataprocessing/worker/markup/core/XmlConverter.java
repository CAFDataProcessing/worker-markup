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

import com.github.cafdataprocessing.worker.markup.core.exceptions.AddHeadersException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.ReferencedData;
import org.apache.commons.codec.binary.Base64;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public final class XmlConverter
{
    private static final Map<String, String> HEADER_DISPLAY_NAMES_TO_SOURCES = ImmutableMap.<String, String> builder()
            .put("From", "from")
            .put("To", "to")
            .put("CC", "cc")
            .put("BCC", "bcc")
            .put("Date", "sent")
            .put("Subject", "subject")
            .put("Importance", "priority")
            .build();
    private static final Logger LOG = LoggerFactory.getLogger(XmlConverter.class);

    private XmlConverter()
    {
    }

    /**
     * Retrieves the field values, sanitises them for inclusion in an xml document, and then returns the sanitised entries.
     *
     * @param dataSource the remote data store which can be used to resolve the values of reference fields
     * @param sourceData the value of the field, or a reference that can be used to retrieve the value of the field
     * @param isEmail whether the entries are being retrieved for an email
     * @param shouldAddEmailHeaders whether email headers should be added to the generated xml field entry for CONTENT
     *
     * @return a list of name-value pairs which can be used to create xml elements without further sanitisation
     * @throws AddHeadersException if there is a failure adding email headers to field value
     */
    public static List<XmlFieldEntry> getXmlFieldEntries(final DataSource dataSource,
                                                         final Multimap<String, ReferencedData> sourceData,
                                                         final boolean isEmail,
                                                         final boolean shouldAddEmailHeaders)
            throws AddHeadersException
    {
        // Get the collection of fields
        final Collection<Map.Entry<String, ReferencedData>> fields = sourceData.entries();

        // Create an empty list which the xml field entries will be added to
        final List<XmlFieldEntry> xmlFieldEntries = new ArrayList<>(fields.size());

        // Cycle through the fields and add them to the list
        for (Map.Entry<String, ReferencedData> referencedDataEntry : fields) {
            // Get the field name and value
            final String fieldName = referencedDataEntry.getKey();
            final String fieldValue = ReferencedDataRetrieval.getContentAsStringEx(dataSource, referencedDataEntry.getValue());

            // Sanitise them for use in xml
            final String elementName = XmlParsingHelper.removeInvalidXmlElementNameChars(fieldName, "UnreadableField");
            final String elementText = XmlParsingHelper.removeInvalidXmlElementTextChars(fieldValue);

            // Create an entry and add it to the list
            xmlFieldEntries.add(new XmlFieldEntry(elementName, elementText));

            // Parse conversation index value for MSG and EML files if supplied
            if (fieldName.equals("CAF_MAIL_CONVERSATION_INDEX")) {
                LOG.debug("convertToXML: Mail message conversation index key detected ...");

                // Only parse if the value is Base64 encoded.
                if (Base64.isBase64(fieldValue)) {

                    try {
                        final String conversationIndexJson = ConversationIndexParser.parseConversationIndex(fieldValue);

                        xmlFieldEntries.add(new XmlFieldEntry("CAF_MAIL_CONVERSATION_INDEX_PARSED", conversationIndexJson));
                    } catch (ConversationIndexParserException cipe) {
                        // Markup worker exception thrown from conversation index parsing should not result in email markup worker failing
                        // Simply log the error and continue.
                        LOG.error("Exception thrown when attempting to parse the conversation index value: ", cipe.getMessage());
                    }

                } else {
                    // Ignore if the conversation index value is not Base64 encoded
                    LOG.warn("Unexpected value for conversation index as it has not been base64 encoded. Parsing will be ignored.");
                }
            }
        }

        if(isEmail && shouldAddEmailHeaders){
            addEmailHeadersToXmlFieldEntries(xmlFieldEntries);
        }

        //sort the xml field entries so they are not dependent on the order the sourceData fields are in
        xmlFieldEntries.sort(Comparator.comparing(fi -> fi.getName()));
        return xmlFieldEntries;
    }

    /**
     * Creates a simple xml document given a list of entries to be added to it.
     *
     * @param xmlFieldEntries the entries to be added to the xml document
     * @return the new xml document containing the specified entries
     */
    public static Document createXmlDocument(final List<XmlFieldEntry> xmlFieldEntries)
    {
        final Element rootElement = new Element("root");
        final Document doc = new Document(rootElement);

        for (final XmlFieldEntry xmlFieldEntry : xmlFieldEntries) {
            final String elementName = xmlFieldEntry.getName();
            final String elementText = xmlFieldEntry.getText();

            final Element element = new Element(elementName);
            element.setText(elementText);

            rootElement.addContent(element);
        }

        return doc;
    }

    private static void addEmailHeadersToXmlFieldEntries(List<XmlFieldEntry> sourceEntries)
            throws AddHeadersException
    {
        StringBuilder headersBuilder = new StringBuilder();
        // Build headers
        for(Map.Entry<String, String> displayNameToSource: HEADER_DISPLAY_NAMES_TO_SOURCES.entrySet()) {
            appendEmailHeaderToBuilder(headersBuilder, displayNameToSource.getKey(), displayNameToSource.getValue(),
                    sourceEntries);
        }

        // Add a new line indicating break in headers section.
        headersBuilder.append("\n");

        for(XmlFieldEntry entryToUpdate: sourceEntries.stream()
                .filter(ent -> ent.getName().equals("CONTENT"))
                .collect(Collectors.toList())){
            entryToUpdate.setText(headersBuilder.toString() + entryToUpdate.getText());
        }
    }

    private static void appendEmailHeaderToBuilder(StringBuilder headersBuilder, String headerStartText,
                                                   String headerSourceFieldName, List<XmlFieldEntry> sourceEntries)
        throws AddHeadersException
    {
        List<String> headerValues = sourceEntries.stream()
                .filter(ent -> ent.getName().toLowerCase(Locale.ENGLISH).equals(headerSourceFieldName))
                .map(ent -> ent.getText())
                .collect(Collectors.toList());

        if(headerValues.isEmpty()){
            LOG.debug("No '" + headerSourceFieldName + "' values to add in email headers during markup.");
        }
        else if (headerValues.size() > 1) {
            throw new AddHeadersException("Unable to add email headers. Multiple field entries found for header: "
                    +headerSourceFieldName);
        }
        else{
            headersBuilder.append(headerStartText+": ");
            String headerValue = headerSourceFieldName.equals("priority")
                    ? Normalizations.normalizePriority(headerValues.get(0))
                    : headerValues.get(0);
            headersBuilder.append(headerValue);
            headersBuilder.append("\n");
        }
    }


}
