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

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.util.Random;

public class ConversationIndexParserTest
{
    @Test
    public void testParseConversationIndex() throws ConversationIndexParserException
    {
        //  Parse conversation index with some expected replies.
        String conversationIndex = "Ac3pCr/g148OQoCCQSCy8dDjwH7QBwAAzLowAAARRGA=";
        String expectedJSON = "{\"headerDate\":\"2013-01-02T17:01:04.168Z\",\"guid\":\"d78f0e42-8082-4120-b2f1-d0e3c07ed007\",\"childMessages\":[{\"messageDate\":\"2013-01-02T17:23:58.064Z\",\"randomNo\":3,\"sequenceCount\":0},{\"messageDate\":\"2013-01-02T17:25:53.931Z\",\"randomNo\":6,\"sequenceCount\":0}]}";

        String parsedConversationIndex = ConversationIndexParser.parseConversationIndex(conversationIndex);

        assert expectedJSON.equals(parsedConversationIndex);
    }

    @Test(expected = ConversationIndexParserException.class)
    public void testParseConversationIndex_UnexpectedLength() throws ConversationIndexParserException
    {
        //  Generate dummy conversation index value with byte size less than 22. This should throw a ConversationIndexParserException.
        Random random = new Random();
        byte[] ciByteArray = new byte[10];
        random.nextBytes(ciByteArray);

        //  Base64 encode.
        String conversationIndex = Base64.encodeBase64String(ciByteArray);

        //  Parse dummy conversation index.
        ConversationIndexParser.parseConversationIndex(conversationIndex);
    }
}
