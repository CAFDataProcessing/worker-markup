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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class HeaderFieldNameMapperTest
{
    private Map<String, List<String>> emailHeaderMappings;

    @Before
    public void setUp()
    {
        emailHeaderMappings = new HashMap<>();
        List<String> toList = new ArrayList<>();
        toList.add("Sent to");
        toList.add("Recipient");
        toList.add("An");
        toList.add("Para");
        toList.add("À");
        emailHeaderMappings.put("To", toList);

        List<String> sentList = new ArrayList<String>();
        sentList.add("Date Sent");
        sentList.add("Date_Sent");
        sentList.add("DateSent");
        sentList.add("Gesendet");
        sentList.add("Fecha");
        sentList.add("Envoyé");
        sentList.add("Datum");
        sentList.add("Skickat");
        sentList.add("Sendt");
        emailHeaderMappings.put("Sent", sentList);

        List<String> fromList = new ArrayList<>();
        fromList.add("De");
        fromList.add("Von");
        fromList.add("Van");
        fromList.add("Fra");
        fromList.add("Från");
        emailHeaderMappings.put("From", fromList);

        List<String> subjectList = new ArrayList<>();
        subjectList.add("Betreff");
        subjectList.add("Asunto");
        subjectList.add("Objet");
        emailHeaderMappings.put("Subject", subjectList);

        MarkupWorkerConfiguration mwc = new MarkupWorkerConfiguration();
        mwc.setOutputQueue("1");
        mwc.setThreads(99);
        mwc.setEmailHeaderMappings(emailHeaderMappings);
    }

    @Test
    public void shouldStandardiseHeaderNamesTo_To()
    {
        String to = "To";
        assertEquals(to, HeaderFieldNameMapper.standardiseHeaderName("Sent to", emailHeaderMappings));
        assertEquals(to, HeaderFieldNameMapper.standardiseHeaderName("Sent To", emailHeaderMappings));
        assertEquals(to, HeaderFieldNameMapper.standardiseHeaderName("Recipient", emailHeaderMappings));
        assertEquals(to, HeaderFieldNameMapper.standardiseHeaderName("An", emailHeaderMappings));
        assertEquals(to, HeaderFieldNameMapper.standardiseHeaderName("Para", emailHeaderMappings));
        assertEquals(to, HeaderFieldNameMapper.standardiseHeaderName("À", emailHeaderMappings));
    }

    @Test
    public void shouldStandardiseHeaderNamesTo_Sent()
    {
        String sent = "Sent";
        assertEquals(sent, HeaderFieldNameMapper.standardiseHeaderName("Date Sent", emailHeaderMappings));
        assertEquals(sent, HeaderFieldNameMapper.standardiseHeaderName("Date_Sent", emailHeaderMappings));
        assertEquals(sent, HeaderFieldNameMapper.standardiseHeaderName("dateSent", emailHeaderMappings));
        assertEquals(sent, HeaderFieldNameMapper.standardiseHeaderName("Gesendet", emailHeaderMappings));
        assertEquals(sent, HeaderFieldNameMapper.standardiseHeaderName("Fecha", emailHeaderMappings));
        assertEquals(sent, HeaderFieldNameMapper.standardiseHeaderName("Envoyé", emailHeaderMappings));
        assertEquals(sent, HeaderFieldNameMapper.standardiseHeaderName("Datum", emailHeaderMappings));
        assertEquals(sent, HeaderFieldNameMapper.standardiseHeaderName("Skickat", emailHeaderMappings));
        assertEquals(sent, HeaderFieldNameMapper.standardiseHeaderName("Sendt", emailHeaderMappings));
    }

    @Test
    public void shouldStandardiseHeaderNamesTo_From()
    {
        String from = "From";
        assertEquals(from, HeaderFieldNameMapper.standardiseHeaderName("Von", emailHeaderMappings));
        assertEquals(from, HeaderFieldNameMapper.standardiseHeaderName("Van", emailHeaderMappings));
        assertEquals(from, HeaderFieldNameMapper.standardiseHeaderName("Fra", emailHeaderMappings));
        assertEquals(from, HeaderFieldNameMapper.standardiseHeaderName("Från", emailHeaderMappings));
    }

    @Test
    public void shouldStandardiseHeaderNamesTo_Subject()
    {
        String subject = "Subject";
        assertEquals(subject, HeaderFieldNameMapper.standardiseHeaderName("Betreff", emailHeaderMappings));
        assertEquals(subject, HeaderFieldNameMapper.standardiseHeaderName("Asunto", emailHeaderMappings));
        assertEquals(subject, HeaderFieldNameMapper.standardiseHeaderName("Objet", emailHeaderMappings));
    }
}