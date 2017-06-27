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

import com.github.cafdataprocessing.worker.markup.core.MarkupWorkerConfiguration;
import com.hpe.caf.worker.testing.*;
import com.hpe.caf.worker.testing.execution.AbstractTestControllerProvider;

public class MarkupTestControllerProvider extends AbstractTestControllerProvider<MarkupWorkerConfiguration, MarkupWorkerTask, MarkupWorkerResult, MarkupTestInput, MarkupTestExpectation>
{
    public MarkupTestControllerProvider()
    {
        super(MarkupWorkerConstants.WORKER_NAME, MarkupWorkerConfiguration::getOutputQueue, MarkupWorkerConfiguration.class, MarkupWorkerTask.class, MarkupWorkerResult.class, MarkupTestInput.class, MarkupTestExpectation.class);
    }

    @Override
    protected WorkerTaskFactory<MarkupWorkerTask, MarkupTestInput, MarkupTestExpectation> getTaskFactory(TestConfiguration<MarkupWorkerTask, MarkupWorkerResult, MarkupTestInput, MarkupTestExpectation> configuration)
        throws Exception
    {
        return new MarkupWorkerTaskFactory(configuration);
    }

    @Override
    protected ResultProcessor getTestResultProcessor(TestConfiguration<MarkupWorkerTask, MarkupWorkerResult, MarkupTestInput, MarkupTestExpectation> configuration, WorkerServices workerServices)
    {
        return new MarkupWorkerResultValidationProcessor(configuration, workerServices);
    }

    @Override
    protected TestItemProvider getDataPreparationItemProvider(TestConfiguration<MarkupWorkerTask, MarkupWorkerResult, MarkupTestInput, MarkupTestExpectation> configuration)
    {
        return new MarkupResultPreparationProvider(configuration);
    }

    @Override
    protected ResultProcessor getDataPreparationResultProcessor(TestConfiguration<MarkupWorkerTask, MarkupWorkerResult, MarkupTestInput, MarkupTestExpectation> configuration, WorkerServices workerServices)
    {
        return new MarkupWorkerSaveResultProcessor(configuration, workerServices);
    }
}
