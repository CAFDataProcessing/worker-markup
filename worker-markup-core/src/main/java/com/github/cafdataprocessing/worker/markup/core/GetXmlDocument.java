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

import com.github.cafdataprocessing.worker.markup.core.XmlConverter;
import com.github.cafdataprocessing.worker.markup.core.XmlFieldEntry;
import java.util.List;
import org.jdom2.Document;

/**
 *
 * @author mcgreeva
 */
public class GetXmlDocument
{
    private GetXmlDocument(){}
    public static Document getXmlDocument(final List<XmlFieldEntry> xmlFieldEntries){
         return XmlConverter.createXmlDocument(xmlFieldEntries);   
    }

}
