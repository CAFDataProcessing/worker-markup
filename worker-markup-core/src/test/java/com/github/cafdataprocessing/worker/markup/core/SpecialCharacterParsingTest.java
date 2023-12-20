/*
 * Copyright 2017-2024 Open Text.
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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.util.ref.ReferencedData;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.hpe.caf.worker.markup.MarkupWorkerTask;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public final class SpecialCharacterParsingTest
{
    /**
     * D709036: This test checks that '@' symbols are no longer parsed as part of a date
     *          as this throws an unexpected character error.
     */
    @Test
    public void AtSymbolParsingTest() throws Exception {
        final ImmutableMultimap<String, String> fields = ImmutableMultimap.<String, String>builder()
                .put("ADDRESS_TO", "[\"david.johnson@example.com\",\"sarah.thompson@example.com\"]")
                .put("ADDRESS_CC", "emily.brown@example.com")
                .put("ADDRESS_FROM", "jessica.wilson@blueskyadventures.com")
                .put("CONTENT_PRIMARY", "Redacted\n\t\n")
                .put("FILEPATH", "/RSC Program Weekly Status Report @ 24-Jul-20")
                .put("FILE_NAME", "RSC Program Weekly Status Report @ 24-Jul-20")
                .put("REPO_FOLDER", "INFORMATION MANAGEMENT - REPORTING - RSC Progress Status Reports")
                .put("THREAD_TOPIC", "RSC Program Weekly Status Report @ 24-Jul-20")
                .put("TITLE", "RSC Program Weekly Status Report @ 24-Jul-20")
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
        config.setInputFieldMappings(Arrays.asList(
                createInputFieldMapping("CONTENT_PRIMARY", "CONTENT"),
                createInputFieldMapping("TITLE", "subject")
        ));
        final EmailSplitter emailSplitter = new EmailSplitter();

        markupDocumentEngine.markupDocument(task, dataStore, codec, config, emailSplitter);
    }

    private static InputFieldMapping createInputFieldMapping(
            final String inputField,
            final String mapToField
    ) {
        final InputFieldMapping mapping = new InputFieldMapping();
        mapping.inputField = inputField;
        mapping.mapToField = mapToField;

        return mapping;
    }
}
