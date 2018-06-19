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

import com.github.cafdataprocessing.worker.markup.core.exceptions.AddHeadersException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.api.worker.DataStoreSource;
import com.hpe.caf.codec.JsonCodec;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.datastore.mem.InMemoryDataStore;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

/**
 * Tests that when the add headers flag is set, that headers are correctly added to xml output for specified field
 * using other source data entries passed during XML conversion.
 */
public class AddHeadersToEmailTest {

    /**
     * Basic test verifying that all expected headers are appended to start of specified field. All known headers are
     * provided in the passed source data.
     */
    @Test
    public void addAllHeadersTest() throws DataStoreException, AddHeadersException {
        DataStore store = new InMemoryDataStore();
        DataSource dataSource = new DataStoreSource(store, new JsonCodec());
        Multimap<String, ReferencedData> sourceData = ArrayListMultimap.create();
        String fromValue = "\"Anne Marie O'Halloran\" <annemarie.ohalloran@microfocus.com>";
        String toValue= "\"Dermot Hardy\" <dermot.hardy@microfocus.com>";
        String ccValue = "\"Anthony McGreevy\" <anthony.mcgreevy@microfocus.com>, \"Michael McAlynn\" " +
                "<michael.mcalynn@microfocus.com>, \"Christopher Comac\" <christopher.jam.comac@microfocus.com>";
        String bccValue = "\"Hanity O'Flaherty\" <han.fla@microfocus.com>, " +
                "\"Jeeves Rochester The Third\" <jeeves.rochester.third@butler.com>";
        String sentValue = "Wed Sep 20 08:34:58 2017";
        String subjectValue = "RE: Charity sleep out";
        addHeaderFieldsToSourceData(store, sourceData, fromValue, toValue, ccValue, bccValue, sentValue,
                subjectValue);

        String fieldToAddHeadersTo = "CONTENT";
        String contentValue = getContentValueForTest();
        sourceData.put(fieldToAddHeadersTo, ReferencedData.getWrappedData(contentValue.getBytes(Charset.forName("UTF-8"))));

        boolean addEmailHeaders = true;
        List<XmlFieldEntry> returnedEntries =
                XmlConverter.getXmlFieldEntries(dataSource, sourceData, true, addEmailHeaders);
        Optional<XmlFieldEntry> updatedFieldOptional =
                returnedEntries.stream().filter(ent -> ent.getName().equals(fieldToAddHeadersTo)).findFirst();
        Assert.assertTrue("Expected the field to add headers to to have been returned in XML entries.",
                updatedFieldOptional.isPresent());
        XmlFieldEntry updatedField = updatedFieldOptional.get();
        String updatedText = updatedField.getText();
        Assert.assertNotNull("Text on updated entry should not be null.", updatedText);

        // Check that updated text starts with the headers
        String expectedFromHeaderPrefix = "From: ";
        int updatedValueCheckStartIndex = 0;
        int updatedValueCheckEndIndex = expectedFromHeaderPrefix.length()  + fromValue.length();
        Assert.assertEquals("Updated text should have 'from' header at start.", expectedFromHeaderPrefix + fromValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));

        // Update the start index to the entirety of from string plus the new lines
        updatedValueCheckStartIndex += updatedValueCheckEndIndex + 1;
        String expectedToHeaderPrefix = "To: ";
        updatedValueCheckEndIndex =
                updatedValueCheckStartIndex + expectedToHeaderPrefix.length() + toValue.length();
        Assert.assertEquals("Updated text should have 'to' header at expected position.",
                expectedToHeaderPrefix + toValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));

        // Update the start index to the entirety of from and to strings plus the new line
        updatedValueCheckStartIndex = updatedValueCheckEndIndex + 1;
        String expectedCcHeaderPrefix = "CC: ";
        updatedValueCheckEndIndex =
                updatedValueCheckStartIndex + expectedCcHeaderPrefix.length() + ccValue.length();
        Assert.assertEquals("Updated text should have 'cc' header at expected position.",
                expectedCcHeaderPrefix + ccValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));

        // Update the start index to the entirety of from, to and cc strings plus the new lines
        updatedValueCheckStartIndex = updatedValueCheckEndIndex + 1;
        String expectedBccHeaderPrefix = "BCC: ";
        updatedValueCheckEndIndex =
                updatedValueCheckStartIndex + expectedBccHeaderPrefix.length() + bccValue.length();
        Assert.assertEquals("Updated text should have 'bcc' header at expected position.",
                expectedBccHeaderPrefix + bccValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));

        // Update the start index to the entirety of from, to, cc and bcc strings plus the new lines
        updatedValueCheckStartIndex = updatedValueCheckEndIndex + 1;
        String expectedSentHeaderPrefix = "Date: ";
        updatedValueCheckEndIndex =
                updatedValueCheckStartIndex + expectedSentHeaderPrefix.length() + sentValue.length();
        Assert.assertEquals("Updated text should have 'sent' header at expected position.",
                expectedSentHeaderPrefix + sentValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));


        // Update the start index to the entirety of from, to, cc, bcc and sent strings plus the new lines
        updatedValueCheckStartIndex = updatedValueCheckEndIndex + 1;
        String expectedSubjectHeaderPrefix = "Subject: ";
        updatedValueCheckEndIndex =
                updatedValueCheckStartIndex + expectedSubjectHeaderPrefix.length() + subjectValue.length();
        Assert.assertEquals("Updated text should have 'subject' header at expected position.",
                expectedSubjectHeaderPrefix + subjectValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));

        // Expecting two newlines after end of headers
        updatedValueCheckStartIndex = updatedValueCheckEndIndex;
        updatedValueCheckEndIndex += 2;
        Assert.assertEquals("Updated text should have two new lines after the headers section.",
                "\n\n",
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));

        // Remaining value should be the original content value
        updatedValueCheckStartIndex = updatedValueCheckEndIndex;
        updatedValueCheckEndIndex += contentValue.length();
        Assert.assertEquals("Updated text should have original content value after the headers section.",
                contentValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));
    }

    /**
     * Tests that when some header values are omitted that the output is as expected.
     */
    @Test
    public void addSomeHeadersTest() throws DataStoreException, AddHeadersException {
        DataStore store = new InMemoryDataStore();
        DataSource dataSource = new DataStoreSource(store, new JsonCodec());
        Multimap<String, ReferencedData> sourceData = ArrayListMultimap.create();
        String fromValue = "\"Anne Marie O'Halloran\" <annemarie.ohalloran@microfocus.com>";
        String ccValue = "\"Anthony McGreevy\" <anthony.mcgreevy@microfocus.com>, \"Michael McAlynn\" " +
                "<michael.mcalynn@microfocus.com>, \"Christopher Comac\" <christopher.jam.comac@microfocus.com>";
        String subjectValue = "RE: Charity sleep out";
        addHeaderFieldsToSourceData(store, sourceData, fromValue, null, ccValue, null, null,
                subjectValue);

        String fieldToAddHeadersTo = "CONTENT";
        String contentValue = getContentValueForTest();
        sourceData.put(fieldToAddHeadersTo, ReferencedData.getWrappedData(contentValue.getBytes(Charset.forName("UTF-8"))));

        boolean addEmailHeaders = true;
        List<XmlFieldEntry> returnedEntries =
                XmlConverter.getXmlFieldEntries(dataSource, sourceData, true, addEmailHeaders);
        Optional<XmlFieldEntry> updatedFieldOptional =
                returnedEntries.stream().filter(ent -> ent.getName().equals(fieldToAddHeadersTo)).findFirst();
        Assert.assertTrue("Expected the field to add headers to to have been returned in XML entries.",
                updatedFieldOptional.isPresent());
        XmlFieldEntry updatedField = updatedFieldOptional.get();
        String updatedText = updatedField.getText();
        Assert.assertNotNull("Text on updated entry should not be null.", updatedText);

        // Check that updated text starts with the headers
        String expectedFromHeaderPrefix = "From: ";
        int updatedValueCheckStartIndex = 0;
        int updatedValueCheckEndIndex = expectedFromHeaderPrefix.length()  + fromValue.length();
        Assert.assertEquals("Updated text should have 'from' header at start.", expectedFromHeaderPrefix + fromValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));

        // Update the start index to the entirety of from string plus the new lines
        updatedValueCheckStartIndex = updatedValueCheckEndIndex + 1;
        String expectedCcHeaderPrefix = "CC: ";
        updatedValueCheckEndIndex =
                updatedValueCheckStartIndex + expectedCcHeaderPrefix.length() + ccValue.length();
        Assert.assertEquals("Updated text should have 'cc' header at expected position.",
                expectedCcHeaderPrefix + ccValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));


        // Update the start index to the entirety of from, to, cc strings plus the new lines
        updatedValueCheckStartIndex = updatedValueCheckEndIndex + 1;
        String expectedSubjectHeaderPrefix = "Subject: ";
        updatedValueCheckEndIndex =
                updatedValueCheckStartIndex + expectedSubjectHeaderPrefix.length() + subjectValue.length();
        Assert.assertEquals("Updated text should have 'subject' header at expected position.",
                expectedSubjectHeaderPrefix + subjectValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));

        // Expecting two newlines after end of headers
        updatedValueCheckStartIndex = updatedValueCheckEndIndex;
        updatedValueCheckEndIndex += 2;
        Assert.assertEquals("Updated text should have two new lines after the headers section.",
                "\n\n",
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));

        // Remaining value should be the original content value
        updatedValueCheckStartIndex = updatedValueCheckEndIndex;
        updatedValueCheckEndIndex += contentValue.length();
        Assert.assertEquals("Updated text should have original content value after the headers section.",
                contentValue,
                updatedText.substring(updatedValueCheckStartIndex, updatedValueCheckEndIndex));
    }

    private void addHeaderFieldsToSourceData(DataStore store, Multimap<String, ReferencedData> sourceData, String fromValue,
                                             String toValue, String ccValue, String bccValue, String sentValue,
                                             String subjectValue) throws DataStoreException {
        if(fromValue!=null) {
            sourceData.put("from", ReferencedData.getWrappedData(fromValue.getBytes(Charset.forName("UTF-8"))));
        }
        if(toValue!=null) {
            sourceData.put("to", ReferencedData.getWrappedData(toValue.getBytes(Charset.forName("UTF-8"))));
        }
        if(ccValue!=null) {
            sourceData.put("CC", ReferencedData.getWrappedData(ccValue.getBytes(Charset.forName("UTF-8"))));
        }
        if(bccValue!=null) {
            String bccStorageReference = store.store(bccValue.getBytes(Charset.forName("UTF-8")), "");
            sourceData.put("bcC", ReferencedData.getReferencedData(bccStorageReference));
        }
        if(sentValue!=null) {
            sourceData.put("sent", ReferencedData.getWrappedData(sentValue.getBytes(Charset.forName("UTF-8"))));
        }
        if(subjectValue!=null) {
            sourceData.put("subject", ReferencedData.getWrappedData(subjectValue.getBytes(Charset.forName("UTF-8"))));
        }
    }

    private String getContentValueForTest(){
        return "Thanks Anne Marie.\\n \\nWe are HPE guys.\\n \\n \\nFrom: Anne Marie O'Halloran " +
                "[mailto:annemarie.ohalloran@microfocus.com] \\nSent: 20 September 2017 9:07 AM\\nTo: Dermot Hardy " +
                "<dermot.hardy@microfocus.com>\\nCc: Anthony McGreevy <anthony.mcgreevy@microfocus.com>; Michael McAlynn " +
                "<michael.mcalynn@microfocus.com>; Christopher Comac <christopher.jam.comac@microfocus.com>\\nSubject: " +
                "RE: Charity sleep out\\n \\nHi Guys,\\n \\nDon’t forget to Pop up to the HPE floor and invite those guys. " +
                "\\n \\nAM x\\n \\nFrom: Dermot Hardy \\nSent: 19 September 2017 17:57\\nTo: " +
                "Ronnie McIlwaine <ronnie.mcIlwaine@microfocus.com>\\nCc: Anthony McGreevy <anthony.mcgreevy@microfocus.com>; " +
                "Michael McAlynn <michael.mcalynn@microfocus.com>; Christopher Comac <christopher.jam.comac@microfocus.com>; " +
                "&Belfast Social Committee <BelfastSocial@microfocus.com>\\nSubject: Charity sleep out\\n \\nHi " +
                "Ronnie,\\n \\nCould you please forward this around the office?\\n \\nThanks,\\nDermot\\n \\n \\n \\nIn " +
                "a couple of weeks (6th October) myself and three of our colleagues here in the Belfast office are " +
                "going to be sleeping outside for one night to help to raise money for homeless young people.  " +
                "(Thankfully the weather looks like it’s starting to change from what it’s been for the last couple " +
                "of weeks.)\\n \\nTo fundraise we’re planning to have a bun sale this Friday (22nd) and to hold a " +
                "pool tournament the following Friday (29th).\\n \\nNo need to register for a bun but if you’d like to " +
                "be involved in the pool tournament then can you please add your name to the spreadsheet below so that " +
                "I can get an idea of numbers and start to put together a schedule for it.\\n \\nPool Tournament " +
                "Registration:\\nhttps://docs.google.com/spreadsheets/d/11uyFfo6-z8mnBTsF-Ns4rZFDdroCUKj8uGiBB3Jw208/" +
                "edit?usp=sharing\\n \\nWe’ll not be collecting money so if you’d like to be involved then please make " +
                "a donation to one of us using the site below:\\n \\nDonation Site:\\nhttps://www.justgiving.com/teams" +
                "/micro-focus-belfast-byte-night-2017-team\\n \\n(And obviously we’d also love to take donations from " +
                "anybody even if they don’t want a bun or to play pool).\\n \\nIf you’re not familiar with the event:\\n" +
                "Lots of IT and technology companies from around the UK participate.  The money directly supports " +
                "homeless young people, particularly those who have ended up on the streets through abuse.  Action for " +
                "Children finds them and supports them to get their lives back on track.  If you’d like to know more " +
                "please take a look here:\\nhttps://www.bytenight.org.uk/\\n \\nThanks very much,\\nDermot\\n \\n\\n";
    }
}
