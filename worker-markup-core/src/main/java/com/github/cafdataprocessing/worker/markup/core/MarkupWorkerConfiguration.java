/*
 * Copyright 2017-2023 Open Text.
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

import com.hpe.caf.api.Configuration;
import com.hpe.caf.api.worker.WorkerConfiguration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * Configuration for the MarkupWorker, read in from test-configs/cfg_test_worker-markup-MarkupWorkerConfiguration.
 */
public class MarkupWorkerConfiguration extends WorkerConfiguration
{
    /**
     * Whether headers for emails should be added to the start of a specified field
     */
    private boolean addEmailHeadersDuringMarkup;

    /**
     * Output queue to return results to RabbitMQ.
     */
    @NotNull
    @Size(min = 1)
    private String outputQueue;

    /**
     * Number of threads to use in the worker.
     */
    @Min(1)
    @Max(20)
    private int threads;

    /**
     * A map of source to target field names.
     * If provided, Markup Worker will use it to internally map (rename) input field names to the provided values.
     */
    @Configuration
    private List<InputFieldMapping> inputFieldMappings;

    /**
     * A map of field names to be mapped to a standardised field name.
     */
    private Map<String, List<String>> emailHeaderMappings;

    /**
     * A multilingual word mapping used in a regular expression to identify condensed email headers.
     *
     * Condensed headers are of the form "On date, name wrote:". The mappings provide multilingual support for this condensed header,
     * providing the "On", "wrote" and date-name separator in different languages.
     */
    private Map<String, List<String>> condensedHeaderMultiLangMappings;

    public MarkupWorkerConfiguration()
    {
    }

    public boolean shouldAddEmailHeadersDuringMarkup()
    {
        return addEmailHeadersDuringMarkup;
    }

    public void setAddEmailHeadersDuringMarkup(boolean addEmailHeadersDuringMarkup)
    {
        this.addEmailHeadersDuringMarkup = addEmailHeadersDuringMarkup;
    }

    public String getOutputQueue()
    {
        return outputQueue;
    }

    public void setOutputQueue(String outputQueue)
    {
        this.outputQueue = outputQueue;
    }

    public int getThreads()
    {
        return threads;
    }

    public void setThreads(int threads)
    {
        this.threads = threads;
    }

    public Map<String, List<String>> getEmailHeaderMappings()
    {
        return emailHeaderMappings;
    }

    public void setEmailHeaderMappings(Map<String, List<String>> emailHeaderMappings)
    {
        this.emailHeaderMappings = emailHeaderMappings;
    }

    public Map<String, List<String>> getCondensedHeaderMultiLangMappings()
    {
        return condensedHeaderMultiLangMappings;
    }

    public void setCondensedHeaderMultiLangMappings(Map<String, List<String>> condensedHeaderMultiLangMappings)
    {
        this.condensedHeaderMultiLangMappings = condensedHeaderMultiLangMappings;
    }

    /**
     * Getter for property 'inputFieldMappings'.
     *
     * @return Value for property 'inputFieldMappings'.
     */
    public List<InputFieldMapping> getInputFieldMappings()
    {
        return inputFieldMappings;
    }

    /**
     * Setter for property 'inputFieldMappings'.
     *
     * @param inputFieldMappings Value to set for property 'inputFieldMappings'.
     */
    public void setInputFieldMappings(List<InputFieldMapping> inputFieldMappings)
    {
        this.inputFieldMappings = inputFieldMappings;
    }
}
