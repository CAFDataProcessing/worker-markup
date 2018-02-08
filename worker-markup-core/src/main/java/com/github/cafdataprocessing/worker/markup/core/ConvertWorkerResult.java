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

import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.markup.MarkupWorkerResult;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mcgreeva
 */
public class ConvertWorkerResult
{
    private ConvertWorkerResult()
    {
    }

    public static void updateDocument(final Document document, final MarkupWorkerResult markupWorkerResult)
    {
        document.getField("MARKUPWORKER_STATUS").set(markupWorkerResult.workerStatus.toString());
        
        List<String> updatedFields = new ArrayList();
        markupWorkerResult.fieldList.forEach((entry) -> {
            String fieldName = entry.name;
            if(!updatedFields.contains(fieldName)) {
                document.getField(fieldName).clear();
                updatedFields.add(fieldName);
            }
            document.getField(fieldName).add(entry.value);
        });
    }
}
