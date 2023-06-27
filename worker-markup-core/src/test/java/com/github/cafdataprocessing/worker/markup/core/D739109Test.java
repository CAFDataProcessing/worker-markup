/*
 * Copyright 2017-2023 Open Text.
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.markup.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public final class D739109Test
{
    @Test
    public void reproduceD739109Test() throws Exception
    {
        final ImmutableMultimap<String, String> fields = ImmutableMultimap.<String, String>builder()
            .put("ADDRESS_CC", "emily.brown@example.com")
            .put("ADDRESS_DISPLAY_CC", "Emily Brown")
            .put("ADDRESS_DISPLAY_FROM", "Jessica Wilson")
            .put("ADDRESS_DISPLAY_TO", "[\"David Johnson\",\"Sarah Thompson\"]")
            .put("ADDRESS_FROM", "jessica.wilson@blueskyadventures.com")
            .put("ADDRESS_TO", "[\"david.johnson@example.com\",\"sarah.thompson@example.com\"]")
            .put("CC", "\"Emily Brown\" <emily.brown@example.com>")
            .put("COLLECTION_STATUS", "CONTENT")
            .put("CONTENT_PRIMARY", "Dear David,\n \nThis entire section is redacted.\n \nKind Regards,\n\nBule Sky Australia\n\nJessica Wilson\nSOLUTION DESIGN ANALYST\nGlobal Services\nBule Sky Australia Pty Limited\nLevel 1\n101 Firehose Road\nBLUESKY PARK NSW 8542 AUSTRALIA - Please think before you print and use duplex printing to preserve resources\nPlease think before you print and use duplex printing to preserve resources\nPlease think before you print and use duplex printing to preserve resources\n| PH: (02) 7458 1254 | Fax: (02) 7843 9674 | E: jessica.wilson@blueskyadventures.com | W:www.blueskyadventures.com |\n \nFrom: Jessica Wilson \nSent: Monday, 13 August 2012 11:57 AM\nTo: 'david.johnson@example.com'\nSubject: GHRE")
            .put("DATE_CREATED", "1596169872")
            .put("DATE_FIRST_PROCESSED", "1675644022")
            .put("DATE_MODIFIED", "1596169872")
            .put("DATE_POSTED", "1596169872")
            .put("DATE_PROCESSED", "1675644022")
            .put("DEVICE_ID", "6685")
            .put("DIRECTION", "OTH")
            .put("DOCUMENT_HIERARCHY", "{\"name\":\"FAMILY_ROOT\"}")
            .put("EXTRACTION_TYPE", "Text")
            .put("FILEPATH", "/RSC Program Weekly Status Report @ 24-Jul-20")
            .put("FILE_EXT", "msg")
            .put("FILE_NAME", "RSC Program Weekly Status Report @ 24-Jul-20")
            .put("FILE_SIZE", "2230784")
            .put("FROM", "\"Jessica Wilson\" <jessica.wilson@blueskyadventures.com>")
            .put("HASH", "6f968299b260cadb5e93e1000c64d0db637cc794")
            .put("HASH_LOWRES", "936067404")
            .put("HAS_ATTACHMENTS", "true")
            .put("ID", "33555124")
            .put("ID_PATH", "/33555124")
            .put("IS_HEAD_OF_FAMILY", "true")
            .put("IS_ROOT_DOC", "true")
            .put("LANGUAGE_CODES", "{\"CODE\":\"en\",\"CONFIDENCE\":\"99\"}")
            .put("MESSAGE_ID", "<3fa85f6457174562b3fc2c963f66afa6@PW0892EX13N370.VC.DET.BSA.COM>")
            .put("MIMETYPE", "application/vnd.ms-outlook")
            .put("PRIORITY", "1")
            .put("RANDOM_NUMBER", "1510151288")
            .put("REPOSITORY_ID", "14362")
            .put("REPOSITORY_IDS", "14362")
            .put("REPOSITORY_TYPE", "CONTENTMANAGER")
            .put("REPO_FOLDER", "INFORMATION MANAGEMENT - REPORTING - RSC Progress Status Reports")
            .put("REPO_RECORDTYPE", "E-MAIL")
            .put("REPO_URI", "25020764")
            .put("ROOT_DATE_MODIFIED", "1596169872")
            .put("ROOT_FILE_EXT", "msg")
            .put("ROOT_FILE_SIZE", "2230784")
            .put("ROOT_ID", "33555124")
            .put("ROOT_RANDOM_NUMBER", "1510151288")
            .put("ROOT_REPO_RECORDTYPE", "E-MAIL")
            .put("SCAN_REFERENCE", "25020764|1596171370|1|5e666d43ea766dcc3d84d563ad2a8ce2")
            .put("SOURCE_REFERENCE", "25020764||1|")
            .put("THREAD_INDEX", "AdZm8pZEdWbUOLxLTW2A0Ups6GvBEg==")
            .put("THREAD_TOPIC", "RSC Program Weekly Status Report @ 24-Jul-20")
            .put("TITLE", "RSC Program Weekly Status Report @ 24-Jul-20")
            .put("TO", "\"David Johnson\" <david.johnson@example.com>, \"Sarah Thompson\" <sarah.thompson@example.com>")
            .put("TYPE", "345")
            .build();

        final MarkupDocumentEngine markupDocumentEngine = new MarkupDocumentEngine();
        final MarkupWorkerTask task = new MarkupWorkerTask();
        task.sourceData = MultimapBuilder.hashKeys().arrayListValues().build(
            Multimaps.transformValues(fields, str -> ReferencedData.getWrappedData(str.getBytes(StandardCharsets.UTF_8))));
        task.isEmail = true;
        final DataStore dataStore = mock(DataStore.class);
        final Codec codec = mock(Codec.class);
        final MarkupWorkerConfiguration config = new MarkupWorkerConfiguration();
        config.setAddEmailHeadersDuringMarkup(true);
        config.setEmailHeaderMappings(ImmutableMap.<String, List<String>>builder()
            .put("From", Arrays.asList("Von", "De", "Van", "Fra", "Från"))
            .put("Subject", Arrays.asList("Betreff", "Asunto", "Objet"))
            .put("To", Arrays.asList("Sent to", "Recipient", "An", "Para", "À"))
            .put("Sent", Arrays.asList("Date", "Date_Sent", "DateSent", "Gesendet", "Fecha", "Envoyé", "Datum", "Skickat", "Sendt"))
            .build());
        config.setCondensedHeaderMultiLangMappings(ImmutableMap.<String, List<String>>builder()
            .put("On", Arrays.asList("Le", "W dniu", "Op", "Am", "På", "Den"))
            .put("Separator", Arrays.asList("użytkownik"))
            .put("Wrote", Arrays.asList("a écrit", "napisał", "schreef", "verzond", "geschreven", "schrieb", "skrev"))
            .build());
        config.setInputFieldMappings(Arrays.asList(
            createInputFieldMapping("HASH", "BINARY_HASH_SHA1"),
            createInputFieldMapping("TYPE", "DOC_FORMAT_CODE"),
            createInputFieldMapping("IS_ROOT_DOC", "IS_ROOT"),
            createInputFieldMapping("CONTENT_PRIMARY", "CONTENT"),
            createInputFieldMapping("FAMILY_TYPE", "FAMILY_DOC_FORMAT_CODE"),
            createInputFieldMapping("PRIORITY", "priority"),
            createInputFieldMapping("IS_HEAD_OF_FAMILY", "IS_FAMILY_ORIGIN"),
            createInputFieldMapping("DATE_POSTED", "sent", InputFieldMapping.Transforms.epochSecondsToISO8601),
            createInputFieldMapping("TITLE", "subject"),
            createInputFieldMapping("THREAD_TOPIC", "CAF_MAIL_CONVERSATION_TOPIC"),
            createInputFieldMapping("THREAD_INDEX", "CAF_MAIL_CONVERSATION_INDEX"),
            createInputFieldMapping("THREAD_REPLY_TO", "CAF_MAIL_CONVERSATION_INDEX")
        ));
        final EmailSplitter emailSplitter = new EmailSplitter();

        markupDocumentEngine.markupDocument(task, dataStore, codec, config, emailSplitter);
    }

    private static InputFieldMapping createInputFieldMapping(final String inputField, final String mapToField)
    {
        return createInputFieldMapping(inputField, mapToField, null);
    }

    private static InputFieldMapping createInputFieldMapping(
        final String inputField,
        final String mapToField,
        final InputFieldMapping.Transforms transform
    )
    {
        final InputFieldMapping mapping = new InputFieldMapping();
        mapping.inputField = inputField;
        mapping.mapToField = mapToField;
        mapping.transform = transform;

        return mapping;
    }
}
