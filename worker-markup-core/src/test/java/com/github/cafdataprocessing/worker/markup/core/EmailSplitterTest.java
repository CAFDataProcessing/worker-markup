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
package com.github.cafdataprocessing.worker.markup.core;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.jdom2.Element;

import static org.mockito.Mockito.when;

/**
 *
 * @author rogan
 */
public class EmailSplitterTest
{
    /**
     * Tests for running EmailSplitter with 2 emails and a divider between them
     *
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void splitEmailWithDivider() throws ExecutionException, InterruptedException, JDOMException, IOException
    {
        String expectedEmail1 = "From: Reid, Andy\n"
            + "Sent: 22 July 2016 10:21 AM\n"
            + "To: Paul, Navamoni\n"
            + "Cc: Ploch, Krzysztof\n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "Hi Navamoni,\n"
            + "\n"
            + "Is this ready yet?\n"
            + "\n"
            + "Thanks, Andy\n"
            + "\n";
        String expectedDivider = "---------- Forwarded message ----------\n";
        String expectedEmail2 = "From: Smith, Conal\n"
            + "Sent: 22 July 2016 11:21 AM\n"
            + "To: Paul, Navamoni\n"
            + "Cc: Ploch, Krzysztof\n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "Hi Conal,\n"
            + "\n"
            + "No it is not ready yet.\n"
            + "\n"
            + "Thanks, Navamoni\n";

        Document doc = createDocumentWithSuppliedStrings(expectedEmail1, expectedDivider, expectedEmail2);

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList(0, 12));

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);

        Assert.assertTrue("Assert the first email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(0).getValue().equals(expectedEmail1));
        Assert.assertTrue("Assert the divider is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(1).getValue().equals(expectedDivider));
        Assert.assertTrue("Assert the second email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(2).getValue().equals(expectedEmail2));
    }

    /**
     * Tests for running EmailSplitter with 2 emails and a divider between them
     *
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void splitEmailWithDivider_CheckRegexIncludesWhitespace() throws ExecutionException, InterruptedException, JDOMException, IOException
    {
        String expectedEmail1 = "From: Reid, Andy\n"
            + "Sent: 22 July 2016 10:21 AM\n"
            + "To: Paul, Navamoni\n"
            + "Cc: Ploch, Krzysztof\n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "Hi Navamoni,\n"
            + "\n"
            + "Is this ready yet?\n"
            + "\n"
            + "Thanks, Andy\n"
            + "\n";
        String expectedDivider = "---------- Forwarded message ----------\n"
            +"   \n"
            +"\n";
        String expectedEmail2 = "From: Smith, Conal\n"
            + "Sent: 22 July 2016 11:21 AM\n"
            + "To: Paul, Navamoni\n"
            + "Cc: Ploch, Krzysztof\n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "Hi Conal,\n"
            + "\n"
            + "No it is not ready yet.\n"
            + "\n"
            + "Thanks, Navamoni\n";

        Document doc = createDocumentWithSuppliedStrings(expectedEmail1, expectedDivider, expectedEmail2);

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList(0, 14));

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);

        Assert.assertTrue("Assert the first email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(0).getValue().equals(expectedEmail1));
        Assert.assertTrue("Assert the divider is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(1).getValue().equals(expectedDivider));
        Assert.assertTrue("Assert the second email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(2).getValue().equals(expectedEmail2));
    }

    /**
     * Tests for running EmailSplitter with 2 emails and a divider between them
     *
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void splitEmailWithDivider_CheckOriginalMessageIntegrity() throws ExecutionException, InterruptedException, JDOMException, IOException
    {
        String expectedEmail1 = "From: Reid, Andy\n"
            + "Sent: 22 July 2016 10:21 AM\n"
            + "To: Paul, Navamoni\n"
            + "Cc: Ploch, Krzysztof\n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "Hi Navamoni,\n"
            + "\n"
            + "Is this ready yet?\n"
            + "\n"
            + "Thanks, Andy\n"
            + "\n";
        String expectedDivider1 = "   > ---------- Forwarded message ----------\n";
        String expectedEmail2 = "   > From: Smith, Conal\n"
            + "   > Sent: 22 July 2016 11:21 AM\n"
            + "   > To: Paul, Navamoni\n"
            + "   > Cc: Ploch, Krzysztof\n"
            + "   > Subject: RE: iSTF - CAF Integration    \n"
            + "   > Hi Conal,\n"
            + "   > \n"
            + "   > No it is not ready yet.\n"
            + "   > \n"
            + "   > Thanks, Navamoni\n";
        String expectedDivider2 = "   > > ---------- Original message ----------\n";
        String expectedEmail3 = "   > > From: Smith, Conal\n"
            + "   > > Sent: 22 July 2016 11:21 AM\n"
            + "   > > To: Paul, Navamoni\n"
            + "   > > Cc: Ploch, Krzysztof\n"
            + "   > > Subject: RE: iSTF - CAF Integration    \n"
            + "   > > Hi Conal,\n"
            + "   > > \n"
            + "   > > No it is not ready yet.\n"
            + "   > > \n"
            + "   > > Thanks, Navamoni\n"
            + "adwadwad   > > ----- Original message -----\n";

        Document doc = createDocumentWithSuppliedStrings(expectedEmail1, expectedDivider1, expectedEmail2, expectedDivider2, expectedEmail3);

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList(0, 12, 22, 23));

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);

        Assert.assertTrue("Assert the first email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(0).getValue().equals(expectedEmail1));
        Assert.assertTrue("Assert the first divider is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(1).getValue().equals(expectedDivider1));
        Assert.assertTrue("Assert the second email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(2).getValue().equals(expectedEmail2));
        Assert.assertTrue("Assert the second divider is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(3).getValue().equals(expectedDivider2));
        Assert.assertTrue("Assert the third email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(4).getValue().equals(expectedEmail3));
    }


    /**
     * Tests for running EmailSplitter with two emails and a divider in the body.
     *
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void splitEmailWithDividerInBody() throws ExecutionException, InterruptedException, JDOMException, IOException
    {
        String expectedEmail1 = "From: Reid, Andy\n"
            + "Sent: 22 July 2016 10:21 AM\n"
            + "To: Paul, Navamoni\n"
            + "Cc: Ploch, Krzysztof\n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "Hi Navamoni,\n"
            + "\n"
            + "Is this ready yet?\n"
            + "\n"
            + "Thanks, Andy\n"
            + "\n"
            + "---------- Forwarded message ----------\n"
            + "\n"
            + "\n"
            + "\n"
            + "     \n"
            + "\n"
            + "asdsadw \n"
            +"\n";
        String expectedEmail2 = "From: Smith, Conal\n"
            + "Sent: 22 July 2016 11:21 AM\n"
            + "To: Paul, Navamoni\n"
            + "Cc: Ploch, Krzysztof\n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "Hi Conal,\n"
            + "\n"
            + "No it is not ready yet.\n"
            + "\n"
            + "Thanks, Navamoni\n";

        Document doc = createDocumentWithSuppliedStrings(expectedEmail1, expectedEmail2);

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList(0, 19));

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);

        Assert.assertTrue("Assert the first email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(0).getValue().equals(expectedEmail1));
        Assert.assertTrue("Assert the second email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(1).getValue().equals(expectedEmail2));
    }

    /**
     * Tests for running EmailSplitter with two emails and a divider in the body, and a divider at the last line followed by the end of
     * file character.
     *
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void splitEmailWithDividerAtEndOfFile() throws ExecutionException, InterruptedException, JDOMException, IOException
    {
        String expectedEmail1 = "From: Reid, Andy\n"
            + "Sent: 22 July 2016 10:21 AM\n"
            + "To: Paul, Navamoni\n"
            + "Cc: Ploch, Krzysztof\n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "Hi Navamoni,\n"
            + "\n"
            + "Is this ready yet?\n"
            + "\n"
            + "Thanks, Andy\n"
            + "\n";
        String expectedDivider1 = "---------- Forwarded message ----------\n";
        String expectedEmail2 = "From: Smith, Conal\n"
            + "Sent: 22 July 2016 11:21 AM\n"
            + "To: Paul, Navamoni\n"
            + "Cc: Ploch, Krzysztof\n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "Hi Conal,\n"
            + "\n"
            + "No it is not ready yet.\n"
            + "\n"
            + "Thanks, Navamoni\n";
        String expectedDivider2 = "---------- Forwarded message ----------";

        Document doc = createDocumentWithSuppliedStrings(expectedEmail1, expectedDivider1, expectedEmail2, expectedDivider2);

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList(0, 12));

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);

        Assert.assertTrue("Assert the first email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(0).getValue().equals(expectedEmail1));
        Assert.assertTrue("Assert the first divider is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(1).getValue().equals(expectedDivider1));
        Assert.assertTrue("Assert the second email is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(2).getValue().equals(expectedEmail2));
        Assert.assertTrue("Assert the second divider is as expected.", doc.getRootElement().getChild("CONTENT").getChildren().get(3).getValue().equals(expectedDivider2));
    }

    /**
     * Tests for running EmailSplitter with only one email in the document
     *
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void splitEmailTestSigleEmail() throws ExecutionException, InterruptedException, JDOMException, IOException
    {
        Document doc = createDummyDocumentOneEmail();

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList(0));

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);

        Assert.assertTrue(assertCorrectAmountOfEmailElements(doc, 1));
    }

    /**
     * Test for running EmailSplitter with a chain of two emails
     *
     * @throws org.jdom2.JDOMException
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    @Test
    public void splitEmailTestTwoEmails() throws JDOMException, ExecutionException, InterruptedException, IOException
    {
        Document doc = createDummyDocumentTwoEmails();

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList(0, 8));

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);

        Assert.assertTrue(assertCorrectAmountOfEmailElements(doc, 2));
    }

    /**
     * Test for running EmailSplitter with a chain of three emails
     *
     * @throws org.jdom2.JDOMException
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    @Test
    public void splitEmailTestThreeEmails() throws JDOMException, ExecutionException, InterruptedException, IOException
    {
        Document doc = createDummyDocumentThreeEmails();

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList(0, 8, 16));

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);

        Assert.assertTrue(assertCorrectAmountOfEmailElements(doc, 3));
    }

    @Test
    public void splitEmailTestFourEmails() throws JDOMException, ExecutionException, InterruptedException, IOException
    {
        Document doc = createDummyDocumentFourEmails();

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList(0, 8, 16, 24));

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);

        Assert.assertTrue(assertCorrectAmountOfEmailElements(doc, 4));
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
        Document doc = null;

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList());

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);
    }

    /**
     * Fail test for documents without the correct elements, Content element required to parse emails
     *
     * @throws org.jdom2.JDOMException
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    @Test
    public void testSuccessNoContent() throws JDOMException, ExecutionException, InterruptedException, IOException
    {
        Document doc = createDummyDocumentNoContentTag();

        JepExecutor j = Mockito.mock(JepExecutor.class);
        when(j.getMessageIndexes(Mockito.any())).thenReturn(Arrays.asList());

        EmailSplitter emailSplitter = new EmailSplitter(j);
        emailSplitter.generateEmailTags(doc);

        Assert.assertTrue(assertCorrectAmountOfEmailElements(doc, 0));
    }

    /**
     * Private method for asserting that the correct number of email elements are in the document i.e. - If there are two emails in the
     * chain there should be two <email> elements
     *
     * @param doc
     * @param amount
     * @return
     */
    private boolean assertCorrectAmountOfEmailElements(Document doc, int amount)
    {
        final Element contentElement = doc.getRootElement().getChild("CONTENT");
        int count = contentElement == null ? 0 : contentElement.getChildren("email").size();
        boolean res = (count == amount);

        return res;
    }

    //<editor-fold desc="createDummy methods">
    /**
     * Create dummy document containing one email
     *
     * @return
     */
    private Document createDummyDocumentOneEmail() throws JDOMException, IOException
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<root>"
            + "<CONTENT>"
            + "From: Reid, Andy      "
            + "Sent: 22 July 2016 10:21 AM      "
            + "To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;      "
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      "
            + "Subject: RE: iSTF - CAF Integration    "
            + "     Hi Navamoni,            "
            + "Is this ready yet?      "
            + "      Thanks      Andy          "
            + "</CONTENT>"
            + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }
    /**
     * Create dummy document containing one email
     *
     * @return
     */
    private Document createDocumentWithSuppliedStrings(String ... args) throws JDOMException, IOException
    {
        String str = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<root>" + "<CONTENT>";
        for( String s : args){
            str = str + s;
        }
        str = str + "</CONTENT>" + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(str.getBytes()));
        return doc;
    }


    /**
     * Create dummy document containing two emails
     *
     * @return
     */
    private Document createDummyDocumentTwoEmails() throws JDOMException, IOException
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<root>"
            + "<CONTENT>"
            + "From: Reid, Andy      \n"
            + "Sent: 22 July 2016 10:21 AM      \n"
            + "To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;     \n"
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      \n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "     Hi Navamoni,            \n"
            + "Is this ready yet?      \n"
            + "      Thanks      Andy          \n"
            + "From: Smith, Conal      \n"
            + "Sent: 22 July 2016 11:21 AM      \n"
            + "To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;      \n"
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      \n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "     Hi Conal,            \n"
            + "No it is not ready yet.       \n"
            + "      Thanks      Navamoni          \n"
            + "</CONTENT>"
            + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }

    /**
     * Create dummy document containing three emails
     *
     * @return
     */
    private Document createDummyDocumentThreeEmails() throws JDOMException, IOException
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<root>"
            + "<CONTENT>"
            + "From: Reid, Andy      \n"
            + "Sent: 22 July 2016 10:21 AM      \n"
            + "To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;      \n"
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      \n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "     Hi Navamoni,            \n"
            + "Is this ready yet?      \n"
            + "      Thanks      Andy          \n"
            + "From: Smith, Conal      \n"
            + "Sent: 22 July 2016 11:21 AM      \n"
            + "To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;      \n"
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      \n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "     Hi Conal,            \n"
            + "No it is not ready yet.       \n"
            + "      Thanks      Navamoni          \n"
            + "From: Comac, Chris Jam      \n"
            + "Sent: 22 July 2016 12:21 AM      \n"
            + "To: Rogan, Adam &lt;paul.navamoni@hpe.com&gt;; Rogan, Adam &lt;dermot.hardy@hpe.com&gt;      \n"
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      \n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "     Hi Adam,            \n"
            + "Why is this not ready yet?       \n"
            + "      Kind Regards, Chris Jam          \n"
            + "</CONTENT>"
            + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }

    private Document createDummyDocumentFourEmails() throws JDOMException, IOException
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<root>"
            + "<CONTENT>"
            + "From: Reid, Andy      \n"
            + "Sent: 22 July 2016 10:21 AM      \n"
            + "To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;      \n"
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      \n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "     Hi Navamoni,            \n"
            + "Is this ready yet?      \n"
            + "      Thanks      Andy          \n"
            + "From: Smith, Conal      \n"
            + "Sent: 22 July 2016 11:21 AM      \n"
            + "To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;      \n"
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      \n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "     Hi Conal,            \n"
            + "No it is not ready yet.       \n"
            + "      Thanks      Navamoni          \n"
            + "From: Comac, Chris Jam      \n"
            + "Sent: 22 July 2016 12:21 AM      \n"
            + "To: Rogan, Adam &lt;paul.navamoni@hpe.com&gt;; Rogan, Adam &lt;dermot.hardy@hpe.com&gt;      \n"
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      \n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "     Hi Adam,            \n"
            + "Why is this not ready yet?       \n"
            + "      Kind Regards, Chris Jam          \n"
            + "From: Rogan, Adam      \n"
            + "Sent: 22 July 2016 12:21 AM      \n"
            + "To: Smith, Adam &lt;conal.smith@hpe.com&gt;; Hardy, Dermot &lt; dermot.hardy@hpe.com&gt;      \n"
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      \n"
            + "Subject: RE: iSTF - CAF Integration    \n"
            + "     Hi Conal,            \n"
            + "Won't be done yet      \n"
            + "      King Regards, Adam      \n"
            + "</CONTENT>"
            + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }

    private Document createDummyDocumentNoContentTag() throws JDOMException, IOException
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<root>"
            + "From: Reid, Andy      "
            + "Sent: 22 July 2016 10:21 AM      "
            + "To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;      "
            + "Cc: Ploch, Krzysztof &lt;krzysztof.ploch@hpe.com&gt;      "
            + "Subject: RE: iSTF - CAF Integration    "
            + "     Hi Navamoni,            "
            + "Is this ready yet?      "
            + "      Thanks      Andy          "
            + "</root>";

        SAXBuilder saxBuilder = new SAXBuilder();

        Document doc = saxBuilder.build(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }
}
