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

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.testing.FileInputWorkerTaskFactory;
import com.hpe.caf.worker.testing.TestConfiguration;
import com.hpe.caf.worker.testing.TestItem;

import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

public class MarkupWorkerTaskFactory extends FileInputWorkerTaskFactory<MarkupWorkerTask, MarkupTestInput, MarkupTestExpectation>
{
    public MarkupWorkerTaskFactory(TestConfiguration configuration) throws Exception
    {
        super(configuration);
    }

    @Override
    public String getWorkerName()
    {
        return MarkupWorkerConstants.WORKER_NAME;
    }

    @Override
    public int getApiVersion()
    {
        return MarkupWorkerConstants.WORKER_API_VER;
    }

    @Override
    protected MarkupWorkerTask createTask(TestItem<MarkupTestInput, MarkupTestExpectation> testItem, ReferencedData sourceData)
    {
        MarkupWorkerTask task = new MarkupWorkerTask();
        if (testItem.getInputData().getSourceData() == null) {
            testItem.getInputData().setSourceData(LinkedListMultimap.create());
        }

        Multimap<String, ReferencedData> sourceDataMap = ArrayListMultimap.create();
        for (Entry<String, String> sourceDataEntry : testItem.getInputData().getSourceData().entries()) {
            String key = sourceDataEntry.getKey();
            String value = sourceDataEntry.getValue();

            ReferencedData refData = ReferencedData.getWrappedData(value.getBytes(StandardCharsets.UTF_8));
            sourceDataMap.put(key, refData);
        }

        String contentFieldName = testItem.getInputData().getContentFieldName();
        if (Strings.isNullOrEmpty(contentFieldName)) {
            contentFieldName = "CONTENT";
        }

        if (!sourceDataMap.containsKey(contentFieldName)) {
            sourceDataMap.put(contentFieldName, sourceData);
        }

        task.sourceData = sourceDataMap;
        task.hashConfiguration = testItem.getInputData().getHashConfiguration();
        task.outputFields = testItem.getInputData().getOutputFieldList();
        task.isEmail = testItem.getInputData().isEmail();

        return task;
    }
}
