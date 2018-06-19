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

    public static void untagFalseEmails(final List<Element> emailElements)
    {
        for (int i = emailElements.size() - 1; i > 0; i--) {
            final Element header = (Element) emailElements.get(i).getContent().get(0);
            final Element body = (Element) emailElements.get(i).getContent().get(1);

            if (header.getContentSize() == 0 && body.getContentSize() == 0) {
                emailElements.get(i).detach();
            } else if (header.getContentSize() == 0) {
                final String currentBody = body.getText();
                final String previousBody = ((Element) emailElements.get(i - 1).getContent().get(1)).getText();
                final String newBody = previousBody + currentBody;
                ((Element) emailElements.get(i - 1).getContent().get(1)).setText(newBody);
                emailElements.get(i).detach();
            }
        }
    }
}
