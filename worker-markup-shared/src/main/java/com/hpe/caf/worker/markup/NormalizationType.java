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

/**
 * The method of normalization to be performed on the field content before being added to the hash.
 */
public enum NormalizationType
{
    /**
     * Content will be added to the hash as-is. No normalization will be performed.
     */
    NONE,

    /**
     * Removes all whitespace and new lines.
     */
    REMOVE_WHITESPACE,

    /**
     * Removes quotation marks and email links in headers.
     */
    NAME_ONLY,

    /**
     * Removes whitespace and links from body text.
     */
    REMOVE_WHITESPACE_AND_LINKS,

    /**
     * Normalizes email priority values to a standard form.
     */
    NORMALIZE_PRIORITY,

    /**
     * Normalizes case of characters.
     */
    NORMALIZE_CASE
}
