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

import com.github.cafdataprocessing.worker.markup.core.EmailSplitter;
import com.github.cafdataprocessing.worker.markup.core.FieldNameMapper;
import com.github.cafdataprocessing.worker.markup.core.MarkupHeadersAndBody;
import com.github.cafdataprocessing.worker.markup.core.XPathHelper;
import com.github.cafdataprocessing.worker.markup.core.XmlConverter;
import com.github.cafdataprocessing.worker.markup.core.XmlFieldEntry;
import com.github.cafdataprocessing.worker.markup.core.XmlVerifier;
import com.google.common.collect.Multimap;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.worker.*;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.ReferencedData;
import com.hpe.caf.worker.AbstractWorker;
import com.github.cafdataprocessing.worker.markup.core.Hashing.HashHelper;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * processing the Hash map key pairs and placing the, into XML.
 */
public class MarkupWorker extends AbstractWorker<MarkupWorkerTask, MarkupWorkerResult>
{
    private final EmailSplitter emailSplitter;
    private final MarkupHeadersAndBody markupEngine;

    /**
     * Logger for logging purposes.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MarkupWorker.class);
    private final DataStore dataStore;

    public MarkupWorker(final MarkupWorkerTask task,
                        final DataStore dataStore,
                        final String outputQueue,
                        final Codec codec,
                        final EmailSplitter emailSplitter,
                        final MarkupHeadersAndBody markupEngine)
        throws InvalidTaskException
    {
        super(task, outputQueue, codec);
        this.dataStore = Objects.requireNonNull(dataStore);
        this.emailSplitter = emailSplitter;
        this.markupEngine = markupEngine;
    }

    @Override
    public String getWorkerIdentifier()
    {
        return MarkupWorkerConstants.WORKER_NAME;
    }

    @Override
    public int getWorkerApiVersion()
    {
        return MarkupWorkerConstants.WORKER_API_VER;
    }

    /**
     * Trigger processing of the source file and determine a response.
     *
     * @return WorkerResponse - a response from the operation.
     * @throws InterruptedException - if the task is interrupted.
     * @throws TaskRejectedException
     */
    @Override
    public WorkerResponse doWork() throws InterruptedException, TaskRejectedException
    {
        MarkupWorkerResult result = processInput();

        if (result.workerStatus == MarkupWorkerStatus.COMPLETED) {
            return createSuccessResult(result);
        } else {
            return createFailureResult(result);
        }
    }

    /**
     * Private method to process the hash map key pairs and generate the XML.
     *
     * @return MarkupWorkerResult
     * @throws InterruptedException
     */
    private MarkupWorkerResult processInput() throws InterruptedException
    {
        LOG.info("Starting work");
        checkIfInterrupted();

        MarkupWorkerTask task = getTask();
        Multimap<String, ReferencedData> sourceData = task.sourceData;
        boolean isEmail = task.isEmail;

        try {
            // Standardize the dataMap key-value pairs
            if (isEmail) {
                FieldNameMapper.mapFieldNames(sourceData);
            }

            // Convert the dataMap to an xml document
            DataSource dataSource = new DataStoreSource(dataStore, getCodec());
            final List<XmlFieldEntry> xmlFieldEntries = XmlConverter.getXmlFieldEntries(dataSource, sourceData);
            Document doc = XmlConverter.createXmlDocument(xmlFieldEntries);

            // Split the content into email tags and mark up the headers and body tags
            if (isEmail) {
                emailSplitter.generateEmailTags(doc);
                markupEngine.markUpHeadersAndBody(doc);
            }

            // Generate the hashes for the fields specified in the hash configuration
            HashHelper.generateHashes(doc, getTask().hashConfiguration);

            // Verify that the values of the fields have not been modified
            // (This is the Markup Worker - it should only be marking up fields, without modifying them)
            XmlVerifier.verifyXmlDocument(doc, xmlFieldEntries);

            //Create the worker result with the ReferencedData object containing the bytes for the XML String
            MarkupWorkerResult workerResult = new MarkupWorkerResult();
            workerResult.workerStatus = MarkupWorkerStatus.COMPLETED;

            // Add the list of fields to the
            workerResult.fieldList = XPathHelper.processDocumentWithXPathExpressions(doc, getTask().outputFields);

            return workerResult;
        } catch (JDOMException jdome) {
            LOG.error("Error during JDOM parsing. ", jdome);
            return createErrorResult(MarkupWorkerStatus.WORKER_FAILED);
        } catch (ExecutionException ee) {
            LOG.error("Error during splitting of emails. ", ee);
            return createErrorResult(MarkupWorkerStatus.WORKER_FAILED);
        }
    }

    /**
     * If an error in the worker occurs, create a new MarkupWorkerResult with the corresponding worker failure status.
     */
    private static MarkupWorkerResult createErrorResult(MarkupWorkerStatus status)
    {
        MarkupWorkerResult workerResult = new MarkupWorkerResult();
        workerResult.workerStatus = status;
        return workerResult;
    }
}
