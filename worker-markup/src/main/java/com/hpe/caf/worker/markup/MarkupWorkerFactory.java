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
package com.hpe.caf.worker.markup;

import com.github.cafdataprocessing.worker.markup.core.MarkupDocumentEngine;
import com.github.cafdataprocessing.worker.markup.core.MarkupWorkerConfiguration;
import com.github.cafdataprocessing.worker.markup.core.MarkupWorkerHealthCheck;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.api.HealthResult;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.InvalidTaskException;
import com.hpe.caf.api.worker.Worker;
import com.hpe.caf.api.worker.WorkerException;
import com.hpe.caf.worker.AbstractWorkerFactory;

/**
 * Factory class for creating a MarkupWorker.
 */
public class MarkupWorkerFactory extends AbstractWorkerFactory<MarkupWorkerConfiguration, MarkupWorkerTask>
{
    private final MarkupWorkerConfiguration config;
    private final MarkupDocumentEngine markupDocument;

    public MarkupWorkerFactory(ConfigurationSource configSource, DataStore store, Codec codec) throws WorkerException
    {
        super(configSource, store, codec, MarkupWorkerConfiguration.class, MarkupWorkerTask.class);
        config = getMarkupWorkerConfig(configSource);
        markupDocument = new MarkupDocumentEngine();
    }

    @Override
    protected String getWorkerName()
    {
        return MarkupWorkerConstants.WORKER_NAME;
    }

    @Override
    protected int getWorkerApiVersion()
    {
        return MarkupWorkerConstants.WORKER_API_VER;
    }

    @Override
    public void shutdown()
    {
        markupDocument.close();
    }

    /**
     * Create a worker given a task, using DataStore, ConfiguratonSource and Codec passed in the constructor.
     *
     * @param task
     * @return MarkupWorker
     * @throws InvalidTaskException
     */
    @Override
    public Worker createWorker(MarkupWorkerTask task) throws InvalidTaskException
    {
        return new MarkupWorker(
            task,
            getDataStore(),
            getConfiguration().getOutputQueue(),
            getCodec(),
            config,
        markupDocument);
    }

    @Override
    public String getInvalidTaskQueue()
    {
        return getConfiguration().getOutputQueue();
    }

    @Override
    public int getWorkerThreads()
    {
        return getConfiguration().getThreads();
    }

    /**
     * MarkupWorkerFactory is responsible for calling the health-check to view the status of the worker and this is displayed on Marathon.
     *
     * @return HealthResult
     */
    @Override
    public HealthResult healthCheck()
    {
        MarkupWorkerHealthCheck healthCheck = new MarkupWorkerHealthCheck();
        return healthCheck.healthCheck();
    }

    /**
     * Retrieves the Markup Worker configuration from a standard CAF Configuration Source
     *
     * @param configSource the CAF Configuration Source to use the retrieve the configuration from
     * @return the Markup Worker configuration object
     * @throws WorkerException if there is an issue retrieving the configuration
     */
    private static MarkupWorkerConfiguration getMarkupWorkerConfig(final ConfigurationSource configSource) throws WorkerException
    {
        try {
            return configSource.getConfiguration(MarkupWorkerConfiguration.class);
        } catch (ConfigurationException ce) {
            throw new WorkerException("Failed to initialise Markup Worker configuration", ce);
        }
    }
}
