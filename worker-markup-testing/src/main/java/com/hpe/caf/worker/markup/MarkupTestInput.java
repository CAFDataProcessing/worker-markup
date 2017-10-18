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
import com.hpe.caf.worker.testing.FileTestInputData;

import java.util.List;

public class MarkupTestInput extends FileTestInputData
{
    private Multimap<String, String> sourceData;

    private List<HashConfiguration> hashConfiguration;

    private List<OutputField> outputFieldList;

    private boolean isEmail;

    private String contentFieldName;

    public Multimap<String, String> getSourceData()
    {
        return sourceData;
    }

    public void setSourceData(Multimap<String, String> sourceData)
    {
        this.sourceData = sourceData;
    }

    public List<HashConfiguration> getHashConfiguration()
    {
        return hashConfiguration;
    }

    public void setHashConfiguration(List<HashConfiguration> hashConfiguration)
    {
        this.hashConfiguration = hashConfiguration;
    }

    public List<OutputField> getOutputFieldList()
    {
        return outputFieldList;
    }

    public void setOutputFieldList(List<OutputField> outputFieldList)
    {
        this.outputFieldList = outputFieldList;
    }

    public boolean isEmail()
    {
        return isEmail;
    }

    public void setEmail(boolean isEmail)
    {
        this.isEmail = isEmail;
    }

    /**
     * Getter for property 'contentFieldName'.
     *
     * @return Value for property 'contentFieldName'.
     */
    public String getContentFieldName()
    {
        return contentFieldName;
    }

    /**
     * Setter for property 'contentFieldName'.
     *
     * @param contentFieldName Value to set for property 'contentFieldName'.
     */
    public void setContentFieldName(String contentFieldName)
    {
        this.contentFieldName = contentFieldName;
    }
}
