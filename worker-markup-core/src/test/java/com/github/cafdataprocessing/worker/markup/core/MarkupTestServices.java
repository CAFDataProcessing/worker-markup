/*
 * Copyright 2017-2026 Open Text.
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

import com.github.cafapi.common.api.Codec;
import com.github.cafapi.common.codecs.json.JsonCodec;
import com.github.cafdataprocessing.workers.document.testing.CodeConfigurationSource;
import com.github.cafdataprocessing.workers.document.testing.DocumentWorkerConfigurationBuilder;
import com.github.cafdataprocessing.workers.document.testing.TestServices;
import com.github.workerframework.api.DataStore;
import com.github.workerframework.datastores.mem.InMemoryDataStore;

/**
 * Extension to TestServices to specify the MarkupWorker config
 */
public class MarkupTestServices extends TestServices {
    public MarkupTestServices(DataStore dataStore, CodeConfigurationSource configurationSource, Codec codec) {
        super(dataStore, configurationSource, codec);
    }

    public static TestServices createDefault() {
        return new TestServices(new InMemoryDataStore(), new CodeConfigurationSource(
                new Object[]{
                        DocumentWorkerConfigurationBuilder.configure().withDefaults().build(),
                        new MarkupWorkerConfiguration()
                }), new JsonCodec());
    }
}