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
     * @param parentElement The element from which the false emails are to be untagged.It also includes dividers
     */
    public static void untagFalseEmails(final Element parentElement)
    {
        final List<Element> emailElements = parentElement.getChildren();

        // The loop ignores the 0th element as we don't have it's header information
        for (int i = emailElements.size() - 1; i > 0; i--) {
            final Element previousElement = emailElements.get(i - 1);
            final Element currentElement = emailElements.get(i);
            if (currentElement.getName().equals("email")) {
                final Element headersElement = (Element) currentElement.getContent().get(0);
                if (headersElement.getContentSize() == 0) {
                    final Element bodyElement = (Element) currentElement.getContent().get(1);
                    final int bodyElementContentSize = bodyElement.getContentSize();
                    if (previousElement.getName().equals("divider")) {
                        if (bodyElementContentSize == 0
                            || (bodyElementContentSize == 1 && bodyElement.getContent().get(0).getValue().isEmpty())) {
                            currentElement.detach();
                        }
                    } else {
                        if (bodyElementContentSize > 0) {
                            final Element previousBodyElement = (Element) previousElement.getContent().get(1);
                            final String currentBodyText = bodyElement.getText();
                            final String previousBodyText = previousBodyElement.getText();
                            final String newBodyText = previousBodyText + currentBodyText;
                            previousBodyElement.setText(newBodyText);
                        }
                        currentElement.detach();
                    }
                }
            }
        }
    }
}
