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

import java.util.List;

/**
 * The hash configuration determining the fields to include in the hash and the scope of hash functions to include.
 */
public class HashConfiguration
{
    /*
     * Name of the hash to be included.
     */
    public String name;

    /*
     * Where the hash should be preformed.
     */
    public Scope scope;

    /**
     * List of fields to be included in the hash.
     */
    public List<Field> fields;

    /**
     * A list of hash functions to use (currently only supported function is xxHash64).
     */
    public List<HashFunction> hashFunctions;
}
