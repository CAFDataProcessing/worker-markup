/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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

import com.hpe.caf.worker.markup.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Unit test for hashing functionality of the markup worker.
 */
public class HashingTest
{
    /**
     * Test the hash functionality. A test configuration will be created with various fields to be included in the hash.
     *
     * Asserts that the hash tag elements have been successfully added into the xml markup.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testHashingOneEmail() throws JDOMException, IOException
    {
        List<HashConfiguration> hashConfiguration = setupHashConfiguration();
        Document jdomDoc = createDummyDocumentOneEmail();

        HashHelper.generateHashes(jdomDoc, hashConfiguration);
        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(jdomDoc, hashConfiguration));
    }

    /**
     * Test the hash functionality. A test configuration will be created with various fields to be included in the hash.
     *
     * Asserts that the hash tag elements have been successfully added into the xml markup.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testHashingTwoEmails() throws JDOMException, IOException
    {
        List<HashConfiguration> hashConfiguration = setupHashConfiguration();
        Document jdomDoc = createDummyDocumentTwoEmails();

        HashHelper.generateHashes(jdomDoc, hashConfiguration);

        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(jdomDoc, hashConfiguration));
    }

    /**
     * Test the hash functionality. A test configuration will be created with various fields to be included in the hash.
     *
     * Asserts that the hash tag elements have been successfully added into the xml markup.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testHashingThreeEmails() throws JDOMException, IOException
    {
        List<HashConfiguration> hashConfiguration = setupHashConfiguration();
        Document jdomDoc = createDummyDocumentThreeEmails();

        HashHelper.generateHashes(jdomDoc, hashConfiguration);

        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(jdomDoc, hashConfiguration));
    }

    /**
     * Test the hash functionality. A test configuration will be created with various fields to be included in the hash.
     *
     * Asserts that the hash tag elements have been successfully added into the xml markup.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testHashingMultipleHashFunctions() throws JDOMException, IOException
    {
        List<HashConfiguration> hashConfiguration = setupHashConfiguration();
        hashConfiguration.get(0).hashFunctions.add(HashFunction.XXHASH64);
        Document jdomDoc = createDummyDocumentThreeEmails();

        HashHelper.generateHashes(jdomDoc, hashConfiguration);

        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(jdomDoc, hashConfiguration));
    }

    /**
     * Test the hash functionality. A test configuration will be created with various fields to be included in the hash.
     *
     * Asserts that the hash tag elements have been successfully added into the xml markup.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testHashingTwoIdenticalEmailsGenerateSameHash() throws JDOMException, IOException
    {
        Field f = new Field();
        f.name = "to";
        List<Field> fields = new ArrayList<>();
        fields.add(f);
        List<HashFunction> hashFunctions = new ArrayList<>();
        hashFunctions.add(HashFunction.XXHASH64);
        HashConfiguration hashConfiguration = new HashConfiguration();
        hashConfiguration.fields = fields;
        hashConfiguration.hashFunctions = hashFunctions;

        List<HashConfiguration> config = new ArrayList<>();
        config.add(hashConfiguration);

        Document jdomDoc = createDummyDocumentTwoIdenticalEmails();
        HashHelper.generateHashes(jdomDoc, config);
        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(jdomDoc, config));

        Iterator<Element> iterator = jdomDoc.getRootElement().getDescendants(new ElementFilter("digest"));
        Assert.assertEquals(iterator.next().getAttributeValue("value"), iterator.next().getAttributeValue("value"));
    }

    /**
     * Test the hash functionality. A test configuration will be created with fields including priority to be included in the hash.
     * The priority values are from different priority formats but are equivalent so they should match after priority normalization.
     *
     * Asserts that the hash tag elements have been successfully added into the xml markup.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testHashingTwoIdenticalEmailsWithPriorityGenerateSameHash() throws JDOMException, IOException
    {
        List<Field> fields = new ArrayList<>();

        Field toField = new Field();
        toField.name = "to";
        fields.add(toField);

        Field priorityField = new Field();
        priorityField.name = "priority";
        priorityField.normalizationType = NormalizationType.NORMALIZE_PRIORITY;
        fields.add(priorityField);

        List<HashFunction> hashFunctions = new ArrayList<>();
        hashFunctions.add(HashFunction.XXHASH64);
        HashConfiguration hashConfiguration = new HashConfiguration();
        hashConfiguration.fields = fields;
        hashConfiguration.hashFunctions = hashFunctions;

        List<HashConfiguration> config = new ArrayList<>();
        config.add(hashConfiguration);

        Document jdomDoc = createDummyDocumentTwoIdenticalEmailsWithEquivalentPriority();
        HashHelper.generateHashes(jdomDoc, config);
        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(jdomDoc, config));

        Iterator<Element> iterator = jdomDoc.getRootElement().getDescendants(new ElementFilter("digest"));
        Assert.assertEquals(iterator.next().getAttributeValue("value"), iterator.next().getAttributeValue("value"));
    }

    /**
     * Test the hash functionality with case normalization.
     *
     * Asserts that the hash tag elements have been successfully added into the xml markup and they are consistent across
     * separate runs where fields used in hash differ only by case when normalization case is used.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testNormalizeCaseHashGeneration() throws JDOMException, IOException {
        List<HashConfiguration> config;
        String fieldName = "test";
        {
            List<Field> fields = new ArrayList<>();

            Field testField = new Field();
            testField.name = fieldName;
            testField.normalizationType = NormalizationType.NORMALIZE_CASE;
            fields.add(testField);

            List<HashFunction> hashFunctions = new ArrayList<>();
            hashFunctions.add(HashFunction.XXHASH64);
            HashConfiguration hashConfiguration = new HashConfiguration();
            hashConfiguration.fields = fields;
            hashConfiguration.hashFunctions = hashFunctions;
            hashConfiguration.scope = Scope.EMAIL_THREAD;
            config = Arrays.asList(hashConfiguration);
        }

        String originalValue = "ABCDEFHIJKLMNOPQRSTUVWXYZ1234567890zzegehjykyabcdebfbnttabh";
        Document originalCaseDoc = buildNormalizeCaseTestDocument(fieldName, originalValue);

        HashHelper.generateHashes(originalCaseDoc, config);
        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(originalCaseDoc, config));

        Iterator<Element> iterator = originalCaseDoc.getRootElement().getDescendants(new ElementFilter("digest"));
        String originalCaseHashValue = iterator.next().getAttributeValue("value");
        Assert.assertNotNull("Hash value returned for original case document should not be null.", originalCaseHashValue);

        String upperCaseValue = originalValue.toUpperCase(Locale.ENGLISH);
        Document upperCaseDoc = buildNormalizeCaseTestDocument(fieldName, upperCaseValue);
        HashHelper.generateHashes(upperCaseDoc, config);
        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(upperCaseDoc, config));

        iterator = upperCaseDoc.getRootElement().getDescendants(new ElementFilter("digest"));
        String upperCaseHashValue = iterator.next().getAttributeValue("value");
        Assert.assertNotNull("Hash value returned for upper case document should not be null.", upperCaseHashValue);

        String lowerCaseValue = originalValue.toLowerCase(Locale.ENGLISH);
        Document lowerCaseDoc = buildNormalizeCaseTestDocument(fieldName, lowerCaseValue);
        HashHelper.generateHashes(lowerCaseDoc, config);
        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(lowerCaseDoc, config));

        iterator = lowerCaseDoc.getRootElement().getDescendants(new ElementFilter("digest"));
        String lowerCaseHashValue = iterator.next().getAttributeValue("value");
        Assert.assertNotNull("Hash value returned for lower case document should not be null.", lowerCaseHashValue);

        Assert.assertEquals("Generated hash value should be the same for original value and upper case value.",
                originalCaseHashValue, upperCaseHashValue);
        Assert.assertEquals("Generated hash value should be the same for original value and lower case value.",
                originalCaseHashValue, lowerCaseHashValue);

        //generate another hash value that doesn't normalize case and verify it is different
        {
            List<Field> fields = new ArrayList<>();

            Field testField = new Field();
            testField.name = fieldName;
            fields.add(testField);

            List<HashFunction> hashFunctions = new ArrayList<>();
            hashFunctions.add(HashFunction.XXHASH64);
            HashConfiguration hashConfiguration = new HashConfiguration();
            hashConfiguration.fields = fields;
            hashConfiguration.hashFunctions = hashFunctions;
            hashConfiguration.scope = Scope.EMAIL_THREAD;
            config = Arrays.asList(hashConfiguration);
        }

        Document noNormalizeCaseDoc = buildNormalizeCaseTestDocument(fieldName, originalValue);

        HashHelper.generateHashes(noNormalizeCaseDoc, config);
        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(noNormalizeCaseDoc, config));

        iterator = noNormalizeCaseDoc.getRootElement().getDescendants(new ElementFilter("digest"));
        String noNormalizeCaseHashValue = iterator.next().getAttributeValue("value");
        Assert.assertNotNull("Hash value returned for no normalizaion on case document should not be null.",
                noNormalizeCaseHashValue);

        Assert.assertNotEquals("Generated hash value should be different for original value between normalize " +
                        "and no normalization runs.",
                noNormalizeCaseHashValue, originalCaseHashValue);
        Assert.assertNotEquals("Generated hash value should be different for upper case normalized value and no " +
                        "normalization value.",
                noNormalizeCaseHashValue, upperCaseHashValue);
        Assert.assertNotEquals("Generated hash value should be different for lower case normalized value and no " +
                        "normalization value.",
                noNormalizeCaseHashValue, lowerCaseHashValue);
    }

    private Document buildNormalizeCaseTestDocument(String fieldName, String fieldValue)
            throws JDOMException, IOException {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<root>"
                + "<"+fieldName+">"
                + fieldValue
                + "</"+fieldName+">"
                + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
    }

    /**
     * Private method for asserting the elements were correctly added to the hash xml tag elements in the correct order and contain the
     * correct values.
     *
     * @param doc
     * @param hashConfigurations
     * @return
     */
    private boolean assertHashingFieldsWereCorrectlyAdded(Document doc, List<HashConfiguration> hashConfigurations)
    {
        // Assert that the element values are as expected.
        for (Element hash : doc.getRootElement().getDescendants(new ElementFilter("hash"))) {
            Optional<HashConfiguration> filteredHashConfig = null;
            for (final Element fieldElement : hash.getDescendants(new ElementFilter("field"))) {
                String elementUnderNameText = fieldElement.getAttributeValue("name");
                // If a hash config has not been found find one that contains the element under name, else try to match
                // the element under name with it
                if (filteredHashConfig == null) {
                    // Find a Hash Configuration that contains the the element under name
                    filteredHashConfig = hashConfigurations.stream().filter(hashConfiguration ->
                            hashConfiguration.fields.stream().filter(field ->
                                    field.name.equals(elementUnderNameText)).findAny().isPresent()
                    ).findAny();
                    Assert.assertTrue("Assert that a hash configuration exists that contains the field name " + elementUnderNameText,
                                      filteredHashConfig.isPresent());
                } else {
                    Assert.assertTrue("Assert that the hash configuration contains field name " + elementUnderNameText,
                            filteredHashConfig.get().fields.stream().filter(field ->
                                    field.name.equals(elementUnderNameText)).findFirst().isPresent());
                }
            }
            for (final Element fieldElement : hash.getDescendants(new ElementFilter("field"))) {
                if (filteredHashConfig.isPresent()) {
                    String elementUnderNormTypeText = fieldElement.getAttributeValue("normalizationType");
                    // Find the element under normalization type within the filtered hash config for testing
                    Optional<Field> filteredHashConfigFieldNormalizationType = filteredHashConfig.get().fields.stream().filter(field ->
                            field.normalizationType.toString().equals(elementUnderNormTypeText)).findFirst();
                    System.out.println();
                    Assert.assertTrue("Assert that the hash configuration contains the field normalisation type "
                        + elementUnderNormTypeText, filteredHashConfigFieldNormalizationType.isPresent());
                } else {
                    Assert.fail("A Hash Configuration has not been filtered for testing");
                }
            }
            for (Element digest : hash.getChildren("digest")) {
                if (filteredHashConfig.isPresent()) {
                    String digestAttributeValue = digest.getAttribute("function").getValue();
                    // Find the element under digest within the filtered hash config for testing
                    Optional<HashFunction> filteredHashConfigHashFunction = filteredHashConfig.get().hashFunctions.stream().filter(hashFunction ->
                            hashFunction.toString().equals(digestAttributeValue)).findFirst();
                    Assert.assertTrue("Assert that the hash configuration contains the element "
                            + digestAttributeValue + " under parent 'digest'", filteredHashConfigHashFunction.isPresent());
                } else {
                    Assert.fail("A Hash Configuration has not been filtered for testing");
                }
            }
        }

        return true;
    }

    /**
     * Fail test for null documents, which may have had incorrect xml parsing resulting in a null document being created.
     *
     * @throws org.jdom2.JDOMException
     */
    @Test(expected = NullPointerException.class)
    public void testFailureNullDocument() throws JDOMException
    {
        List<HashConfiguration> hashConfiguration = setupHashConfiguration();
        Document doc = null;
        HashHelper.generateHashes(doc, hashConfiguration);
    }

    /**
     * Test for if no normalization type has been specified it defaults to the correct default (NONE).
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testNoNormalizationTypeSpecified() throws JDOMException, IOException
    {
        Field f = new Field();
        f.name = "to";
        List<Field> fields = new ArrayList<>();
        fields.add(f);
        List<HashFunction> hashFunctions = new ArrayList<>();
        hashFunctions.add(HashFunction.XXHASH64);
        HashConfiguration hashConfiguration = new HashConfiguration();
        hashConfiguration.fields = fields;
        hashConfiguration.hashFunctions = hashFunctions;

        List<HashConfiguration> config = new ArrayList<>();
        config.add(hashConfiguration);

        Document doc = createDummyDocumentOneEmail();
        HashHelper.generateHashes(doc, config);

        Assert.assertEquals(config.get(0).fields.get(0).normalizationType.toString(), "NONE");

        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(doc, config));
    }

    /**
     * Test for if no hash function has been specified it defaults to the correct default (NONE).
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testNoHashFunctionSpecified() throws JDOMException, IOException
    {
        Field f = new Field();
        f.name = "to";
        f.normalizationType = NormalizationType.NONE;
        List<Field> fields = new ArrayList<>();
        fields.add(f);
        List<HashFunction> hashFunctions = new ArrayList<>();
        HashConfiguration hashConfiguration = new HashConfiguration();
        hashConfiguration.fields = fields;
        hashConfiguration.hashFunctions = hashFunctions;

        List<HashConfiguration> config = new ArrayList<>();
        config.add(hashConfiguration);

        Document doc = createDummyDocumentOneEmail();
        HashHelper.generateHashes(doc, config);

        Assert.assertEquals(config.get(0).hashFunctions.get(0).toString(), "NONE");

        Assert.assertTrue(assertHashingFieldsWereCorrectlyAdded(doc, config));
    }

    /**
     * Set up a mock hash configuration with some fields to search for and normalization types
     *
     * @return
     */
    private List<HashConfiguration> setupHashConfiguration()
    {
        // Configuration example one
        Field f = new Field();
        f.name = "to";
        f.normalizationType = NormalizationType.NONE;
        Field f2 = new Field();
        f2.name = "from";
        f2.normalizationType = NormalizationType.NONE;
        Field f3 = new Field();
        f3.name = "body";
        f3.normalizationType = NormalizationType.REMOVE_WHITESPACE;
        Field f4 = new Field();
        f4.name = "CHILD_INFO_*_HASH";
        f4.normalizationType = NormalizationType.NONE;

        List<Field> fields = new ArrayList<>();
        fields.add(f);
        fields.add(f2);
        fields.add(f3);
        fields.add(f4);

        List<HashFunction> hashFunctions = new ArrayList<>();
        hashFunctions.add(HashFunction.XXHASH64);

        HashConfiguration hashConfiguration = new HashConfiguration();
        hashConfiguration.name = "Normalized";
        hashConfiguration.scope = Scope.EMAIL_SPECIFIC;
        hashConfiguration.fields = fields;
        hashConfiguration.hashFunctions = hashFunctions;

        // Configuration example two
        Field f5 = new Field();
        f5.name = "cc";
        f5.normalizationType = NormalizationType.NONE;
        Field f6 = new Field();
        f6.name = "subject";
        f6.normalizationType = NormalizationType.NONE;
        Field f7 = new Field();
        f7.name = "sent";
        f7.normalizationType = NormalizationType.REMOVE_WHITESPACE;
        Field f8 = new Field();
        f8.name = "CHILD_INFO_*_HASH";
        f8.normalizationType = NormalizationType.NONE;

        List<Field> fieldsTwo = new ArrayList<>();
        fieldsTwo.add(f5);
        fieldsTwo.add(f6);
        fieldsTwo.add(f7);
        fieldsTwo.add(f8);

        HashConfiguration hashConfigurationTwo = new HashConfiguration();
        hashConfigurationTwo.name = "Varient";
        hashConfigurationTwo.scope = Scope.EMAIL_THREAD;
        hashConfigurationTwo.fields = fieldsTwo;
        hashConfigurationTwo.hashFunctions = hashFunctions;

        List<HashConfiguration> config = new ArrayList<>();
        config.add(hashConfiguration);
        config.add(hashConfigurationTwo);

        return config;
    }

    /**
     * Create dummy document from a string containing one email.
     *
     * @return
     */
    private Document createDummyDocumentOneEmail() throws JDOMException, IOException
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<root>  "
            + "<CHILD_INFO_1_HASH>CHILDINFOHASH1</CHILD_INFO_1_HASH>    "
            + "<CHILD_INFO_2_HASH>CHILDINFOHASH2</CHILD_INFO_2_HASH>    "
            + "<ChIlD_InFo_3_HaSh>CHILDINFOHASH3</ChIlD_InFo_3_HaSh>    "
            + "<sentTime time=\"2016-07-22T10:21:00Z\" />  "
            + "<cc>Cc: Reid, Andy &lt;andrew.reid@hpe.com&gt;</cc>      "
            + "<subject>Subject: RE: iSTF - CAF Integration</subject>"
            + "<sent date=\"2016-07-22 10:21\">Sent: 22 July 2016 10:21 AM</sent>      "
            + "<CONTENT>"
            + "<email messageId=\"950124.162336@example.com\" exchangeConversionId=\"sdsd\">    "
            + "<headers>      "
            + "<from user=\"andrew.reid@hpe.com\">From: Reid, Andy</from>      "
            + "<sent date=\"2016-07-22 10:21\">Sent: 22 July 2016 10:21 AM</sent>      "
            + "<to>To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;</to>      "
            + "<cc>Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;</cc>      "
            + "<subject normalised=\"iSTF - CAF Integration\">Subject: RE: iSTF - CAF Integration</subject>    "
            + "</headers>    "
            + "<body>      "
            + "<opening>     Hi Navamoni,      </opening>      "
            + "<p>Is this ready yet?</p>      "
            + "<signature>      Thanks      Andy      </signature>    "
            + "</body>  "
            + "<CHILD_INFO_1_HASH>CHILDINFOHASH1</CHILD_INFO_1_HASH>    "
            + "<CHILD_INFO_2_HASH>CHILDINFOHASH2</CHILD_INFO_2_HASH>    "
            + "<ChIlD_InFo_3_HaSh>CHILDINFOHASH3</ChIlD_InFo_3_HaSh>    "
            + "</email>"
            + "</CONTENT>"
            + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }

    /**
     * Create dummy document from a string containing one email.
     *
     * @return
     */
    private Document createDummyDocumentTwoIdenticalEmails() throws JDOMException, IOException
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<root>  "
            + "<sentTime time=\"2016-07-22T10:21:00Z\" />  "
            + "<CONTENT>"
            + "<email messageId=\"950124.162336@example.com\" exchangeConversionId=\"sdsd\">    "
            + "<headers>      "
            + "<from user=\"andrew.reid@hpe.com\">From: Reid, Andy</from>      "
            + "<sent date=\"2016-07-22 10:21\">Sent: 22 July 2016 10:21 AM</sent>      "
            + "<to>To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;</to>      "
            + "<cc>Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;</cc>      "
            + "<subject normalised=\"iSTF - CAF Integration\">Subject: RE: iSTF - CAF Integration</subject>    "
            + "</headers>    "
            + "<body>      "
            + "<opening>     Hi Navamoni,      </opening>      "
            + "<p>Is this ready yet?</p>      "
            + "<signature>      Thanks      Andy      </signature>    "
            + "</body>  "
            + "</email>"
            + "<email messageId=\"950124.162336@example.com\" exchangeConversionId=\"sdsd\">    "
            + "<headers>      "
            + "<from user=\"andrew.reid@hpe.com\">From: Reid, Andy</from>      "
            + "<sent date=\"2016-07-22 10:21\">Sent: 22 July 2016 10:21 AM</sent>      "
            + "<to>To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;</to>      "
            + "<cc>Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;</cc>      "
            + "<subject normalised=\"iSTF - CAF Integration\">Subject: RE: iSTF - CAF Integration</subject>    "
            + "</headers>    "
            + "<body>      "
            + "<opening>     Hi Navamoni,      </opening>      "
            + "<p>Is this ready yet?</p>      "
            + "<signature>      Thanks      Andy      </signature>    "
            + "</body>  "
            + "</email>"
            + "</CONTENT>"
            + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }

    /**
     * Create dummy document from a string containing one email.
     *
     * @return
     */
    private Document createDummyDocumentTwoIdenticalEmailsWithEquivalentPriority() throws JDOMException, IOException
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<root>  "
                + "<sentTime time=\"2016-07-22T10:21:00Z\" />  "
                + "<CONTENT>"
                + "<email messageId=\"950124.162336@example.com\" exchangeConversionId=\"sdsd\">    "
                + "<headers>      "
                + "<from user=\"andrew.reid@hpe.com\">From: Reid, Andy</from>      "
                + "<sent date=\"2016-07-22 10:21\">Sent: 22 July 2016 10:21 AM</sent>      "
                + "<to>To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;</to>      "
                + "<cc>Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;</cc>      "
                + "<subject normalised=\"iSTF - CAF Integration\">Subject: RE: iSTF - CAF Integration</subject>    "
                + "<priority>2</priority>    "
                + "</headers>    "
                + "<body>      "
                + "<opening>     Hi Navamoni,      </opening>      "
                + "<p>Is this ready yet?</p>      "
                + "<signature>      Thanks      Andy      </signature>    "
                + "</body>  "
                + "</email>"
                + "<email messageId=\"950124.162336@example.com\" exchangeConversionId=\"sdsd\">    "
                + "<headers>      "
                + "<from user=\"andrew.reid@hpe.com\">From: Reid, Andy</from>      "
                + "<sent date=\"2016-07-22 10:21\">Sent: 22 July 2016 10:21 AM</sent>      "
                + "<to>To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;</to>      "
                + "<cc>Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;</cc>      "
                + "<subject normalised=\"iSTF - CAF Integration\">Subject: RE: iSTF - CAF Integration</subject>    "
                + "<priority>Highest</priority>    "
                + "</headers>    "
                + "<body>      "
                + "<opening>     Hi Navamoni,      </opening>      "
                + "<p>Is this ready yet?</p>      "
                + "<signature>      Thanks      Andy      </signature>    "
                + "</body>  "
                + "</email>"
                + "</CONTENT>"
                + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }

    /**
     * Create dummy document from a string containing one email.
     *
     * @return
     */
    private Document createDummyDocumentTwoEmails() throws JDOMException, IOException
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<root>  "
            + "<sentTime time=\"2016-07-22T10:21:00Z\" />  "
            + "<cc>Cc: Reid, Andy &lt;andrew.reid@hpe.com&gt;</cc>      "
            + "<subject>Subject: RE: iSTF - CAF Integration</subject>"
            + "<sent date=\"2016-07-22 10:21\">Sent: 22 July 2016 10:21 AM</sent>      "
            + "<CONTENT>"
            + "<email messageId=\"950124.162336@example.com\" exchangeConversionId=\"sdsd\">    "
            + "<headers>      "
            + "<from user=\"andrew.reid@hpe.com\">From: Reid, Andy</from>      "
            + "<sent date=\"2016-07-22 10:21\">Sent: 22 July 2016 10:21 AM</sent>      "
            + "<to>To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;</to>      "
            + "<cc>Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;</cc>      "
            + "<subject normalised=\"iSTF - CAF Integration\">Subject: RE: iSTF - CAF Integration</subject>    "
            + "</headers>    "
            + "<body>      "
            + "<opening>     Hi Navamoni,      </opening>      "
            + "<p>Is this ready yet?</p>      "
            + "<signature>      Thanks      Andy      </signature>    "
            + "</body>  "
            + "</email>"
            + "<email messageId=\"914214.124136@example.com\" exchangeConversionId=\"sdsd\">    "
            + "<headers>      "
            + "<from user=\"conal.smith@hpe.com\">From: Smith, Conal</from>      "
            + "<sent date=\"2016-07-22 11:21\">Sent: 22 July 2016 11:21 AM</sent>      "
            + "<to>To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;</to>      "
            + "<cc>Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;</cc>      "
            + "<subject normalised=\"iSTF - CAF Integration\">Subject: RE: iSTF - CAF Integration</subject>    "
            + "</headers>    "
            + "<body>      "
            + "<opening>     Hi Conal,      </opening>      "
            + "<p>No it is not ready yet. </p>      "
            + "<signature>      Thanks      Navamoni      </signature>    "
            + "</body>  "
            + "</email>"
            + "</CONTENT>"
            + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }

    /**
     * Create dummy document from a string containing one email.
     *
     * @return
     */
    private Document createDummyDocumentThreeEmails() throws JDOMException, IOException
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<root>  "
            + "<sentTime time=\"2016-07-22T10:21:00Z\" />  "
            + "<cc>Cc: Reid, Andy &lt;andrew.reid@hpe.com&gt;</cc>      "
            + "<subject>Subject: RE: iSTF - CAF Integration</subject>"
            + "<sent date=\"2016-07-22 10:21\">Sent: 22 July 2016 10:21 AM</sent>      "
            + "<CONTENT>"
            + "<email messageId=\"950124.162336@example.com\" exchangeConversionId=\"sdsd\">    "
            + "<headers>      "
            + "<from user=\"andrew.reid@hpe.com\">From: Reid, Andy</from>      "
            + "<sent date=\"2016-07-22 10:21\">Sent: 22 July 2016 10:21 AM</sent>      "
            + "<to>To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;</to>      "
            + "<cc>Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;</cc>      "
            + "<subject normalised=\"iSTF - CAF Integration\">Subject: RE: iSTF - CAF Integration</subject>    "
            + "</headers>    "
            + "<body>      "
            + "<opening>     Hi Navamoni,      </opening>      "
            + "<p>Is this ready yet?</p>      "
            + "<signature>      Thanks      Andy      </signature>    "
            + "</body>  "
            + "</email>"
            + "<email messageId=\"914214.124136@example.com\" exchangeConversionId=\"sdsd\">    "
            + "<headers>      "
            + "<from user=\"conal.smith@hpe.com\">From: Smith, Conal</from>      "
            + "<sent date=\"2016-07-22 11:21\">Sent: 22 July 2016 11:21 AM</sent>      "
            + "<to>To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;</to>      "
            + "<cc>Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;</cc>      "
            + "<subject normalised=\"iSTF - CAF Integration\">Subject: RE: iSTF - CAF Integration</subject>    "
            + "</headers>    "
            + "<body>      "
            + "<opening>     Hi Conal,      </opening>      "
            + "<p>No it is not ready yet. </p>      "
            + "<signature>      Thanks      Navamoni      </signature>    "
            + "</body>  "
            + "</email>"
            + "<email messageId=\"914214.124136@example.com\" exchangeConversionId=\"sdsd\">    "
            + "<headers>      "
            + "<from user=\"chris.jam.comac@hpe.com\">From: Comac, Chris Jam</from>      "
            + "<sent date=\"2016-07-22 12:21\">Sent: 22 July 2016 12:21 AM</sent>      "
            + "<to>To: Rogan, Adam &lt;paul.navamoni@hpe.com&gt;; Rogan, Adam &lt;dermot.hardy@hpe.com&gt;</to>      "
            + "<cc>Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;</cc>      "
            + "<subject normalised=\"iSTF - CAF Integration\">Subject: RE: iSTF - CAF Integration</subject>    "
            + "</headers>    "
            + "<body>      "
            + "<opening>     Hi Adam,      </opening>      "
            + "<p>Why is this not ready yet? </p>      "
            + "<signature>      Kind Regards, Chris Jam      </signature>    "
            + "</body>  "
            + "</email>"
            + "</CONTENT>"
            + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }
}
