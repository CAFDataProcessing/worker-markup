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
package com.github.cafdataprocessing.worker.markup.core.exceptions;

/**
 *
 * @author mcgreeva
 */
public final class MarkupWorkerExceptions extends Exception
{
    private MarkupWorkerExceptions(){}
    
    public static final String JDOMEXCEPTION_FAILURE = "MARKUP_WORKER_FAILURE_0001";
    public static final String EXECUTION_EXCEPTION = "MARKUP_WORKER_FAILURE_0002";
    public static final String ADD_HEADERS_EXCEPTION = "MARKUP_WORKER_FAILURE_0003";
    public static final String MAPPING_EXCEPTION = "MARKUP_WORKER_FAILURE_0004";
}