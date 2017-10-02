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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderFieldNameMapper
{
    private static final Logger LOG = LoggerFactory.getLogger(HeaderFieldNameMapper.class);

    /**
     * Standardises the email header names for XML Elements against a pre-defined list of values. i.e. Elements "&lt;Recipent&gt;" and
     * "&lt;Sent To&gt;" are convert to "&lt;To&gt;"
     *
     * @param headerName String which is to be mapped to a Standardised Header Names if not already the Standard Name
     * @param emailHeaderMappings Map providing the mapping of Standard Header Names from Non-Standard Header Names
     */
    public static String standardiseHeaderName(String headerName, Map<String, List<String>> emailHeaderMappings)
    {
        if (null != emailHeaderMappings && !emailHeaderMappings.isEmpty()) {
            LOG.debug("Mapping data supplied.  Attempting to map Email header field: [{}] to Standardised Email header field",
                      headerName);
            // Iterate of Key Value pairs for the mapping
            for (final Map.Entry<String, List<String>> entry : emailHeaderMappings.entrySet()) {

                // If the header name is found within the Entry Keys return the Key
                if (headerName.equalsIgnoreCase(entry.getKey())) {
                    LOG.debug("Email header: [{}] mapped to: [{}]", headerName, entry.getKey());
                    return entry.getKey();
                } else {
                    // Else search for the header name in the Entry's Value List<String>,
                    // if found, return the value
                    for (String name : entry.getValue()) {
                        if (headerName.equalsIgnoreCase(name)) {
                            LOG.debug("Email header: [{}] mapped to: [{}]", headerName, entry.getKey());
                            return entry.getKey();
                        }
                    }
                }
            }
        }

        LOG.debug("No mapping data supplied. Email header field remains: [{}]", headerName);
        return headerName;
    }
}
