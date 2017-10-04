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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.testing.TestConfiguration;
import com.hpe.caf.worker.testing.TestItem;
import com.hpe.caf.worker.testing.preparation.PreparationItemProvider;
import org.apache.commons.io.IOUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Result Preparation Provider for the Markup Worker to generate test items in test case generation mode.
 */
public class MarkupResultPreparationProvider extends PreparationItemProvider<MarkupWorkerTask, MarkupWorkerResult, MarkupTestInput, MarkupTestExpectation>
{
    public MarkupResultPreparationProvider(TestConfiguration<MarkupWorkerTask, MarkupWorkerResult, MarkupTestInput, MarkupTestExpectation> configuration)
    {
        super(configuration);
    }

    @Override
    protected TestItem<MarkupTestInput, MarkupTestExpectation> createTestItem(Path inputFile, Path expectedFile) throws Exception
    {
        TestItem<MarkupTestInput, MarkupTestExpectation> item = super.createTestItem(inputFile, expectedFile);

        MarkupWorkerTask task = getTaskTemplate();

        // If no task template, set up default hash configuration and add to the test item's test input
        if (task == null) {
            HashConfiguration hashConfiguration = new HashConfiguration();
            hashConfiguration.fields = new ArrayList<>();

            Field f1 = new Field();
            f1.name = "to";
            f1.normalizationType = NormalizationType.NONE;
            Field f2 = new Field();
            f2.name = "body";
            f2.normalizationType = NormalizationType.REMOVE_WHITESPACE;

            hashConfiguration.fields.add(f1);
            hashConfiguration.fields.add(f2);

            hashConfiguration.name = "Normalized";
            hashConfiguration.scope = Scope.EMAIL_SPECIFIC;

            hashConfiguration.hashFunctions = new ArrayList<>();
            hashConfiguration.hashFunctions.add(HashFunction.XXHASH64);

            // Configuration example two
            Field f4 = new Field();
            f4.name = "cc";
            f4.normalizationType = NormalizationType.NONE;
            Field f5 = new Field();
            f5.name = "subject";
            f5.normalizationType = NormalizationType.NONE;
            Field f6 = new Field();
            f6.name = "sent";
            f6.normalizationType = NormalizationType.REMOVE_WHITESPACE;

            List<Field> fieldsTwo = new ArrayList<>();
            fieldsTwo.add(f4);
            fieldsTwo.add(f5);
            fieldsTwo.add(f6);

            HashConfiguration hashConfigurationTwo = new HashConfiguration();
            hashConfigurationTwo.name = "Varient";
            hashConfigurationTwo.scope = Scope.EMAIL_THREAD;
            hashConfigurationTwo.fields = fieldsTwo;
            hashConfigurationTwo.hashFunctions = new ArrayList<>();
            hashConfigurationTwo.hashFunctions.add(HashFunction.XXHASH64);

            //Creating list of configurations
            List<HashConfiguration> config = new ArrayList<>();
            config.add(hashConfiguration);
            config.add(hashConfigurationTwo);

            List<OutputField> outputFieldList = new ArrayList<>();
            OutputField of1 = new OutputField();
            of1.field = "EXAMPLE_FIELD_NAME";
            of1.xPathExpression = ".";
            outputFieldList.add(of1);

            item.getInputData().setSourceData(LinkedListMultimap.create());
            item.getInputData().setHashConfiguration(config);
            item.getInputData().setOutputFieldList(outputFieldList);
            item.getInputData().setEmail(true);
        } else {
            updateItem(item, task);
        }

        return item;
    }

    private void updateItem(TestItem<MarkupTestInput, MarkupTestExpectation> item, MarkupWorkerTask task) throws Exception
    {
        Multimap<String, String> sourceDataMap = LinkedListMultimap.create();
        for (Entry<String, ReferencedData> sourceDataEntry : task.sourceData.entries()) {

            final String storageReference = sourceDataEntry.getValue().getReference();

            if (storageReference == null || storageReference.isEmpty()) {
                String key = sourceDataEntry.getKey();
                String value = IOUtils.toString(sourceDataEntry.getValue().getData(), "UTF-8");
                sourceDataMap.put(key, value);
            } else {
                throw new Exception("Storage references not supported in markup worker tests.");
            }
        }

        item.getInputData().setSourceData(sourceDataMap);
        item.getInputData().setHashConfiguration(task.hashConfiguration);
        item.getInputData().setOutputFieldList(task.outputFields);
        item.getInputData().setEmail(task.isEmail);
    }
}
