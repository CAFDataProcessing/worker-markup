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

import com.google.common.collect.Multimap;
import com.hpe.caf.util.ref.ReferencedData;

import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * The task supplied to the worker. This is the main means of communication to the worker, providing the source data.
 */
public final class MarkupWorkerTask
{
    /**
     * The source data to be marked up.
     */
    @NotNull
    public Multimap<String, ReferencedData> sourceData;

    /**
     * The hash configuration is multiple per-task.
     *
     * The hash configuration specifies the configuration for which fields to be included in the hash, the type of normalization to be
     * performed per field and the hash function to be carried out on the list of fields. Multiple hash configurations can be specified
     * and digest tags will be added to the xml markup for each hash function, including the hash value.
     */
    public List<HashConfiguration> hashConfiguration;

    /**
     * The list of the output fields to be returned.  These fields will be extracted from the XML document as specified.
     * If output fields are not specified then the entire xml will be returned in a MARKUP_XML field.
     */
    public List<OutputField> outputFields;

    /**
     * This boolean indicates if the message being passed into the Markup Worker is an email.
     */
    public boolean isEmail;
}
