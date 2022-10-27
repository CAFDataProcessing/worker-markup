/*
 * Copyright 2017-2022 Micro Focus or one of its affiliates.
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

import org.junit.Assert;
import org.junit.Test;

public class TalonEmailSplitterTest
{
    @Test
    public void testSplitEmails()
    {
        final String msgBody = "From: Mr. X\n"
            + "    Date: 24 February 2016\n"
            + "    To: Mr. Y\n"
            + "    Subject: Hi\n"
            + "    Attachments: none\n"
            + "    Goodbye.\n"
            + "    From: Mr. Y\n"
            + "    To: Mr. X\n"
            + "    Date: 24 February 2016\n"
            + "    Subject: Hi\n"
            + "    Attachments: none\n\n"
            + "    Hello.\n\n"
            + "        On 24th February 2016 at 09.32am, Conal wrote:\n\n"
            + "        Hey!\n\n"
            + "        On Mon, 2016-10-03 at 09:45 -0600, Stangel, Dan wrote:\n"
            + "        > Mohan,\n"
            + "        >\n"
            + "        > We have not yet migrated the systems.\n"
            + "        >\n"
            + "        > Dan\n"
            + "        >\n"
            + "        > > -----Original Message-----\n"
            + "        > > Date: Mon, 2 Apr 2012 17:44:22 +0400\n"
            + "        > > Subject: Test\n"
            + "        > > From: bob@xxx.mailgun.org\n"
            + "        > > To: xxx@gmail.com; xxx@hotmail.com; xxx@yahoo.com; xxx@aol.com; xxx@comcast.net; xxx@nyc.rr.com\n"
            + "        > >\n"
            + "        > > Hi\n"
            + "        > >\n"
            + "        > > > From: bob@xxx.mailgun.org\n"
            + "        > > > To: xxx@gmail.com; xxx@hotmail.com; xxx@yahoo.com; xxx@aol.com; xxx@comcast.net; xxx@nyc.rr.com\n"
            + "        > > > Date: Mon, 2 Apr 2012 17:44:22 +0400\n"
            + "        > > > Subject: Test\n"
            + "        > > > Hi\n"
            + "        > > >\n"
            + "        > >\n"
            + "        >\n"
            + "        >";

        final String expectedMarkers = "stttttsttttetesetesmmmmmmssmmmmmmsmmmmmmmm";

        final String actualMarkers = TalonEmailSplitter.splitEmails(msgBody);

        Assert.assertEquals(expectedMarkers, actualMarkers);
    }
}
