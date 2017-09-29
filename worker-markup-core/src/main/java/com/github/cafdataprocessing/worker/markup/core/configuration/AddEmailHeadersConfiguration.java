/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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
package com.github.cafdataprocessing.worker.markup.core.configuration;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Configuration around addition of headers to a field during markup of emails.
 */
public class AddEmailHeadersConfiguration {
    /**
     * Whether headers should be added to the specified field for emails undergoing markup.
     */
    private boolean enabled;

    /**
     * The field to add headers to for emails during markup.
     */
    @NotNull
    @Size(min = 1)
    private String fieldName = "CONTENT";

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
