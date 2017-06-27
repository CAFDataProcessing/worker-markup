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
package com.github.cafdataprocessing.worker.markup.core.DocumentWorkerEntryPoint;

import com.github.cafdataprocessing.worker.markup.core.EmailSplitter;
import com.github.cafdataprocessing.worker.markup.core.FieldNameMapper;
import com.github.cafdataprocessing.worker.markup.core.Hashing.HashHelper;
import com.github.cafdataprocessing.worker.markup.core.JepExecutor;
import com.github.cafdataprocessing.worker.markup.core.MarkupHeadersAndBody;
import com.github.cafdataprocessing.worker.markup.core.MarkupWorkerConfiguration;
import com.github.cafdataprocessing.worker.markup.core.XPathHelper;
import com.github.cafdataprocessing.worker.markup.core.XmlConverter;
import com.github.cafdataprocessing.worker.markup.core.XmlFieldEntry;
import com.github.cafdataprocessing.worker.markup.core.XmlVerifier;
import com.github.cafdataprocessing.worker.markup.core.exceptions.MarkupWorkerExceptions;
import com.google.common.collect.Multimap;
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
import com.hpe.caf.worker.markup.OutputField;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkupWorker
{
    private static final Logger LOG = LoggerFactory.getLogger(MarkupWorker.class);

    final ExecutorService jepThreadPool = Executors.newSingleThreadExecutor();
    final EmailSplitter emailSplitter = new EmailSplitter(new JepExecutor(jepThreadPool));

    /**
     * Private method to process the hash map key pairs and generate the XML.
     *
     * @return MarkupWorkerResult
     * @throws InterruptedException
     */
    private void markupDocument(final Document document, final List<HashConfiguration> hashConfiguration,
                                final List<OutputField> outputFields, final boolean isEmail)
        throws InterruptedException, ConfigurationException
    {

        final MarkupWorkerConfiguration config = document.getApplication()
            .getService(ConfigurationSource.class)
            .getConfiguration(MarkupWorkerConfiguration.class);
        final MarkupHeadersAndBody markupEngine = new MarkupHeadersAndBody(config.getEmailHeaderMappings(),
                                                                           config.getCondensedHeaderMultiLangMappings());
        DataStore dataStore = document.getApplication().getService(DataStore.class);

        LOG.info("Starting work");

        Multimap<String, ReferencedData> sourceData = ConvertSourceData.getSourceData(document);

        try {
            // Standardize the dataMap key-value pairs
            if (isEmail) {
                FieldNameMapper.mapFieldNames(sourceData);
            }

            // Convert the dataMap to an xml document
            DataSource dataSource = new DataStoreSource(dataStore, new JsonCodec());
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

            ConvertWorkerResult.updateDocument(document, workerResult);
        } catch (JDOMException jdome) {
            LOG.error("Error during JDOM parsing. ", jdome);
            document.addFailure(MarkupWorkerExceptions.JDOMEXCEPTION_FAILURE, "Error during JDOM parsing.");

        } catch (ExecutionException ee) {
            LOG.error("Error during splitting of emails. ", ee);
            document.addFailure(MarkupWorkerExceptions.EXECUTION_EXCEPTION, "Error during splitting of emails.");
        }
    }
}
