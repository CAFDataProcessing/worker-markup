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

import com.hpe.caf.api.Codec;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.codec.JsonCodec;
import com.hpe.caf.worker.datastore.mem.InMemoryDataStore;
import com.hpe.caf.worker.document.testing.CodeConfigurationSource;
import com.hpe.caf.worker.document.testing.DocumentWorkerConfigurationBuilder;
import com.hpe.caf.worker.document.testing.TestServices;

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