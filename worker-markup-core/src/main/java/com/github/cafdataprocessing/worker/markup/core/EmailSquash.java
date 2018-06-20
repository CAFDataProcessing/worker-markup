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

import org.jdom2.Element;

import java.util.List;

public final class EmailSquash
{
    private EmailSquash()
    {
    }

    /**
     * Untags emails with blank headers except the parent email.
     *
     * @param emailElements The number of emails from which the false ones are to untagged
     */
    public static void untagFalseEmails(final List<Element> emailElements)
    {
        // The loop starts with 1 because the we do not have header information of the parent email
        for (int i = emailElements.size() - 1; i > 0; i--) {

            final Element emailElement = emailElements.get(i);
            final Element headersElement = (Element) emailElement.getContent().get(0);

            // If the header of an email is null and has a body it will untag that email and attach the body of the untagged email to the
            // previous email. If the header and body both are null it will directly untag that email.
            if (headersElement.getContentSize() == 0) {
                final Element bodyElement = (Element) emailElement.getContent().get(1);
                if (bodyElement.getContentSize() > 0) {
                    final Element previousEmailElement = emailElements.get(i - 1);
                    final Element previousBodyElement = (Element) previousEmailElement.getContent().get(1);
                    final String currentBodyText = bodyElement.getText();
                    final String previousBodyText = previousBodyElement.getText();
                    final String newBodyText = previousBodyText + currentBodyText;
                    previousBodyElement.setText(newBodyText);
                }

                emailElement.detach();
            }
        }
    }
}
