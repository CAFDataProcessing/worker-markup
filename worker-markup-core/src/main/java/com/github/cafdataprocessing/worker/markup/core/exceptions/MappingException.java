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
package com.github.cafdataprocessing.worker.markup.core.exceptions;

/**
 * Exception to be thrown if an error occurs during mapping of input fields.
 */
public class MappingException extends Exception {
    /**
     * Create a new MappingException
     *
     * @param message information about this exception
     */
    public MappingException(final String message)
    {
        super(message);
    }

    /**
     * Create a new MappingException
     *
     * @param message information about this exception
     * @param cause the original cause of this exception
     */
    public MappingException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
