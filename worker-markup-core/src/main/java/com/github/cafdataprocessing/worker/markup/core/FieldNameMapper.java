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

import com.google.common.collect.Multimap;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.ReferencedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FieldNameMapper
{
    private static final Logger LOG = LoggerFactory.getLogger(FieldNameMapper.class);
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
    public static void mapFieldNames(final Multimap<String, ReferencedData> mapData, final boolean isEmail,
                                     final List<InputFieldMapping> inputFieldMappings, final DataSource dataSource)
    {
        if (inputFieldMappings != null) {
            LOG.trace("Input Field mappings were provided.");
            for (InputFieldMapping entry : inputFieldMappings) {
                renameField(mapData, entry, dataSource);
            }
        }
        if (isEmail) {
            //Loop to go iterate through newKeyNames and perform a check to see if it exists in mapData, if it does - Create a new key-value pair with the original
            //Values. Then remove the old key-value pairs.
            for (Map.Entry<String, String> entry : newKeyNames.entrySet()) {
                renameField(mapData, entry);
            }
        }
    }

    private static void renameField(final Multimap<String, ReferencedData> mapData, final Map.Entry<String, String> entry)
    {
        LOG.trace("Trying to rename field {} to {}", entry.getKey(), entry.getValue());
        final String key = entry.getKey();
        if (mapData.containsKey(key)) {
            LOG.trace("Field found, renaming field {} to {}", entry.getKey(), entry.getValue());
            // For each value associated with the KV key, put the value into the new CAF key name
            mapData.get(key).stream().forEach(mapDataValue -> mapData.put(entry.getValue(), mapDataValue));
            // Remove the instances of the KV key
            mapData.removeAll(key);
        }
    }

    private static void renameField(final Multimap<String, ReferencedData> mapData, final InputFieldMapping inputFieldMapping,
                                    final DataSource dataSource)
    {
        LOG.trace("Trying to rename field {} to {}", inputFieldMapping.inputField, inputFieldMapping.mapToField);
        if (mapData.containsKey(inputFieldMapping.inputField)) {
            LOG.trace("Field found, renaming field {} to {}", inputFieldMapping.inputField, inputFieldMapping.mapToField);
            // For each value associated with the input field, put the value into the new CAF field name
            for(ReferencedData mapDataValue: mapData.get(inputFieldMapping.inputField))
            {
                if(inputFieldMapping.transform!=null)
                {
                    ReferencedData transformedValue;
                    switch (inputFieldMapping.transform)
                    {
                        case epochSecondsToISO8601:
                            transformedValue = epochSecondsToISO8601Transform(mapDataValue, dataSource);
                            break;
                        default:
                            throw new RuntimeException("Unrecognized transform specified on input field mapping: "
                                    +inputFieldMapping.transform.toString());
                    }
                    mapData.put(inputFieldMapping.mapToField, transformedValue);
                }
                else
                {
                    mapData.put(inputFieldMapping.mapToField, mapDataValue);
                }
            }
            // Remove the instances of the input field in its original form
            mapData.removeAll(inputFieldMapping.inputField);
        }
    }

    private static ReferencedData epochSecondsToISO8601Transform(ReferencedData inputValue, DataSource dataSource)
    {
        LOG.debug("Retrieving epoch seconds value to transform to ISO8601");
        String valueToTransform = ReferencedDataRetrieval.getContentAsStringEx(dataSource, inputValue);
        LOG.debug("Parsing epoch seconds value to long.");
        long input = Long.parseLong(valueToTransform);
        String transformedValue = Instant.ofEpochSecond(input).toString();
        LOG.debug("Transformed epoch seconds value to ISO-8601 value.");
        return ReferencedData.getWrappedData(transformedValue.getBytes());
    }
}
