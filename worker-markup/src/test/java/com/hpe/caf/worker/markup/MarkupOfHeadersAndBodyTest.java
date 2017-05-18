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
package com.hpe.caf.worker.markup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class MarkupOfHeadersAndBodyTest
{
    private Map<String, List<String>> emailHeaderMappings;
    private Map<String, List<String>> condensedHeaderMultiLangMappings;

    @Before
    public void setUp()
    {
        emailHeaderMappings = new HashMap<>();
        List<String> toList = new ArrayList<>();
        toList.add("Sent to");
        toList.add("Recipient");
        emailHeaderMappings.put("To", toList);
        List<String> sentList = new ArrayList<String>();
        sentList.add("Date_Sent");
        sentList.add("DateSent");
        emailHeaderMappings.put("Sent", sentList);

        condensedHeaderMultiLangMappings = new HashMap<>();
        List<String> onList = new ArrayList<>();
        onList.add("On");
        onList.add("Le");
        onList.add("W dniu");
        onList.add("Op");
        onList.add("Am");
        onList.add("På");
        onList.add("Den");

        List<String> separatorList = new ArrayList<>();
        separatorList.add(",");
        separatorList.add("użytkownik");

        List<String> wroteList = new ArrayList<>();
        wroteList.add("wrote");
        wroteList.add("sent");
        wroteList.add("a écrit");
        wroteList.add("napisał");
        wroteList.add("schreef");
        wroteList.add("verzond");
        wroteList.add("geschreven");
        wroteList.add("schrieb");
        wroteList.add("skrev");

        condensedHeaderMultiLangMappings.put("On", onList);
        condensedHeaderMultiLangMappings.put("Separator", separatorList);
        condensedHeaderMultiLangMappings.put("Wrote", wroteList);

        MarkupWorkerConfiguration mwc = new MarkupWorkerConfiguration();
        mwc.setOutputQueue("1");
        mwc.setThreads(99);
        mwc.setEmailHeaderMappings(emailHeaderMappings);
        mwc.setCondensedHeaderMultiLangMappings(condensedHeaderMultiLangMappings);
    }


    /*
     * Testing of the condensed header multi language mapping provided from configuration
     */
    @Test
    public void testCondensedHeaderMultiLangMappings() throws IOException, JDOMException
    {
        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/CondensedHeaderMultiLangMapping.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/CondensedHeaderMultiLangMappingExpected.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

   /*
    * Testing markup of headers surrounded with asterisks
    */
    @Test
    public void testHeaderSurroundedWithAsterisks() throws IOException, JDOMException
    {
        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/headerAsterisk.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/headerAsteriskExpected.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

   /*
    * Testing markup of headers surrounded with asterisks and the From field split over two lines
    */
    @Test
    public void testFromFieldSurroundedWithAsterisksWithFromValueSplit() throws IOException, JDOMException
    {
        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/headerAsteriskFromValueOnTwoLines.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/headerAsteriskFromValueOnTwoLinesExpected.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

   /*
    * Testing markup of headers surrounded with asterisks and the From field split over two lines and From is not the first field
    */
    @Test
    public void testFromFieldSurroundedWithAsterisksWithFromValueSplitFromNotFirst() throws IOException, JDOMException
    {
        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/headerAsteriskFromValueOnTwoLinesFromNotFirst.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/headerAsteriskFromValueOnTwoLinesFromNotFirstExpected.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

   /*
    * Testing markup of headers surrounded with asterisks,the From field split over two lines, and in a different language
    */
    @Test
    public void testFromFieldSurroundedWithAsterisksOtherLanguage() throws IOException, JDOMException
    {
        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/OtherLanguageHeaderSurroundedWithAsterisks.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/OtherLanguageHeaderSurroundedWithAsterisksExpected.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

    /*
     * Testing markup of one email tag
     */
    @Test
    public void testMarkupOfOneEmailTag() throws IOException, JDOMException
    {
        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/ThreadedEmail.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/ThreadedEmailMarkedup.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

    /*
     * Testing markup of multiple email tags 
     */
    @Test
    public void testMarkupOfMultipleEmailsTags() throws IOException, JDOMException
    {
        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/MultipleEmailTags.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/MultipleEmailTagsMarkedup.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

    /*
     * Testing markup where possible email header tag contains invalid characters
     * i.e.
     * "'To'" : joe.bloggs@hpe.com; jane.doe@hpe.com
     * Will become
     * <To>"'To'" : joe.bloggs@hpe.com; jane.doe@hpe.com</To>
     */
    @Test
    public void testMarkupForOneEmailWithTagContainingIllegalChars() throws IOException, JDOMException
    {
        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/ThreadedEmailWithIllegalChars.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/ThreadedEmailWithIllegalCharsMarkedup.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

    /*
     * Testing that when Talon miss identifies an element such as 'date' and places it within a single email it does not hit
     * a run time exception. 
     */
    @Test
    public void testMarkupOfOneLineInEmailElement() throws IOException, JDOMException
    {

        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/OneLineInEmailElement.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/OneLineInEmailElementMarkedUp.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

    /*
     * Testing standardised header mapping on a single email tag
     */
    @Test
    public void testFieldNameMappingOnOneEmailHeaderTag() throws IOException, JDOMException
    {
        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/SingleEmailHeaderMapping.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/SingleEmailMarkedupHeaderMapping.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

    /*
     * Testing standardised header mapping on a multiple email tags
     */
    @Test
    public void testFieldNameMappingOnMultipleEmailHeaderTag() throws IOException, JDOMException
    {
        Document xmlDocument = TestUtility.readXmlFile("src/test/resources/xml/MultiEmailHeaderMapping.xml");
        MarkupHeadersAndBody.markUpHeadersAndBody(xmlDocument, emailHeaderMappings, condensedHeaderMultiLangMappings);
        Document docForComparison = TestUtility.readXmlFile("src/test/resources/xml/MultiEmailMarkedupHeaderMapping.xml");

        String docMarkedupValue = xmlDocument.getRootElement().getValue();
        String docForComparisonValue = docForComparison.getRootElement().getValue();

        assertEquals(docForComparisonValue, docMarkedupValue);
        assertTrue(TestUtility.compareHeaderElements(docForComparison, xmlDocument));
    }

    /**
     * Fail test for null documents.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     */
    @Test(expected = NullPointerException.class)
    public void testFailureNullDocument() throws JDOMException, ExecutionException, InterruptedException
    {
        Document nullDoc = null;
        MarkupHeadersAndBody.markUpHeadersAndBody(nullDoc, emailHeaderMappings, condensedHeaderMultiLangMappings);
    }
}
