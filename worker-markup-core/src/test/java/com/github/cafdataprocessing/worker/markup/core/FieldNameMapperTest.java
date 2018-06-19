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

import com.github.cafdataprocessing.worker.markup.core.exceptions.MappingException;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.hpe.caf.util.ref.ReferencedData;
import org.junit.Assert;
import org.junit.Test;

public class FieldNameMapperTest
{
    ReferencedData firstTopic = ReferencedData.getWrappedData("a topic".getBytes());
    ReferencedData secondTopic = ReferencedData.getWrappedData("another topic".getBytes());
    ReferencedData firstConversationIndex = ReferencedData.getWrappedData("conversation index".getBytes());
    ReferencedData secondConversationIndex = ReferencedData.getWrappedData("conversation index2".getBytes());
    ReferencedData firstInternetMessageId = ReferencedData.getWrappedData("internet message id".getBytes());
    ReferencedData secondInternetMessageId = ReferencedData.getWrappedData("internet message id".getBytes());
    ReferencedData firstMailInReplyTo = ReferencedData.getWrappedData("mail reply to".getBytes());
    ReferencedData secondMailInReplyTo = ReferencedData.getWrappedData("another mail reply to".getBytes());

    @Test
    public void shouldTransformMultimapContainingDuplicateKvMsgFormatFieldNamesToCafMailFieldNames() throws MappingException
    {
        Multimap<String, ReferencedData> dataMap = LinkedListMultimap.create();

        // KV MSG format fields
        dataMap.put(FieldNameMapper.KV_MSG_MAIL_CONVERSATION_TOPIC, firstTopic);
        dataMap.put(FieldNameMapper.KV_MSG_MAIL_CONVERSATION_TOPIC, secondTopic);
        dataMap.put(FieldNameMapper.KV_MSG_MAIL_CONVERSATION_INDEX, firstConversationIndex);
        dataMap.put(FieldNameMapper.KV_MSG_MAIL_CONVERSATION_INDEX, secondConversationIndex);
        dataMap.put(FieldNameMapper.KV_MSG_MAIL_INTERNET_MESSAGE_ID, firstInternetMessageId);
        dataMap.put(FieldNameMapper.KV_MSG_MAIL_INTERNET_MESSAGE_ID, secondInternetMessageId);
        dataMap.put(FieldNameMapper.KV_MSG_MAIL_IN_REPLY_TO, firstMailInReplyTo);
        dataMap.put(FieldNameMapper.KV_MSG_MAIL_IN_REPLY_TO, secondMailInReplyTo);

        //Transform the fields in the map to CAF recognised metadata fields
        FieldNameMapper.mapFieldNames(dataMap, true, null, null);

        // Check that the KV MSG format fields were transformed to their CAF types
        testThatMailFieldsTransformedToCafTypes(dataMap);
    }

    @Test
    public void shouldTransformMultimapContainingDuplicateKvEmlFieldNamesToCafMailFieldNames() throws MappingException
    {
        Multimap<String, ReferencedData> dataMap = LinkedListMultimap.create();

        // KV EML format fields
        dataMap.put(FieldNameMapper.KV_EML_MAIL_CONVERSATION_TOPIC, firstTopic);
        dataMap.put(FieldNameMapper.KV_EML_MAIL_CONVERSATION_TOPIC, secondTopic);
        dataMap.put(FieldNameMapper.KV_EML_MAIL_CONVERSATION_INDEX, firstConversationIndex);
        dataMap.put(FieldNameMapper.KV_EML_MAIL_CONVERSATION_INDEX, secondConversationIndex);
        dataMap.put(FieldNameMapper.KV_EML_MAIL_INTERNET_MESSAGE_ID, firstInternetMessageId);
        dataMap.put(FieldNameMapper.KV_EML_MAIL_INTERNET_MESSAGE_ID, secondInternetMessageId);
        dataMap.put(FieldNameMapper.KV_EML_MAIL_IN_REPLY_TO, firstMailInReplyTo);
        dataMap.put(FieldNameMapper.KV_EML_MAIL_IN_REPLY_TO, secondMailInReplyTo);

        //Transform the fields in the map to CAF recognised metadata fields
        FieldNameMapper.mapFieldNames(dataMap, true, null, null);

        // Check that the KV EML format fields were transformed to their CAF types
        testThatMailFieldsTransformedToCafTypes(dataMap);
    }

    public void testThatMailFieldsTransformedToCafTypes(Multimap<String, ReferencedData> dataMap)
    {
        Assert.assertTrue("Check " + FieldNameMapper.CAF_MAIL_CONVERSATION_TOPIC + " transformed first value",
                          dataMap.get(FieldNameMapper.CAF_MAIL_CONVERSATION_TOPIC).contains(firstTopic));
        Assert.assertTrue("Check " + FieldNameMapper.CAF_MAIL_CONVERSATION_TOPIC + " transformed second value",
                          dataMap.get(FieldNameMapper.CAF_MAIL_CONVERSATION_TOPIC).contains(secondTopic));

        Assert.assertTrue("Check " + FieldNameMapper.CAF_MAIL_CONVERSATION_INDEX + " transformed first value",
                          dataMap.get(FieldNameMapper.CAF_MAIL_CONVERSATION_INDEX).contains(firstConversationIndex));
        Assert.assertTrue("Check " + FieldNameMapper.CAF_MAIL_CONVERSATION_INDEX + " transformed second value",
                          dataMap.get(FieldNameMapper.CAF_MAIL_CONVERSATION_INDEX).contains(secondConversationIndex));

        Assert.assertTrue("Check " + FieldNameMapper.CAF_MAIL_MESSAGE_ID + " transformed first value",
                          dataMap.get(FieldNameMapper.CAF_MAIL_MESSAGE_ID).contains(firstInternetMessageId));
        Assert.assertTrue("Check " + FieldNameMapper.CAF_MAIL_MESSAGE_ID + " transformed second value",
                          dataMap.get(FieldNameMapper.CAF_MAIL_MESSAGE_ID).contains(secondInternetMessageId));

        Assert.assertTrue("Check " + FieldNameMapper.CAF_MAIL_IN_REPLY_TO + " transformed first value",
                          dataMap.get(FieldNameMapper.CAF_MAIL_IN_REPLY_TO).contains(firstMailInReplyTo));
        Assert.assertTrue("Check " + FieldNameMapper.CAF_MAIL_IN_REPLY_TO + " transformed second value",
                          dataMap.get(FieldNameMapper.CAF_MAIL_IN_REPLY_TO).contains(secondMailInReplyTo));
    }
}
