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
package com.hpe.caf.worker.markup;

import com.github.cafdataprocessing.worker.markup.core.EmailSplitter;
import com.github.cafdataprocessing.worker.markup.core.MarkupDocumentEngine;
import com.github.cafdataprocessing.worker.markup.core.MarkupWorkerConfiguration;
import com.github.cafdataprocessing.worker.markup.core.exceptions.AddHeadersException;
import com.github.cafdataprocessing.worker.markup.core.exceptions.MappingException;
import com.hpe.caf.api.Codec;
import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.worker.*;
import com.hpe.caf.worker.AbstractWorker;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * processing the Hash map key pairs and placing the, into XML.
 */
public class MarkupWorker extends AbstractWorker<MarkupWorkerTask, MarkupWorkerResult>
{
    private final MarkupWorkerConfiguration config;
    private final MarkupDocumentEngine markupDocument;

    /**
     * Logger for logging purposes.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MarkupWorker.class);
    private final DataStore dataStore;
    private final EmailSplitter emailSplitter;

    public MarkupWorker(final MarkupWorkerTask task,
                        final DataStore dataStore,
                        final String outputQueue,
                        final Codec codec,
                        final MarkupWorkerConfiguration config,
                        final MarkupDocumentEngine markupDocument,
                        final EmailSplitter emailSplitter)
        throws InvalidTaskException
    {
        super(task, outputQueue, codec);
        this.dataStore = Objects.requireNonNull(dataStore);
        this.config = config;
        this.markupDocument = markupDocument;
        this.emailSplitter = emailSplitter;
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
        try {
            return markupDocument.markupDocument(getTask(), dataStore, getCodec(), config, emailSplitter);
        } catch (JDOMException jdome) {
            LOG.error("Error during JDOM parsing. ", jdome);
            return createErrorResult(MarkupWorkerStatus.WORKER_FAILED);
        } catch (ExecutionException ee) {
            LOG.error("Error during splitting of emails. ", ee);
            return createErrorResult(MarkupWorkerStatus.WORKER_FAILED);
        } catch (ConfigurationException ex) {
            LOG.error("Error during retrieval of configuration. ", ex);
            return createErrorResult(MarkupWorkerStatus.WORKER_FAILED);
        } catch (AddHeadersException ex) {
            LOG.error("Error adding headers to email. ", ex);
            return createErrorResult(MarkupWorkerStatus.WORKER_FAILED);
        } catch (MappingException ex) {
            LOG.error("Error mapping input fields for markup. ", ex);
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
