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
package com.github.cafdataprocessing.worker.markup.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * Utility class for functions used during unit testing
 */
public final class TestUtility
{
    private TestUtility()
    {
    }

    /*
     * Read the specified XML file 
     */
    public static Document readXmlFile(String location) throws IOException, JDOMException
    {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = null;
        try {
            document = saxBuilder.build(new File(location));
        } catch (JDOMException ex) {
            Logger.getLogger(TestUtility.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        return document;
    }

    /*
     * Compare element header names and element header text
     */
    public static boolean compareHeaderElements(Document doc1, Document doc2)
    {
        List<Element> list1 = doc1.getRootElement().getChild("CONTENT").getChild("email").getChild("headers").getChildren();
        List<Element> list2 = doc2.getRootElement().getChild("CONTENT").getChild("email").getChild("headers").getChildren();
        List<String> list1a = new ArrayList<>();
        List<String> list2a = new ArrayList<>();
        for (Element e1 : list1) {
            list1a.add(e1.getName());
            list1a.add(e1.getText());
        }
        for (Element e2 : list2) {
            list2a.add(e2.getName());
            list2a.add(e2.getText());
        }
        if (list1a.containsAll(list2a)) {
            return true;
        }
        return false;
    }
}
