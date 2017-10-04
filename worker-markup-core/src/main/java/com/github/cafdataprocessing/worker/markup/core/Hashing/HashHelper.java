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
package com.github.cafdataprocessing.worker.markup.core.Hashing;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.hpe.caf.worker.markup.*;
import net.openhft.hashing.LongHashFunction;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Helper for hashing specified elements in a JDOM2 document.
 */
public class HashHelper
{
    private static final String ERR_MSG_NO_HASH_FUNCTION = "No hash functions have been specified in the hash configuration. Defaulting to use NONE.";
    private static final String ERR_MSG_NO_NORMALIZATION_TYPE_PREFIX = "Normalization type not specified for the field: ";
    private static final String ERR_MSG_NO_HASH_CONFIGURATION = "Hash configuration is null. You must create a hash configuration.";
    private static final String ERR_MSG_NO_FIELDS_SPECIFIED = "No fields have been specified in the hash configuration. You must specify fields to be included in the hash.";
    private static final String ERR_MSG_DOCUMENT_NULL = "The document is null.";
    private static final String ERR_MSG_NO_TYPE = "No type has been specified in the hash configuration. This has been defaulted to be EMAIL_SPECIFIC.";

    private static final Map<String, String> PRIORITY_NORMALIZATIONS = ImmutableMap.<String, String> builder()
            .put("0", "Low")
            .put("1", "Normal")
            .put("2", "High")
            .put("lowest", "Low")
            .put("low", "Low")
            .put("normal", "Normal")
            .put("high", "High")
            .put("highest", "High")
            .build();

    private HashHelper()
    {
    }

    /**
     * Logger for logging purposes.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HashHelper.class);

    /*
     * Location where to preform hash
     *
     * @param Document - the JDOM2 document we want to perform hash
     * @param List<HashConfiguration> - The list of hash configurations to be preformed
     */
    public static void generateHashes(Document document, List<HashConfiguration> hashConfiguration) throws JDOMException
    {
        LOG.info("Identifying where to preform hash.");
        if (hashConfiguration != null) {
            for (HashConfiguration config : hashConfiguration) {
                validateInputs(document, config);

                switch (config.scope) {
                    case EMAIL_SPECIFIC:
                        generateEmailSpecificHashes(document, config);
                        break;

                    case EMAIL_THREAD:
                        generateEmailThreadHashes(document, config);
                        break;

                    default:
                        generateEmailSpecificHashes(document, config);
                        break;
                }
            }
        }
    }

    /**
     * Generates xml sample of fields requested in the hashConfiguration, hashes this xml sample and puts back into the xml markup.
     *
     * @param document - the JDOM2 document we want to perform hash
     * @param hashConfiguration - The configuration for the hash from the task data.
     */
    private static void generateEmailSpecificHashes(final Document document, final HashConfiguration hashConfiguration)
    {
        LOG.info("Starting email specific hash generation based on hash configuration received from task (doc).");

        final Element rootElement = document.getRootElement();

        for (final Element contentElement : rootElement.getChildren("CONTENT")) {
            generateEmailSpecificHashes(contentElement, hashConfiguration);
        }
    }

    /**
     * Generates xml sample of fields requested in the hashConfiguration, hashes this xml sample and puts back into the xml markup.
     *
     * @param parentElement the element which contains the 'email' elements
     * @param hashConfiguration the configuration for the hash from the task data
     */
    private static void generateEmailSpecificHashes(final Element parentElement, final HashConfiguration hashConfiguration)
    {
        LOG.info("Starting email specific hash generation based on hash configuration received from task (parent).");

        // Iterate through the email tag elements.
        for (Element e : parentElement.getChildren("email")) {
            // Temporary document for storing the fields to be hashed. This is passed by reference to the recursive function
            final Element tempXMLElement = new Element("root");

            // Perform the recursive search through the email element.
            recursivelyGenerateEmailSpecificHashesAndAddToDocument(e, hashConfiguration, tempXMLElement);

            // hash the tempXMLElement and add it to the email element
            hashAndAddToDocument(e, hashConfiguration, tempXMLElement);

            LOG.info("recursivelyGenerateHashesAndAddToDocument: Info - '{}'", "Hashing complete for the current email.");
        }
    }

    /**
     * Recursively search through the child nodes for the fields specified in the configuration.
     *
     * Build a temporary xml document with the fields to hash.
     *
     * Put the hash value into the document including the configuration details.
     *
     * @param currentElement
     * @param hashConfiguration
     * @param tempXMLElement
     */
    private static void recursivelyGenerateEmailSpecificHashesAndAddToDocument(Element currentElement,
                                                                               HashConfiguration hashConfiguration,
                                                                               Element tempXMLElement)
    {
        // Iterate through the fields we need to search for defined in the configuration
        generateNormalizationType(currentElement, hashConfiguration, tempXMLElement);

        // If there are children of the current element, recursively call the search again
        final List<Element> childElements = currentElement.getChildren();

        if (childElements != null) {
            for (Element e : childElements) {
                recursivelyGenerateEmailSpecificHashesAndAddToDocument(e, hashConfiguration, tempXMLElement);
            }
        }
    }

    /*
     * Generates xml sample of fields requested in the hashConfiguration for email threads, hashes this xml sample and puts back into the xml markup.
     * @param Document
     * @param HashConfiguration
     */
    private static void generateEmailThreadHashes(Document document, HashConfiguration hashConfiguration)
    {
        LOG.info("Starting email thread hash generation based on hash configuration received from task.");

        // Temporary document for storing the fields to be hashed.
        final Element tempXMLElement = new Element("root");

        for (Element e : document.getRootElement().getChildren()) {
            generateNormalizationType(e, hashConfiguration, tempXMLElement);
        }

        // hash the tempXMLElement and add it to the root element
        hashAndAddToDocument(document.getRootElement(), hashConfiguration, tempXMLElement);
        LOG.info("generateEmailThreadHashes: Info - '{}'", "Hashing complete for the current email.");
    }

    /*
     *   Build a temporary xml document with the fields to hash. Also normalizing them if required.
     */
    private static void generateNormalizationType(Element currentElement, HashConfiguration hashConfiguration,
                                                  Element tempXMLElement)
    {
        // Iterate through the fields we need to search for defined in the configuration
        for (Field currentField : hashConfiguration.fields) {
            // Get the predicate for metadata key matching from the field name
            final Predicate<String> doesFieldSpecMatch = getKeyToFilterFieldNameMatchPredicate(currentField.name);
            // If the current field name matches with the field name normalize the value and add it to the xml element
            if (doesFieldSpecMatch.test(currentElement.getName())) {
                // We have a match, now normalize the field based on normalization type from configuration
                normalizeValueAndAddToXMLElement(currentElement, currentField, tempXMLElement);
            }
        }
    }

    private static Predicate<String> getKeyToFilterFieldNameMatchPredicate(String filterFieldName) {
        // If the filter field name contains an asterisk for wildcard matching transform it for regex matching and
        // return the predicate for matching a key against it.
        // Else return the predicate for matching a key against the filter field name.
        if (filterFieldName.contains("*")) {
            String[] splitFieldFieldName = filterFieldName.split("\\*", -1);
            for (int i = 0; i < splitFieldFieldName.length; i++) {
                splitFieldFieldName[i] = Pattern.quote(splitFieldFieldName[i].toUpperCase());
            }
            final String finalRegEx = String.join(".*", splitFieldFieldName);
            return key -> key.toUpperCase().matches(finalRegEx);
        } else {
            return key -> key.equalsIgnoreCase(filterFieldName);
        }
    }

    /**
     * Private method to validate the inputs and throw exceptions if needed.
     *
     * @param document
     * @param hashConfiguration
     * @throws JDOMException
     */
    private static void validateInputs(Document document, HashConfiguration hashConfiguration) throws JDOMException
    {
        // Validation of hash configuration
        if (document == null) {
            LOG.error("generateHashes: Error - '{}'", ERR_MSG_DOCUMENT_NULL);
            throw new NullPointerException(ERR_MSG_DOCUMENT_NULL);
        }

        if (hashConfiguration == null) {
            LOG.error("generateHashes: Error - '{}'", ERR_MSG_NO_HASH_CONFIGURATION);
            throw new IllegalArgumentException(ERR_MSG_NO_HASH_CONFIGURATION);
        }

        if (hashConfiguration.fields == null || hashConfiguration.fields.size() < 1) {
            LOG.error("generateHashes: Error - '{}'", ERR_MSG_NO_FIELDS_SPECIFIED);
            throw new IllegalArgumentException(ERR_MSG_NO_FIELDS_SPECIFIED);
        }

        for (Field f : hashConfiguration.fields) {
            if (f.normalizationType == null) {
                LOG.trace("generateHashes: Trace - '{}'", ERR_MSG_NO_NORMALIZATION_TYPE_PREFIX + f.name);
                f.normalizationType = NormalizationType.NONE;
            }
        }

        if (hashConfiguration.hashFunctions == null || hashConfiguration.hashFunctions.size() < 1) {
            LOG.warn("generateHashes: Warning - '{}'", ERR_MSG_NO_HASH_FUNCTION);
            hashConfiguration.hashFunctions.add(HashFunction.NONE);
        }

        if (hashConfiguration.scope == null) {
            LOG.warn("generateHashes: Warning - '{}'", ERR_MSG_NO_TYPE);
            hashConfiguration.scope = Scope.EMAIL_SPECIFIC;
        }
    }

    /**
     * Private method containing switch statement which chooses which normalization method to perform based on the hash configuration for
     * the current field. Normalizes and adds the normalized text to the temporary XML element.
     *
     * @param currentElement
     * @param currentField
     * @param tempXMLElement
     */
    private static void normalizeValueAndAddToXMLElement(Element currentElement, Field currentField,
                                                         Element tempXMLElement)
    {
        final String currentElementValue = currentElement.getValue();

        String normalizedStr;
        switch (currentField.normalizationType) {
            case NONE:
                normalizedStr = currentElementValue;
                break;
            case REMOVE_WHITESPACE:
                normalizedStr = removeAllWhitespace(currentElementValue);
                break;
            case REMOVE_WHITESPACE_AND_LINKS:
                normalizedStr = removeAllWhitespaceAndLinks(currentElementValue);
                break;
            case NAME_ONLY:
                normalizedStr = nameOnly(currentElementValue);
                break;
            case NORMALIZE_PRIORITY:
                normalizedStr = normalizePriority(currentElementValue);
                break;
            case NORMALIZE_CASE:
                normalizedStr = normalizeCase(currentElementValue);
                break;
            default:
                LOG.error("normalizeValueAndAddToXMLElement: Error - '{}'",
                          "Normalization type must be specified for the field " + currentElement.getName());
                throw new IllegalArgumentException(
                    "Normalization type must be specified for the field " + currentElement.getName());
        }
        // Add current element to the temporary xml root element
        if (currentField.normalizationType == NormalizationType.NORMALIZE_PRIORITY && normalizedStr.equals("Normal")) {
            return;
        }
        tempXMLElement.addContent(new Element(currentElement.getName()).setText(normalizedStr));
    }

    /**
     * Private method to add the new hash element back into the document.
     *
     * @param currentElement
     * @param hashConfiguration
     */
    private static void hashAndAddToDocument(Element currentElement, HashConfiguration hashConfiguration,
                                             Element tempXMLElement)
    {
        // build a string from the entire temporary xml element at this point containing all fields required in configuration
        XMLOutputter xmlOutputter = new XMLOutputter();
        String stringToHash = xmlOutputter.outputString(tempXMLElement);

        // Set up the structure of the <hash> element and its children.
        Element hashElement = new Element("hash");
        Element configElement = new Element("config");
        Element fieldsElement = new Element("fields");
        configElement.addContent(fieldsElement);
        hashElement.addContent(configElement);
        currentElement.addContent(0, hashElement); // add hash to element 0
        final String hashConfigurationName = hashConfiguration.name;
        if (hashConfigurationName != null) {
            hashElement.setAttribute("name", hashConfigurationName);
        }

        // For each of the fields in the hash configuration, add the name and normalization type to a new field element
        // and add each <field> as a child to <fields>
        for (Field f : hashConfiguration.fields) {
            Element field = new Element("field");
            field.setAttribute("name", f.name);
            field.setAttribute("normalizationType", f.normalizationType.toString());

            fieldsElement.addContent(field);
        }

        makeHashDigest(hashElement, hashConfiguration, stringToHash);
    }

    /**
     * Private method to generate the hash digest
     */
    private static void makeHashDigest(final Element hashElement, final HashConfiguration hashConfiguration, final String stringToHash)
    {
        // For each hash function defined in the hash configuration, create digest tag element with the function used and
        // hash value as attributes, and add this element as a child of the hash element.
        for (int i = 0; i < hashConfiguration.hashFunctions.size(); i++) {
            Element digestElement = new Element("digest");
            digestElement.setAttribute(new Attribute("function", hashConfiguration.hashFunctions.get(i).toString()));

            // Based on the hashConfiguration for the field, perform the desired hash function.
            final HashFunction hashFunction = hashConfiguration.hashFunctions.get(i);

            switch (hashFunction) {
                case NONE:
                    digestElement.setAttribute(new Attribute("value", stringToHash));
                    break;
                case XXHASH64:
                    digestElement.setAttribute(new Attribute("value", performXxHash64(stringToHash)));
                    break;
                default:
                    LOG.error("hashAndAddToDocument: Error - Unrecognised hash function: '{}'", hashFunction);
                    throw new IllegalArgumentException("Unrecognised hash function: " + hashFunction);
            }

            hashElement.addContent(digestElement);
        }
    }

    /**
     * Remove all whitespace and newlines from a String.
     *
     * @param str
     * @return String
     */
    private static String removeAllWhitespace(String str)
    {
        String normalizedString = str.replaceAll("\\s+", "");
        return normalizedString;
    }

    /**
     * Removes whitespace and then removes &lt; &gt; tags and content (links)
     */
    private static String removeAllWhitespaceAndLinks(String str)
    {
        // Remove whitespace characters.
        str = str.replaceAll("\\s+", "");
        // Search for a <, remove non whitespace characters until the next > character (including the < and >).
        str = str.replaceAll("<\\S+?>", "");
        // Remove reply email quotation marks (> characters)
        str = str.replaceAll(">", "");
        str = str.replaceAll("‘", "'");
        str = str.replaceAll("’", "'");
        str = str.replaceAll("“", "\"");
        str = str.replaceAll("”", "\"");
        str = str.replaceAll("–", "-");
        return str;
    }

    /**
     * If the string begins with ' it assumes it is of the type '&lt;user@email.xxx&gt;' and so removes the '&lt;' and '&gt;'.
     *
     * Otherwise it removes these tags and their content and strips out ' and " punctuation.
     */
    private static String nameOnly(String str)
    {
        // Remove whitespace.
        str = str.replaceAll("\\s+", "");
        // Search for a < character, remove all non-whitespace and whitespace characters until the next >, including the < and >.
        str = str.replaceAll("<[^,;]+>", "");
        // Remove double quotation marks.
        str = str.replaceAll("\"", "");
        // Remove single quotation marks.
        str = str.replaceAll("\'", "");
        // Remove reply email quotation marks (> characters)
        str = str.replaceAll(">", "");
        // Replace semicolons to comas
        str = str.replaceAll(";", ",");
        return str;
    }

    /**
     * Returns a normalized case version of the provided string.
     * @param str Value to normalize case for.
     * @return Normalized case version of provided string value.
     */
    private static String normalizeCase(final String str){
        return str.toLowerCase(Locale.ENGLISH);
    }

    /**
     * If the supplied string is a recognized email priority value then this is converted to a standard form.
     *
     * Otherwise the supplied string is returned.
     */
    private static String normalizePriority(final String str)
    {
        final String normalizedPriority = PRIORITY_NORMALIZATIONS.get(str.trim().toLowerCase());
        return Strings.isNullOrEmpty(normalizedPriority) ? str : normalizedPriority;
    }

    /**
     * Perform xxHash64 hashing on a string.
     *
     * @param str - The string to be hashed.
     * @return String - The hex representation of the hash value.
     */
    private static String performXxHash64(String str)
    {
        return Long.toHexString(LongHashFunction.xx_r39().hashChars(str));
    }
}
