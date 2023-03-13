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
package com.hpe.caf.worker.markup;

import com.hpe.caf.api.Codec;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.WorkerException;
import com.hpe.caf.api.worker.WorkerFactory;
import com.hpe.caf.api.worker.WorkerFactoryProvider;

/**
 * MarkupWorker factory provider implementation.
 */
public class MarkupWorkerFactoryProvider implements WorkerFactoryProvider
{
    /**
     * Get the worker factory implementation. Called by the ModuleLoader. MarkupWorkerFactoryProvider must be registered by the service
     * file in resources/META-INF/services.
     *
     * @param configSource
     * @param dataStore
     * @param codec
     * @return MarkupWorkerFactory
     * @throws WorkerException
     */
    @Override
    public WorkerFactory getWorkerFactory(final ConfigurationSource configSource, final DataStore dataStore, final Codec codec) throws WorkerException
    {
        return new MarkupWorkerFactory(configSource, dataStore, codec);
    }
}
