/*
 * Copyright 2017-2024 Open Text.
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
 * Represents a field to be returned by the Markup Worker.
 * The field is extracted using the specified XPath expression.
 */
public class OutputField
{
    /**
     * Name of document field to be returned
     */
    public String field;

    /**
     * XPath expression needed to extract the field value from the XML document
     */
    public String xPathExpression;
}
