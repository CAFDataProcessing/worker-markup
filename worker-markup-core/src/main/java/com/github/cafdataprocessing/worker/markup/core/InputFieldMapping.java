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

/**
 * Describes how an input field should be mapped during markup
 */
public class InputFieldMapping
{
    /**
     * Field on input data that should be mapped to another value,.
     */
    public String inputField;
    /**
     * Field name to use for input while performing internal markup logic.
     */
    public String mapToField;
    /**
     * Identifies optional transformation function to apply to input value.
     */
    public Transforms transform = null;

    /**
     * Known transforms for input fields.
     */
    public enum Transforms
    {
        /**
         * Convert epoch seconds to ISO-8601 date format.
         */
        epochSecondsToISO8601
    }
}
