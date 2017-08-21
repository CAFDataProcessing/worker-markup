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
package com.github.cafdataprocessing.worker.markup.core;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.WorkerException;
import com.hpe.caf.codec.JsonCodec;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.document.testing.DocumentBuilder;
import com.hpe.caf.worker.document.testing.FieldsBuilder;
import com.hpe.caf.worker.markup.*;
import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Basic testing for MarkupDocumentEngine
 */
public class MarkupDocumentEngineTest {
    /**
     * Tests that the order fields are passed to markupDocument don't affect the hash generated. Tests both Document and
     * SourceData entry points.
     */
    @Test
    public void fieldOrderingIrrelevantForHashTest() throws WorkerException, ConfigurationException, InterruptedException, JDOMException, ExecutionException {
        final ExecutorService jepThreadPool = Executors.newSingleThreadExecutor();
        final EmailSplitter emailSplitter = new EmailSplitter(new JepExecutor(jepThreadPool));
        MarkupDocumentEngine markupDocumentEngine = new MarkupDocumentEngine();
        HashConfiguration hashConfiguration = new HashConfiguration();
        hashConfiguration.name = "Comparison";
        hashConfiguration.scope = Scope.EMAIL_THREAD;
        hashConfiguration.hashFunctions = Arrays.asList(HashFunction.XXHASH64);
        hashConfiguration.fields = new ArrayList<>();
        addHashFieldsToConfiguration(hashConfiguration.fields);
        List<HashConfiguration> hashConfigurations = Arrays.asList(hashConfiguration);

        OutputField outputField = new OutputField();
        outputField.field = "COMPARISON_HASH";
        outputField.xPathExpression = "/root/hash/digest/@value";
        List<OutputField> outputFields = Arrays.asList(outputField);

        boolean isEmail = false;

        Document documentToTest = buildDocumentForHashOrderingTest();
try{
        markupDocumentEngine.markupDocument(documentToTest, hashConfigurations, outputFields, isEmail, emailSplitter);
        com.hpe.caf.worker.document.model.Field documentComparisonHashField = documentToTest.getField("COMPARISON_HASH");
        Assert.assertTrue("COMPARISON_HASH field on worker-document returned should have values.",
                documentComparisonHashField.hasValues());
        String documentComparisonHash = documentComparisonHashField.getStringValues().get(0);

        Codec codec = new JsonCodec();
        final MarkupWorkerConfiguration config = documentToTest.getApplication().getService(ConfigurationSource.class)
                .getConfiguration(MarkupWorkerConfiguration.class);
        final DataStore dataStore = documentToTest.getApplication().getService(DataStore.class);
        Multimap<String, ReferencedData> sourceData = buildSourceDataForHashOrderingTest();

        MarkupWorkerTask firstMarkupWorkerTask = createMarkupWorkerTask(
                sourceData,
                hashConfigurations,
                outputFields,
                isEmail
        );
        MarkupWorkerResult firstMarkupWorkerResult =
                markupDocumentEngine.markupDocument(firstMarkupWorkerTask, dataStore, codec, config, emailSplitter);

        Assert.assertNotNull("Should have got a markup result.", firstMarkupWorkerResult);
        Optional<NameValuePair> firstMarkupWorkerComparisonHashOptional = firstMarkupWorkerResult.fieldList.stream().filter(fi -> fi.name.equals("COMPARISON_HASH")).findFirst();
        Assert.assertTrue("COMPARISON_HASH field should be set on the first resul from the markup worker entry point to markupDocument.",
                firstMarkupWorkerComparisonHashOptional.isPresent());
        NameValuePair firstMarkupWorkerComparisonHash = firstMarkupWorkerComparisonHashOptional.get();

        Assert.assertEquals("COMPARISON_HASH from the Document Worker call should be the same as when called via MarkupWorkerResult entrypoint.",
                documentComparisonHash, firstMarkupWorkerComparisonHash.value);

        //repeat the markupDocument call with a new MarkupWorkerResult and verify hash returned is the same
        Multimap<String, ReferencedData> secondSourceData = buildSourceDataForHashOrderingTest();

        MarkupWorkerTask secondMarkupWorkerTask = createMarkupWorkerTask(
                secondSourceData,
                hashConfigurations,
                outputFields,
                isEmail
        );
        MarkupWorkerResult secondMarkupWorkerResult =
                markupDocumentEngine.markupDocument(secondMarkupWorkerTask, dataStore, codec, config, emailSplitter);
        Assert.assertNotNull("Should have got a markup result.", firstMarkupWorkerResult);
        Optional<NameValuePair> secondMarkupWorkerComparisonHashOptional = secondMarkupWorkerResult.fieldList.stream()
                .filter(fi -> fi.name.equals("COMPARISON_HASH")).findFirst();
        Assert.assertTrue("COMPARISON_HASH field should be set on the second result from the markup worker entry point to markupDocument.",
                secondMarkupWorkerComparisonHashOptional.isPresent());
        NameValuePair secondMarkupWorkerComparisonHash = secondMarkupWorkerComparisonHashOptional.get();

        Assert.assertEquals("COMPARISON_HASH from the Document Worker call should be the same as when called via MarkupWorkerResult entrypoint the second time.",
                documentComparisonHash, secondMarkupWorkerComparisonHash.value);

        Assert.assertEquals("COMPARISON_HASH from the Document Worker call should be the same as when called via MarkupWorkerResult entrypoint the first time.",
                firstMarkupWorkerComparisonHash.value, secondMarkupWorkerComparisonHash.value);
        } finally {
            jepThreadPool.shutdown();
        }
    }

    /**
     * Returns a map of strings to reference data where the string keys are in a randomized order. For use
     * with testing hash ordering is the same no matter the field order.
     * @return SourceData map for markupDocument call.
     */
    private Multimap<String, ReferencedData> buildSourceDataForHashOrderingTest(){
        Multimap<String, ReferencedData> sourceData = LinkedListMultimap.create();
        List<String> hashFieldNames = getHashFieldNames();
        //shuffle the entries in the collection so that when passed to markup the fields will be in a different order
        //for different calls
        Collections.shuffle(hashFieldNames);
        for(String fieldName: hashFieldNames){
            sourceData.put(fieldName, ReferencedData.getWrappedData((fieldName + "_value").getBytes()));
        }
        return sourceData;
    }

    private MarkupWorkerTask createMarkupWorkerTask(Multimap<String, ReferencedData> sourceData,
                List<HashConfiguration> hashConfigurations,
                List<OutputField> outputFields,
                boolean isEmail){
        MarkupWorkerTask task = new MarkupWorkerTask();
        task.sourceData = sourceData;
        task.hashConfiguration = hashConfigurations;
        task.outputFields = outputFields;
        task.isEmail = isEmail;
        return task;
    }

    /**
     * Builds a Document with the hash ordering test fields.
     * @return Built Document
     * @throws WorkerException
     */
    private Document buildDocumentForHashOrderingTest() throws WorkerException {
        final FieldsBuilder fieldsBuilder = DocumentBuilder.configure()
                .withServices(MarkupTestServices.createDefault())
                .withFields();

        for(String fieldName: getHashFieldNames()){
            fieldsBuilder.addFieldValue(fieldName, fieldName + "_value");
        }

        return fieldsBuilder.documentBuilder()
                .build();
    }

    private void addHashFieldsToConfiguration(List<Field> hashFieldsConfiguration){
        for(String fieldName: getHashFieldNames()){
            Field hashField = new Field();
            hashField.name = fieldName;
                hashField.normalizationType = NormalizationType.REMOVE_WHITESPACE;
            hashFieldsConfiguration.add(hashField);
        }
    }

    /**
     * Returns a list of field names to use in hash testing. New instance of this list is created each time so it may be sorted or modified
     * without affecting subsequent calls to the method.
     * @return List of hash field names.
     */
    private List<String> getHashFieldNames(){
        List<String> hashFieldNames = new ArrayList<>();
        hashFieldNames.add("BINARY_HASH_SHA1");
        hashFieldNames.add("GAMMA");
        hashFieldNames.add("ALPHA");
        hashFieldNames.add("BETA");
        hashFieldNames.add("test_99");
        hashFieldNames.add("test_11");
        hashFieldNames.add("CHAOS");
        hashFieldNames.add("CRASH");
        hashFieldNames.add("LINK");
        hashFieldNames.add("alpha");
        hashFieldNames.add("OMEGA");
        hashFieldNames.add("home");
        hashFieldNames.add("EPSILON");
        hashFieldNames.add("DELTA");
        hashFieldNames.add("CONTENT");
        return hashFieldNames;
    }
}
