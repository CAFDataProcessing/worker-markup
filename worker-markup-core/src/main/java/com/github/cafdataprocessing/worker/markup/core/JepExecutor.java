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
package com.github.cafdataprocessing.worker.markup.core;

import com.hpe.caf.worker.emailsegregation.ContentSegregation;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JepExecutor
{
    //Logger for logging purposes
    private static final Logger LOG = LoggerFactory.getLogger(JepExecutor.class);

    private final ExecutorService jepThreadPool;

    public JepExecutor(ExecutorService jepThreadPool)
    {
        this.jepThreadPool = jepThreadPool;
    }

    public List<Integer> getMessageIndexes(String emailContent) throws JDOMException, ExecutionException, InterruptedException
    {
        LOG.debug("Attempting to call python script 'split_email'");

        Callable<List<Integer>> callPython = () -> ContentSegregation.splitEmail(emailContent);
        Future<List<Integer>> futureResult = jepThreadPool.submit(callPython);
        List<Integer> indexes = futureResult.get();

        return indexes;
    }
}
