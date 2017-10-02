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

import com.hpe.caf.worker.markup.NameValuePair;
import com.hpe.caf.worker.markup.OutputField;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Test class for the XPath Helper functionality.
 */
public class XPathTester
{
    @Test
    public void testXPathFieldRetrieval() throws IOException, JDOMException
    {
        // Read document from the xml resources file
        Document doc = TestUtility.readXmlFile("src/test/resources/xml/XPathTestFile.xml");

        // The expected map will have the expected values and will be used for assertions.
        HashMap<String, List<String>> expectedMap = new HashMap<>();
        List<OutputField> outputFieldList = createOutputFields(expectedMap, doc);

        // Call the processDocumentWithXPathExpressions
        List<NameValuePair> pairs = XPathHelper.processDocumentWithXPathExpressions(doc, outputFieldList);

        // Read all pairs into a HashMap. Note, as there can be multiple values per key, the values will be lists of Strings per key.
        HashMap<String, List<String>> resultsAsMap = new HashMap<>();
        for (NameValuePair p : pairs) {
            addToMap(resultsAsMap, p.name, p.value);
        }

        // Cycle through the output fields, and for each result pair, make sure the list of Strings for each value is as expected.
        for (OutputField f : outputFieldList) {
            Assert.assertTrue("Resultant hash map should contain the key " + f.field, resultsAsMap.containsKey(f.field));
            for (int i = 0; i < resultsAsMap.get(f.field).size(); i++) {
                Assert.assertEquals("Resultant hash map value should be the same as expected for the key " + f.field,
                                    expectedMap.get(f.field).get(i),
                                    resultsAsMap.get(f.field).get(i));
            }
        }
    }

    @Test
    public void testNullOutputFieldsUsesDefaultOutputField() throws IOException, JDOMException
    {
        Document doc = TestUtility.readXmlFile("src/test/resources/xml/XPathTestFile.xml");
        XMLOutputter outputter = new XMLOutputter();

        List<NameValuePair> pairs = XPathHelper.processDocumentWithXPathExpressions(doc, null);

        HashMap<String, List<String>> resultsAsMap = new HashMap<>();
        for (NameValuePair p : pairs) {
            addToMap(resultsAsMap, p.name, p.value);
        }

        Assert.assertTrue("Map should contain the default field name: " + XPathHelper.DEFAULT_FIELD_NAME,
                          resultsAsMap.containsKey(XPathHelper.DEFAULT_FIELD_NAME));

        Assert.assertTrue("Map's default field should be a list of size 1. ", resultsAsMap.get(XPathHelper.DEFAULT_FIELD_NAME).size() == 1);

        Assert.assertEquals("Default field: " + XPathHelper.DEFAULT_FIELD_NAME + " content should match the expected content. ",
                            resultsAsMap.get(XPathHelper.DEFAULT_FIELD_NAME).get(0), outputter.outputString(doc));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailureNullDocument() throws JDOMException
    {
        XPathHelper.processDocumentWithXPathExpressions(null, null);
    }

    private static void addToMap(HashMap<String, List<String>> map, String key, String value)
    {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            map.put(key, new ArrayList<>());
            map.get(key).add(value);
        }
    }

    private static List<OutputField> createOutputFields(HashMap<String, List<String>> expectedMap, Document doc)
    {
        XMLOutputter outputter = new XMLOutputter();
        List<OutputField> outputFieldList = new ArrayList<>();

        OutputField f1 = new OutputField();
        f1.field = "SECTION_ID";
        f1.xPathExpression = "/root/email[1]/hash/digest/@value";
        addToMap(expectedMap, f1.field, "a4e7fa8100f5f6fb");
        outputFieldList.add(f1);

        OutputField f2 = new OutputField();
        f2.field = "SECTION_SORT";
        f2.xPathExpression = "/root/email[1]/headers/Sent/text()";
        addToMap(expectedMap, f2.field, "Sent: 20 July 2016 17:51");
        outputFieldList.add(f2);

        OutputField f3 = new OutputField();
        f3.field = "PARENT_ID";
        f3.xPathExpression = "/root/email[2]/hash/digest/@value";
        addToMap(expectedMap, f3.field, "f5aaaeca8206aa23");
        outputFieldList.add(f3);

        OutputField f4 = new OutputField();
        f4.field = "ROOT_ID";
        f4.xPathExpression = "/root/email[last()]/hash/digest/@value";
        addToMap(expectedMap, f4.field, "6c86d7d70c3d14f8");
        outputFieldList.add(f4);

        OutputField f5 = new OutputField();
        f5.field = "CONVERSATION_INDEX_JSON";
        f5.xPathExpression = "/root/CAF_MAIL_CONVERSATION_INDEX_PARSED/text()";
        addToMap(expectedMap, f5.field, "{\"headerDate\":\"2013-01-02T17:01:04.168Z\",\"guid\":\"d78f0e42-8082-4120-b2f1-d0e3c07ed007\",\"childMessages\":[{\"messageDate\":\"2013-01-02T17:23:58.064Z\",\"randomNo\":3,\"sequenceCount\":0},{\"messageDate\":\"2013-01-02T17:25:53.931Z\",\"randomNo\":6,\"sequenceCount\":0}]}");
        outputFieldList.add(f5);

        OutputField f6 = new OutputField();
        f6.field = "ATTEMPT_TO_GET_ALL_XML";
        f6.xPathExpression = ".";
        addToMap(expectedMap, f6.field, outputter.outputString(doc));
        outputFieldList.add(f6);

        OutputField f10 = new OutputField();
        f10.field = "MULTIPLE_TO_VALUES";
        f10.xPathExpression = "//To";
        addToMap(expectedMap, f10.field, "To: Reid, Andy &lt;andrew.reid@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;");
        addToMap(expectedMap, f10.field, "To: Reid, Andy &lt;andrew.reid@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;");
        addToMap(expectedMap, f10.field, "To: Paul, Navamoni &lt;paul.navamoni@hpe.com&gt;; Hardy, Dermot &lt;dermot.hardy@hpe.com&gt;");
        outputFieldList.add(f10);

        return outputFieldList;
    }
}
