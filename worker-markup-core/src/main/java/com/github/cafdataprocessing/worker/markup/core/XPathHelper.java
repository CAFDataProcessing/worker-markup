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
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for performing XPath expressions on a JDOM2 document object.
 */
public final class XPathHelper
{
    // Logger for logging purposes.
    private static final Logger LOG = LoggerFactory.getLogger(XPathHelper.class);

    private static final String ERR_MSG_DOCUMENT_NULL = "The document is null.";
    private static final String ERR_MSG_NO_XML_PARSING_CONFIGURATION = "No XML Parsing Configuration has been specified. Defaulting to return the entire XML document.";
    private static final String ERR_MSG_NO_FIELDS_SPECIFIED = "No XML parsing fields name have been specified in the configuration. Defaulting to return the entire XML document.";
    private static final String ERR_MSG_NO_FIELD_NAME_SPECIFIED = "No field name have been specified.";
    private static final String ERR_MSG_NO_EXPRESSION_SPECIFIED = "No expression has been specified for the field: ";
    private static final String ERR_MSG_UNKNOWN_EXPRESSION_TYPE = "Behaviour subject to change for this expression type";
    public static final String DEFAULT_FIELD_NAME = "MARKUPWORKER_XML";
    public static final String DEFAULT_FIELD_EXPRESSION = ".";

    private XPathHelper()
    {
    }

    /**
     * Create a HashMap containing the metadata field names and values to be returned in the MarkupWorkerResult.
     *
     * @param doc
     * @param outputFieldList
     * @return pairs A List of metadata field names and values
     */
    public static List<NameValuePair> processDocumentWithXPathExpressions(Document doc, List<OutputField> outputFieldList)
    {
        if (outputFieldList == null) {
            LOG.warn("ProcessDocumentWithXPathExpressions: Warning - '{}'", ERR_MSG_NO_XML_PARSING_CONFIGURATION);
            outputFieldList = new ArrayList<>();
        }

        validateInputs(doc, outputFieldList);

        final XPathFactory xPathFactory = XPathFactory.instance();
        List<NameValuePair> pairs = new ArrayList<>();

        for (OutputField f : outputFieldList) {
            // Get the String in the XML found by the xPathExpression
            pairs.addAll(getValuesByXPath(f, xPathFactory, doc));
        }
        return pairs;
    }

    /**
     * Get a list of key-value Pairs for the current OutputField, where the key is the field name in the OutputField object and the value
     * is the value or XML of the element retrieved by the XPath expression.
     * <p>
     * For the case where the expression is "." and a Document object is returned, we return the XML of the entire document.
     *
     * @param outputField
     * @param xPathFactory
     * @param doc
     * @return
     */
    private static List<NameValuePair> getValuesByXPath(OutputField outputField, XPathFactory xPathFactory, Document doc)
    {
        // List of Strings as an expression could return multiple values.
        List<NameValuePair> values = new ArrayList<>();

        final String xPathString = getXPathString(outputField);
        XPathExpression<Object> xPathExpression = xPathFactory.compile(xPathString);

        // evaluate the expression against the document, which returns a list of objects
        List<Object> objList = xPathExpression.evaluate(doc);

        // Iterate through the objects in the list and get the value (or XML in the case of a document) and add to the key value pairs
        for (Object o : objList) {
            if (o instanceof Attribute) {
                Attribute a = (Attribute) o;
                values.add(constructPair(outputField.field, a.getValue()));
            } else if (o instanceof Content) {
                Content c = (Content) o;
                values.add(constructPair(outputField.field, c.getValue()));
            } else if (o instanceof Document) {
                Document d = (Document) o;
                XMLOutputter outputter = new XMLOutputter();
                values.add(constructPair(outputField.field, outputter.outputString(d)));
            } else {
                LOG.warn("ProcessDocumentWithXPathExpressions: Warning - '{}'", ERR_MSG_UNKNOWN_EXPRESSION_TYPE);
                values.add(constructPair(outputField.field, o.toString()));
            }
        }

        return values;
    }

    /**
     * Validate the inputs.
     *
     * @param document
     * @param outputFieldList
     */
    private static void validateInputs(Document document, List<OutputField> outputFieldList)
    {
        // Validation of hash configuration before we enter recursive search loop
        if (document == null) {
            LOG.error("ProcessDocumentWithXPathExpressions: Error - '{}'", ERR_MSG_DOCUMENT_NULL);
            throw new IllegalArgumentException(ERR_MSG_DOCUMENT_NULL);
        }

        if (outputFieldList.size() < 1) {
            LOG.warn("ProcessDocumentWithXPathExpressions: Warning - '{}'", ERR_MSG_NO_FIELDS_SPECIFIED);
            createDefaultOutputField(outputFieldList);
        }

        for (OutputField f : outputFieldList) {
            if (f.field == null) {
                LOG.warn("ProcessDocumentWithXPathExpressions: Warning - '{}'", ERR_MSG_NO_FIELD_NAME_SPECIFIED);
            }
            if (f.xPathExpression == null) {
                LOG.warn("ProcessDocumentWithXPathExpressions: Warning - '{}'", ERR_MSG_NO_EXPRESSION_SPECIFIED + f.field);
                f.xPathExpression = DEFAULT_FIELD_EXPRESSION;
            }
        }
    }

    /**
     * Create a default OutputField which will have the default field name and the default field expression, which will return the full
     * XML of the document.
     *
     * @param outputFieldList
     */
    private static void createDefaultOutputField(List<OutputField> outputFieldList)
    {
        // Create a default OutputField which will return the entire XML
        OutputField outputField = new OutputField();
        outputField.field = DEFAULT_FIELD_NAME;
        outputField.xPathExpression = DEFAULT_FIELD_EXPRESSION;
        outputFieldList.add(outputField);
    }

    /**
     * Retrieves the XPathExpression string from the specified output field.
     * <p>
     * For backwards compatibility reasons "/root/email..." is transformed into "/root/CONTENT/email...".
     */
    private static String getXPathString(OutputField outputField)
    {
        final String xPathString = outputField.xPathExpression;

        if (xPathString.startsWith("/root/email")) {
            return "/root/CONTENT/email" + xPathString.substring("/root/email".length());
        } else {
            return xPathString;
        }
    }

    private static NameValuePair constructPair(String name, String value)
    {
        NameValuePair nameValuePair = new NameValuePair();
        nameValuePair.name = name;
        nameValuePair.value = value;
        return nameValuePair;
    }
}
