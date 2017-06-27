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
package com.hpe.caf.worker.markup;

import com.github.cafdataprocessing.worker.markup.core.MarkupHeadersAndBody;
import com.github.cafdataprocessing.worker.markup.core.XmlParsingHelper;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class XmlParsingHelperTest
{
    private String defaultElementName = MarkupHeadersAndBody.UNREADABLE_HEADER;

    @Test
    public void shouldRemoveInvalidCharsFromXmlElementName()
    {
        assertEquals("To", XmlParsingHelper.removeInvalidXmlElementNameChars(" To{}[];:'@~#,></?+=)(*&^%$£!", defaultElementName));
    }

    @Test
    public void shouldOnlyRemoveInvalidCharsFromXmlElementName()
    {
        assertEquals("To_To-To.To", XmlParsingHelper.removeInvalidXmlElementNameChars("To_To-To.To", defaultElementName));
    }

    @Test
    public void shouldSetXmlElementNameToUnreadableHeader()
    {
        assertEquals(defaultElementName, XmlParsingHelper.removeInvalidXmlElementNameChars("!£$%^&*()", defaultElementName));
    }

    @Test
    public void shouldRemoveInvalidCharsFromXmlElementText()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("c=US;a=");
        char c = 0x0000;
        sb.append(c);
        sb.append(";p=COMPAQ;l=G2W2431-151109154418Z-19279");
        String expectedResult = "c=US;a=;p=COMPAQ;l=G2W2431-151109154418Z-19279";
        assertEquals(expectedResult, XmlParsingHelper.removeInvalidXmlElementTextChars(sb.toString()));
    }
}
