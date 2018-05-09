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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailSplitter
{
    // The DIVIDER_GROUP_ID specifies the capturing group which contains the DIVIDER_REGEX.
    public static final int DIVIDER_GROUP_ID = 2;

    private final JepExecutor jepExec;
    private final Pattern dividerPattern;

    //Logger for logging purposes
    private static final Logger LOG = LoggerFactory.getLogger(EmailSplitter.class);

    private static final String ERR_MSG_DOCUMENT_NULL = "The document is null.";
    private static final String ERR_MSG_NO_CONTENT = "There is no content in this document to process.";

    public EmailSplitter(JepExecutor jepExec)
    {
        this.jepExec = jepExec;

        /**
         * Searches the email line for the email divider i.e. "---------- Forwarded message ----------"
         * - "(\n|^)" is the 1st capturing group which makes sure a divider line isn't matched if there is text before it i.e.
         * "jdiwaowa   ----- Forwarded message -----"
         * - "( |>)*+" is the 3rd capturing group which matches any number of spaces or email quotation markers (">" symbols).
         * - "*+-++[^-]++-++\s*+" matches the actual divider text i.e. "---- abc ----"
         * - "$" makes sure the divider is at the end of the line.
         * - This regex has a + quantifier after each of the quantifiers specified i.e. "*+" instead of just "*". This was done to reduce
         * back tracing and reduce the anchor points the regex would hold on to. This modification was performed to prevent the regex from
         * throwing StackOverflowErrors, the Jira this work relates to is SCMOD-3065.
         */
        this.dividerPattern = Pattern.compile("(\n|^)(( |>)*+-++[^-]++-++\\s*+)$");
    }

    public void generateEmailTags(Document doc) throws JDOMException, ExecutionException, InterruptedException {
        LOG.info("Starting email splitting based on document received");
        validateDocument(doc);

        for (Element e : doc.getRootElement().getChildren("CONTENT")) {
            final String stringToSplit = e.getText();
            final List<String> emailList = getSeparatedEmails(stringToSplit);

            e.removeContent();

            for (String email : emailList) {
                // We will attempt to match the regex to find "---------- Forwarded Message ---------" or similar dividers
                Matcher matcher = dividerPattern.matcher(email);
                String divider = null; // null to make a decision later.
                // If we find a match, strip out the divider from the email text.
                if (matcher.find()) {
                    divider = matcher.group(DIVIDER_GROUP_ID);//group 1 matches the divider in the regex above
                    email = email.substring(0, email.indexOf(divider));
                }

                // If the email text is empty, do not mark up an empty email with email tags.
                if (!email.isEmpty()) {
                    Element emailElement = new Element("email");
                    emailElement.setText(email);
                    e.addContent(emailElement);
                }

                // If we have found a divider then create the divider element and add to the root.
                if (divider != null) {
                    Element dividerElement = new Element("divider");
                    dividerElement.setText(divider); // add this newline which was dropped by the regex.
                    e.addContent(dividerElement);
                }
            }
        }
        LOG.info("Email Splitting completed");
    }

    private List<String> getSeparatedEmails(String emailContent) throws JDOMException, ExecutionException, InterruptedException
    {
        List<Integer> indexes = jepExec.getMessageIndexes(emailContent);
        return separatedIndividualMessages(emailContent, indexes);
    }

    /**
     * Adapted from caf/worker/boilerplate/emailsegregation/EmailSegregation.java
     *
     * Splits the given message into individual messages based on the supplied indexes
     *
     * @param emailContent - The email to be separated
     * @param indexes - The line numbers of the start of each message in the chain
     * @return - A list of the individual messages in the chain
     */
    private static List<String> separatedIndividualMessages(String emailContent, List<Integer> indexes)
    {
        LOG.debug("Attempting to split and return separated emails");

        List<String> separatedEmails = new ArrayList<>();

        //bufferpoint to allow the loop to continue from where it left off during each iteration of the newline loop.
        //Allows for faster searches in large email chains.
        int bufferPoint = 0;

        //PrevIndexValue keeps the previous value of 'i' in memory. Allows the counter to pick up from where it left off.
        int prevIndexValue = 0;

        for (int i : indexes) {

            int counter = prevIndexValue;

            for (int pos = emailContent.indexOf("\n", bufferPoint); pos != -1; pos = emailContent.indexOf("\n", pos + 1)) {
                counter++;
                if (counter == i) {
                    String s = emailContent.substring(bufferPoint, pos + 1);
                    separatedEmails.add(s);
                    bufferPoint = pos + 1;
                    prevIndexValue = i;
                    break;
                }
            }
        }

        //Map final email into list
        String s = emailContent.substring(bufferPoint, emailContent.length());
        separatedEmails.add(s);

        return separatedEmails;
    }

    /**
     * Validate that document contains the required information for email splitting
     *
     * @param doc
     * @throws JDOMException
     */
    private static void validateDocument(Document doc)
    {
        if (doc == null) {
            LOG.error("EmailSplitter: Error - '{}'", ERR_MSG_DOCUMENT_NULL);
            throw new NullPointerException(ERR_MSG_DOCUMENT_NULL);
        }

        if (doc.getRootElement().getChild("CONTENT") == null) {
            LOG.warn("EmailSplitter: Warn - '{}'", ERR_MSG_NO_CONTENT);
        }
    }
}
