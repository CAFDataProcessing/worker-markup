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
package com.hpe.caf.worker.markup;

import com.hpe.caf.api.worker.TaskMessage;
import com.hpe.caf.worker.testing.*;
import com.hpe.caf.worker.testing.configuration.ValidationSettings;
import com.hpe.caf.worker.testing.validation.PropertyValidatingProcessor;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MarkupWorkerResultValidationProcessor extends PropertyValidatingProcessor<MarkupWorkerResult, MarkupTestInput, MarkupTestExpectation>
{
    public MarkupWorkerResultValidationProcessor(TestConfiguration<MarkupWorkerTask, MarkupWorkerResult, MarkupTestInput, MarkupTestExpectation> testConfiguration,
                                                 WorkerServices workerServices)
    {

        super(testConfiguration, workerServices, ValidationSettings.configure().build());
    }

    @Override
    protected boolean processWorkerResult(TestItem<MarkupTestInput, MarkupTestExpectation> testItem, TaskMessage message, MarkupWorkerResult workerResult)
        throws Exception
    {
        assertEquals(testItem.getExpectedOutputData().getStatus(), workerResult.workerStatus);
        return super.processWorkerResult(testItem, message, workerResult);
    }

    @Override
    protected boolean isCompleted(TestItem<MarkupTestInput, MarkupTestExpectation> testItem, TaskMessage message, MarkupWorkerResult markupWorkerResult)
    {
        return true;
    }

    @Override
    protected Map<String, Object> getExpectationMap(TestItem<MarkupTestInput, MarkupTestExpectation> testItem, TaskMessage message, MarkupWorkerResult markupWorkerResult)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("workerStatus", testItem.getExpectedOutputData().getStatus());
        map.put("fieldList", testItem.getExpectedOutputData().getFieldList());
        return map;
    }

    @Override
    protected Object getValidatedObject(TestItem<MarkupTestInput, MarkupTestExpectation> testItem, TaskMessage message, MarkupWorkerResult markupWorkerResult)
    {
        return markupWorkerResult;
    }
}
