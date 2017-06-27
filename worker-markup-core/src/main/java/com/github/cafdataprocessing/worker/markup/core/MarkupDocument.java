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

import com.github.cafdataprocessing.worker.markup.core.Hashing.HashHelper;
import com.github.cafdataprocessing.worker.markup.core.exceptions.MarkupWorkerExceptions;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreSource;
import com.hpe.caf.codec.JsonCodec;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.markup.HashConfiguration;
import com.hpe.caf.worker.markup.MarkupWorkerResult;
import com.hpe.caf.worker.markup.MarkupWorkerStatus;
import com.hpe.caf.worker.markup.MarkupWorkerTask;
import com.hpe.caf.worker.markup.OutputField;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkupDocument
{
    private static final Logger LOG = LoggerFactory.getLogger(MarkupDocument.class);

    final ExecutorService jepThreadPool = Executors.newSingleThreadExecutor();
    final EmailSplitter emailSplitter = new EmailSplitter(new JepExecutor(jepThreadPool));

    /**
     * Public method to expose Markup worker logic to a document worker
     *
     * @param document Instance of document worker document object
     * @param hashConfiguration The hash configuration to use when generating document hashes
     * @param outputFields The list of fields the worker should output
     * @param isEmail if the document is of email type.
     * @throws InterruptedException throws in cases of a thread being interrupted during processing.
     * @throws com.hpe.caf.api.ConfigurationException throws when configuration for worker is malformed or missing.
     * @throws org.jdom2.JDOMException throws when an error occurs during parsing.
     * @throws java.util.concurrent.ExecutionException throws when an error occurs during email splitting.
     */
    public void markupDocument(final Document document, final List<HashConfiguration> hashConfiguration,
                               final List<OutputField> outputFields, final boolean isEmail)
        throws InterruptedException, ConfigurationException, JDOMException, ExecutionException
    {
        final MarkupWorkerConfiguration config = document.getApplication().getService(ConfigurationSource.class)
            .getConfiguration(MarkupWorkerConfiguration.class);
        final DataStore dataStore = document.getApplication().getService(DataStore.class);

        try {
            MarkupWorkerResult result = markupDocument(ConvertSourceData.getSourceData(document), hashConfiguration, outputFields, isEmail,
                                                       new JsonCodec(), dataStore, config);
            ConvertWorkerResult.updateDocument(document, result);
        } catch (JDOMException jdome) {
            LOG.error("Error during JDOM parsing. ", jdome);
            document.addFailure(MarkupWorkerExceptions.JDOMEXCEPTION_FAILURE, "Error during JDOM parsing.");
        } catch (ExecutionException ee) {
            LOG.error("Error during splitting of emails. ", ee);
            document.addFailure(MarkupWorkerExceptions.EXECUTION_EXCEPTION, "Error during splitting of emails.");
        }
    }

    /**
     * Public method to expose Markup worker logic to a markup worker
     *
     * @param task Markup worker task provided to the worker
     * @param codec codec to use in creation of dataSource
     * @param dataStore data store implementation
     * @param config Markup Worker configuration
     * @return MarkupWorkerResult object containing the result of the workers processing
     * @throws InterruptedException throws in cases of a thread being interrupted during processing.
     * @throws com.hpe.caf.api.ConfigurationException throws when configuration for worker is malformed or missing.
     * @throws org.jdom2.JDOMException throws when an error occurs during parsing.
     * @throws java.util.concurrent.ExecutionException throws when an error occurs during email splitting.
     */
    public MarkupWorkerResult markupDocument(final MarkupWorkerTask task, final DataStore dataStore, final Codec codec,
                                             final MarkupWorkerConfiguration config) throws InterruptedException, ConfigurationException,
                                                                                            JDOMException, ExecutionException
    {
        MarkupWorkerResult result = markupDocument(task.sourceData, task.hashConfiguration, task.outputFields, task.isEmail,
                                                   codec, dataStore, config);
        return result;
    }

    /**
     * Private method to process a document with markup worker.
     *
     * @param sourceData The source data provided to the worker on the task message
     * @param hashConfiguration The hash configuration to use when generating document hashes
     * @param outputFields The list of fields the worker should output
     * @param isEmail if the document is of email type.
     * @param codec codec to use in creation of dataSource
     * @param dataStore data store implementation
     * @param config Markup Worker configuration
     * @return MarkupWorkerResult object containing the result of the workers processing
     * @throws InterruptedException throws in cases of a thread being interrupted during processing.
     * @throws com.hpe.caf.api.ConfigurationException throws when configuration for worker is malformed or missing.
     * @throws org.jdom2.JDOMException throws when an error occurs during parsing.
     * @throws java.util.concurrent.ExecutionException throws when an error occurs during email splitting.
     */
    private MarkupWorkerResult markupDocument(final Multimap<String, ReferencedData> sourceData, final List<HashConfiguration> hashConfiguration,
                                              final List<OutputField> outputFields, final boolean isEmail, final Codec codec, final DataStore dataStore,
                                              final MarkupWorkerConfiguration config)
        throws InterruptedException, ConfigurationException, JDOMException, ExecutionException
    {

        final MarkupHeadersAndBody markupEngine = new MarkupHeadersAndBody(config.getEmailHeaderMappings(),
                                                                           config.getCondensedHeaderMultiLangMappings());

        LOG.info("Starting work");

        try {
            // Standardize the dataMap key-value pairs
            if (isEmail) {
                FieldNameMapper.mapFieldNames(sourceData);
            }

            // Convert the dataMap to an xml document
            DataSource dataSource = new DataStoreSource(dataStore, codec);
            final List<XmlFieldEntry> xmlFieldEntries = XmlConverter.getXmlFieldEntries(dataSource, sourceData);

            org.jdom2.Document doc = GetXmlDocument.getXmlDocument(xmlFieldEntries);
            // Split the content into email tags and mark up the headers and body tags
            if (isEmail) {
                emailSplitter.generateEmailTags(doc);
                markupEngine.markUpHeadersAndBody(doc);
            }

            // Generate the hashes for the fields specified in the hash configuration
            HashHelper.generateHashes(doc, hashConfiguration);

            // Verify that the values of the fields have not been modified
            // (This is the Markup Worker - it should only be marking up fields, without modifying them)
            XmlVerifier.verifyXmlDocument(doc, xmlFieldEntries);

            //Create the worker result with the ReferencedData object containing the bytes for the XML String
            MarkupWorkerResult workerResult = new MarkupWorkerResult();
            workerResult.workerStatus = MarkupWorkerStatus.COMPLETED;

            // Add the list of fields to the
            workerResult.fieldList = XPathHelper.processDocumentWithXPathExpressions(doc, outputFields);

            return workerResult;
        } catch (JDOMException jdome) {
            LOG.error("Error during JDOM parsing. ", jdome);
            throw jdome;

        } catch (ExecutionException ee) {
            LOG.error("Error during splitting of emails. ", ee);
            throw ee;
        }
    }
}
