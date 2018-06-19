/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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
import com.github.cafdataprocessing.worker.markup.core.exceptions.AddHeadersException;
import com.github.cafdataprocessing.worker.markup.core.exceptions.MappingException;
import com.github.cafdataprocessing.worker.markup.core.exceptions.MarkupWorkerExceptions;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreSource;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.markup.HashConfiguration;
import com.hpe.caf.worker.markup.MarkupWorkerResult;
import com.hpe.caf.worker.markup.MarkupWorkerStatus;
import com.hpe.caf.worker.markup.MarkupWorkerTask;
import com.hpe.caf.worker.markup.OutputField;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MarkupDocumentEngine
{
    private static final Logger LOG = LoggerFactory.getLogger(MarkupDocumentEngine.class);

    /**
     * Public method to expose Markup Worker logic to a Document Worker
     *
     * @param document Instance of document worker document object
     * @param hashConfiguration The hash configuration to use when generating document hashes
     * @param outputFields The list of fields the worker should output
     * @param isEmail if the document is of email type.
     * @param emailSplitter python email splitter that is used across multiple threads
     * @throws InterruptedException throws in cases of a thread being interrupted during processing.
     * @throws com.hpe.caf.api.ConfigurationException throws when configuration for worker is malformed or missing.
     */
    public void markupDocument(final Document document, final List<HashConfiguration> hashConfiguration,
                               final List<OutputField> outputFields, final boolean isEmail, final EmailSplitter emailSplitter)
        throws InterruptedException, ConfigurationException
    {
        final MarkupWorkerConfiguration config = document.getApplication().getService(ConfigurationSource.class)
            .getConfiguration(MarkupWorkerConfiguration.class);
        final DataStore dataStore = document.getApplication().getService(DataStore.class);
        final Codec codec = document.getApplication().getService(Codec.class);

        String addEmailHeadersDuringMarkupOverrideStr = document.getCustomData("addEmailHeadersDuringMarkup");
        Boolean addEmailHeadersDuringMarkup = Strings.isNullOrEmpty(addEmailHeadersDuringMarkupOverrideStr)
                ? null
                : Boolean.parseBoolean(addEmailHeadersDuringMarkupOverrideStr);

        try {
            MarkupWorkerResult result = markupDocument(ConvertSourceData.getSourceData(document), hashConfiguration,
                    outputFields, isEmail, codec, dataStore, config, emailSplitter,
                    addEmailHeadersDuringMarkup);
            ConvertWorkerResult.updateDocument(document, result);
        } catch (JDOMException jdome) {
            LOG.error("Error during JDOM parsing. ", jdome);
            document.addFailure(MarkupWorkerExceptions.JDOMEXCEPTION_FAILURE, "Error during JDOM parsing.");
        } catch (ExecutionException ee) {
            LOG.error("Error during splitting of emails. ", ee);
            document.addFailure(MarkupWorkerExceptions.EXECUTION_EXCEPTION, "Error during splitting of emails.");
        } catch (AddHeadersException ee) {
            LOG.error("Error adding header values to email. ", ee);
            document.addFailure(MarkupWorkerExceptions.ADD_HEADERS_EXCEPTION, "Error adding headers to email.");
        } catch (MappingException ee) {
            LOG.error("Error mapping input fields for markup. ", ee);
            document.addFailure(MarkupWorkerExceptions.MAPPING_EXCEPTION, "Error mapping input fields for markup. "
                    +ee.getMessage());
        }
    }

    /**
     * Public method to expose Markup Worker logic to a Markup Worker
     *
     * @param task Markup worker task provided to the worker
     * @param codec codec to use in creation of dataSource
     * @param dataStore data store implementation
     * @param config Markup Worker configuration
     * @param emailSplitter Python email splitter that can be shared over multiple threads.
     * @return MarkupWorkerResult object containing the result of the workers processing
     * @throws AddHeadersException throws when there is a failure adding headers to email field value
     * @throws InterruptedException throws in cases of a thread being interrupted during processing.
     * @throws com.hpe.caf.api.ConfigurationException throws when configuration for worker is malformed or missing.
     * @throws org.jdom2.JDOMException throws when an error occurs during parsing.
     * @throws java.util.concurrent.ExecutionException throws when an error occurs during email splitting.
     * @throws MappingException throws when an error occurs mapping input fields for use during markup.
     */
    public MarkupWorkerResult markupDocument(final MarkupWorkerTask task, final DataStore dataStore, final Codec codec,
                                             final MarkupWorkerConfiguration config, final EmailSplitter emailSplitter)
        throws AddHeadersException, InterruptedException, ConfigurationException,
            JDOMException, ExecutionException, MappingException
    {
        MarkupWorkerResult result = markupDocument(task.sourceData, task.hashConfiguration, task.outputFields, task.isEmail,
                                                   codec, dataStore, config, emailSplitter, null);
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
     * @param emailSplitter emailSplitter instance to use in splitting emails in the document
     * @param addEmailHeadersOverride can be passed to override the settings in {@code config} object specifying whether
     *                                email headers should be added to content field value
     * @return MarkupWorkerResult object containing the result of the workers processing
     * @throws AddHeadersException throws when there is a failure adding headers to email field value
     * @throws InterruptedException throws in cases of a thread being interrupted during processing.
     * @throws com.hpe.caf.api.ConfigurationException throws when configuration for worker is malformed or missing.
     * @throws org.jdom2.JDOMException throws when an error occurs during parsing.
     * @throws java.util.concurrent.ExecutionException throws when an error occurs during email splitting.
     * @throws MappingException throws when an error occurs mapping input fields for use during markup.
     */
    private MarkupWorkerResult markupDocument(final Multimap<String, ReferencedData> sourceData,
                                              final List<HashConfiguration> hashConfiguration,
                                              final List<OutputField> outputFields, final boolean isEmail,
                                              final Codec codec, final DataStore dataStore,
                                              final MarkupWorkerConfiguration config, final EmailSplitter emailSplitter,
                                              final Boolean addEmailHeadersOverride)
        throws AddHeadersException, InterruptedException, ConfigurationException, JDOMException,
            ExecutionException, MappingException
    {

        final MarkupHeadersAndBody markupEngine = new MarkupHeadersAndBody(config.getEmailHeaderMappings(),
                                                                           config.getCondensedHeaderMultiLangMappings());

        LOG.info("Starting work");

        try {
            DataSource dataSource = new DataStoreSource(dataStore, codec);

            // Standardize the dataMap key-value pairs
            FieldNameMapper.mapFieldNames(sourceData, isEmail, config.getInputFieldMappings(), dataSource);

            // Use either a provided addEmailHeaders value or the one provided in the config
            boolean addEmailHeaders = addEmailHeadersOverride != null ? addEmailHeadersOverride.booleanValue() : config.shouldAddEmailHeadersDuringMarkup();

            // Convert the dataMap to an xml document
            final List<XmlFieldEntry> xmlFieldEntries =
                    XmlConverter.getXmlFieldEntries(dataSource, sourceData, isEmail, addEmailHeaders);

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
        } catch (AddHeadersException ee) {
            LOG.error("Error adding headers to email.", ee);
            throw ee;
        }
    }
}
