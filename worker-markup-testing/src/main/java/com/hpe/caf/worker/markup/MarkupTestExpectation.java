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

import com.hpe.caf.worker.testing.ContentFileTestExpectation;

import java.util.List;

public class MarkupTestExpectation extends ContentFileTestExpectation
{
    private MarkupWorkerStatus status;

    private List<NameValuePair> fieldList;

    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public MarkupWorkerStatus getStatus()
    {
        return status;
    }

    /**
     * Setter for property 'status'.
     *
     * @param status Value to set for property 'status'.
     */
    public void setStatus(MarkupWorkerStatus status)
    {
        this.status = status;
    }

    public List<NameValuePair> getFieldList()
    {
        return fieldList;
    }

    public void setFieldList(List<NameValuePair> fieldList)
    {
        this.fieldList = fieldList;
    }
}
