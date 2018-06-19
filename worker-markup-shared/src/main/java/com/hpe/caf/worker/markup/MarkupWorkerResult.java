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

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The result class of the worker.
 */
public final class MarkupWorkerResult
{
    /**
     * Worker specific return code.
     */
    @NotNull
    public MarkupWorkerStatus workerStatus;

    /**
     * The fields retrieved by the xml parsing configuration passed to the worker.
     */
    @NotNull
    public List<NameValuePair> fieldList;
}
