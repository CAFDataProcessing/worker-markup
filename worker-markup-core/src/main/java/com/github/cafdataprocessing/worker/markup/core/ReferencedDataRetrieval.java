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
package com.github.cafdataprocessing.worker.markup.core;

import com.hpe.caf.api.worker.TaskFailedException;
import com.hpe.caf.util.ref.DataSource;
import com.hpe.caf.util.ref.DataSourceException;
import com.hpe.caf.util.ref.ReferencedData;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to retrieve value of referenced data.
 */
public class ReferencedDataRetrieval
{
    private static final Logger LOG = LoggerFactory.getLogger(ReferencedDataRetrieval.class);

    /**
     * Returns value of ReferenceData as a String.
     * @param dataSource for use in retrieving data stored for {@code referencedData}
     * @param referencedData the ReferencedData to retrieve value for
     * @return string representation of the value for provided {@code referencedData}
     */
    public static String getContentAsStringEx(final DataSource dataSource, final ReferencedData referencedData)
    {
        try {
            return getContentAsString(dataSource, referencedData);
        } catch (DataSourceException dse) {
            throw new TaskFailedException("Failed to retrieve content from storage", dse);
        } catch (IOException ioe) {
            throw new TaskFailedException("Failed to read input stream from storage", ioe);
        }
    }

    private static String getContentAsString(DataSource dataSource, final ReferencedData referencedData)
            throws DataSourceException, IOException
    {
        try(InputStream dataStream = referencedData.acquire(dataSource))
        {
            final byte[] refDataBytes = IOUtils.toByteArray(dataStream);
            try {
                // Get content using UTF-8
                return IOUtils.toString(refDataBytes, "UTF-8");
            } catch (Exception e) {
                // Catch and retry with 1252 encoding.
                LOG.error("Failed to convert to utf8", e);
                return IOUtils.toString(refDataBytes, "Windows-1252");
            }
        }
    }
}
