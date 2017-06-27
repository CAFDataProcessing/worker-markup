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

import com.google.common.collect.Multimap;
import com.hpe.caf.util.ref.ReferencedData;

import java.util.HashMap;
import java.util.Map;

public final class FieldNameMapper
{
    public static final String KV_MSG_MAIL_CONVERSATION_TOPIC = "conversationtopic";
    public static final String KV_MSG_MAIL_CONVERSATION_INDEX = "caf-mail-conversation-index";
    public static final String KV_MSG_MAIL_INTERNET_MESSAGE_ID = "internetmessageid";
    public static final String KV_MSG_MAIL_IN_REPLY_TO = "caf-mail-in-reply-to";

    public static final String KV_EML_MAIL_CONVERSATION_TOPIC = "thread-topic";
    public static final String KV_EML_MAIL_CONVERSATION_INDEX = "thread-index";
    public static final String KV_EML_MAIL_INTERNET_MESSAGE_ID = "message-id";
    public static final String KV_EML_MAIL_IN_REPLY_TO = "in-reply-to";

    public static final String CAF_MAIL_CONVERSATION_TOPIC = "CAF_MAIL_CONVERSATION_TOPIC";
    public static final String CAF_MAIL_CONVERSATION_INDEX = "CAF_MAIL_CONVERSATION_INDEX";
    public static final String CAF_MAIL_MESSAGE_ID = "CAF_MAIL_MESSAGE_ID";
    public static final String CAF_MAIL_IN_REPLY_TO = "CAF_MAIL_IN_REPLY_TO";

    //Hashmap of each potential key-value pair that could be found in the map
    private static final HashMap<String, String> newKeyNames = new HashMap<>();

    static {
        //MSG
        newKeyNames.put(KV_MSG_MAIL_CONVERSATION_TOPIC, CAF_MAIL_CONVERSATION_TOPIC);
        newKeyNames.put(KV_MSG_MAIL_CONVERSATION_INDEX, CAF_MAIL_CONVERSATION_INDEX);
        newKeyNames.put(KV_MSG_MAIL_INTERNET_MESSAGE_ID, CAF_MAIL_MESSAGE_ID);
        newKeyNames.put(KV_MSG_MAIL_IN_REPLY_TO, CAF_MAIL_IN_REPLY_TO);

        //EML
        newKeyNames.put(KV_EML_MAIL_CONVERSATION_INDEX, CAF_MAIL_CONVERSATION_INDEX);
        newKeyNames.put(KV_EML_MAIL_CONVERSATION_TOPIC, CAF_MAIL_CONVERSATION_TOPIC);
        newKeyNames.put(KV_EML_MAIL_INTERNET_MESSAGE_ID, CAF_MAIL_MESSAGE_ID);
        newKeyNames.put(KV_EML_MAIL_IN_REPLY_TO, CAF_MAIL_IN_REPLY_TO);
    }

    /**
     * Declaring the constructor private to ensure instances of this class are not created
     */
    private FieldNameMapper()
    {
    }

    /**
     * Standardises the key names in the specified Multimap
     */
    public static void mapFieldNames(Multimap<String, ReferencedData> mapData)
    {
        //Loop to go iterate through newKeyNames and perform a check to see if it exists in mapData, if it does - Create a new key-value pair with the original
        //Values. Then remove the old key-value pairs.
        for (Map.Entry<String, String> entry : newKeyNames.entrySet()) {
            final String key = entry.getKey();

            if (mapData.containsKey(key)) {

                // For each value associated with the KV key, put the value into the new CAF key name
                mapData.get(key).stream().forEach(mapDataValue -> mapData.put(entry.getValue(), mapDataValue));
                // Remove the instances of the KV key
                mapData.removeAll(key);
            }
        }
    }
}
