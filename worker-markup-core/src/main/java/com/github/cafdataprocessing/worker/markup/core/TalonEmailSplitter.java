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

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import edu.emory.mathcs.backport.java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Java implementation of <a href="https://github.com/mailgun/talon/commits/master/talon/quotations.py">quotations.py</a> from the Talon
 * library.
 *
 * This implementation was created from the master branch of Talon on 25 October 2022.
 *
 * Due to this <a href="https://github.com/mailgun/talon/commit/8138ea9a604f037f47f544dfb805f13d26696275">commit</a> changing some
 * behaviour that caused some of our integration tests to fail (see the
 * <a href="https://internal.almoctane.com/ui/entity-navigation?p=131002/6001&entityType=work_item&id=477072">comments on Octane ticket
 * 477072</a> for details), we are using the <a href="https://github.com/mailgun/talon/blob/v1.4.6/talon/quotations.py#L142">1.4.6</a>
 * version of the RE_FROM_COLON_OR_DATE_COLON regex.
 *
 * If updating this class, note the following <a href="https://stackoverflow.com/a/10492382/12177456">differences</a> between Java regex
 * and Python regex:
 *
 * - Java's matcher.matches() (also Pattern.matches( regex, input )) matches the entire string. It has no direct equivalent in Python. -
 * The same result can be achieved by using re.match( regex, input ) with a regex that ends with $. - Java's matcher.find() and Python's
 * re.search( regex, input ) match any part of the string. - Java's matcher.lookingAt() and Python's re.match( regex, input ) match the
 * beginning of the string.
 */
public final class TalonEmailSplitter
{
    private TalonEmailSplitter()
    {
    }

    private final static Pattern RE_FWD = Pattern.compile(
        "^[-]+[ ]*Forwarded message[ ]*[-]+\\s*$",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private final static Pattern RE_ON_DATE_SMB_WROTE = Pattern.compile(
        "(-*[>]?[ ]?(On|Le|W dniu|Op|Am|Em|På|Den|Vào)[ ].*(,|użytkownik)(.*\\n){0,2}.*(wrote|sent|a écrit|napisał|schreef|verzond|geschreven|schrieb|escreveu|skrev|đã viết):?-*)");

    // Special case for languages where text is translated like this: 'on {date} wrote {somebody}:'
    private final static Pattern RE_ON_DATE_WROTE_SMB = Pattern.compile(
        "(-*[>]?[ ]?(Op|Am)[ ].*(.*\\n){0,2}.*(schreef|verzond|geschreven|schrieb)[ ]*.*:)");

    // ------Original Message------ or ---- Reply Message ----
    // With variations in other languages.
    private final static Pattern RE_ORIGINAL_MESSAGE = Pattern.compile(
        "[\\\\s]*[-]+[ ]*(Original Message|Reply Message|Ursprüngliche Nachricht|Antwort Nachricht|Oprindelig meddelelse)[ ]*[-]+",
        Pattern.CASE_INSENSITIVE);

    private final static Pattern RE_FROM_COLON_OR_DATE_COLON = Pattern.compile(
        "(_+\\r?\\n)?[\\\\s]*(:?[*]?From|Van|De|Von|Fra|Från|Date|Datum|Envoyé|Skickat|Sendt)[\\\\s]?:[*]?.*",
        Pattern.CASE_INSENSITIVE);

    // ---- John Smith wrote ----
    private final static Pattern RE_ANDROID_WROTE = Pattern.compile(
        "[\\\\s]*[-]+.*(wrote)[ ]*[-]+",
        Pattern.CASE_INSENSITIVE);

    // Support polymail.io reply format
    // On Tue, Apr 11, 2017 at 10:07 PM John Smith
    //
    // <
    // mailto:John Smith <johnsmith@gmail.com>
    // > wrote:
    private final static Pattern RE_POLYMAIL = Pattern.compile(
        "On.*\\s{2}<\\smailto:.*\\s> wrote:",
        Pattern.CASE_INSENSITIVE);

    private final static List<Pattern> SPLITTER_PATTERNS = Lists.newArrayList(
        RE_ORIGINAL_MESSAGE,
        RE_ON_DATE_SMB_WROTE,
        RE_ON_DATE_WROTE_SMB,
        RE_FROM_COLON_OR_DATE_COLON,
        // 02.04.2012 14:20 пользователь "bob@example.com" <
        // bob@xxx.mailgun.org> написал:
        Pattern.compile("(\\d+/\\d+/\\d+|\\d+\\.\\d+\\.\\d+).*\\s\\S+@\\S+", Pattern.DOTALL),
        // 2014-10-17 11:28 GMT+03:00 Bob <
        // bob@example.com>:
        Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}\\s+GMT.*\\s\\S+@\\S+", Pattern.DOTALL),
        // Thu, 26 Jun 2014 14:00:51 +0400 Bob <bob@example.com>:
        Pattern.compile("\\S{3,10}, \\d\\d? \\S{3,10} 20\\d\\d,? \\d\\d?:\\d\\d(:\\d\\d)?( \\S+){3,6}@\\S+:"),
        // Sent from Samsung MobileName <address@example.com> wrote:
        Pattern.compile("Sent from Samsung.* \\S+@\\S+> wrote"),
        RE_ANDROID_WROTE,
        RE_POLYMAIL
    );

    private final static int SPLITTER_MAX_LINES = 6;

    private final static int MAX_LINES_COUNT = 1000;

    private final static Pattern RE_LINK = Pattern.compile(
        "<(http://[^>]*)>");

    private final static Pattern QUOT_PATTERN = Pattern.compile(
        "^>+ ?");

    // Regular expression to identify if a line is a header.
    private final static Pattern RE_HEADER = Pattern.compile(
        ": ");

    public static void main(String[] args)
    {
        splitEmails("From: K, Tom\n"
            + "Sent: Fri Nov 25 10:52:09 2016\n"
            + "To: D, Mohanasundaram; S, Dan\n"
            + "Cc: B, Rick (Rick); openvpn.community@lists.com\n"
            + "Subject: Re: Had to reimage my ubuntu workstation.....\n"
            + "Importance: Normal\n"
            + "\n"
            + "By chance was the \"green light\" ever provided for the move out of redacted.com to new.redacted.com domain?  Appreciate the help here.\n"
            + "\n"
            + "/tom\n"
            + "\n"
            + "--\n"
            + "Tom K\n"
            + "Strategist and Solutions\n"
            + "EG Quality Strategy and Analytics\n"
            + "\n"
            + "\n"
            + "On 10/4/16, 4:30 PM, \"openvpn.community-request@lists.com on behalf of D, Mohanasundaram\" <openvpn.community-request@lists.com on behalf of mohanasundaram.d@hpe.com> wrote:\n"
            + "\n"
            + "    Thanks Dan,\n"
            + "\n"
            + "    Hope we take action soon to move out of redacted.com to new.redacted.com domain. This\n"
            + "    may stop working on Oct 31 or some day. Who are the OpenVPM admins?\n"
            + "\n"
            + "    Thanks\n"
            + "    Mohan\n"
            + "\n"
            + "\n"
            + "\n"
            + "    On Mon, 2016-10-03 at 09:45 -0600, S, Dan wrote:\n"
            + "    > Mohan,\n"
            + "    >\n"
            + "    > We have not yet migrated the osprey[01].fc.com systems.  These can be moved at any time to osprey[01].ftc.net but I need the \"green light\" from the OpenVPN admins to do so -- earlier in the year we decided to wait.\n"
            + "    >\n"
            + "    > As for osprey[01].linux.com, these also remain intact, but there are new hosts osprey[01].osp.com that are set up in DNS.  They don't appear to respond to any ping or SSH attempts.\n"
            + "    >\n"
            + "    >\n"
            + "    > Dan S\n"
            + "    > Hewlett Packard Enterprise Open Source Program Office\n"
            + "    >\n"
            + "    > -----Original Message-----\n"
            + "    > From: openvpn.community-request@lists.com [mailto:openvpn.community-request@lists.com] On Behalf Of D, Mohanasundaram\n"
            + "    > Sent: Monday, October 03, 2016 8:25 AM\n"
            + "    > To: B, Rick (Rick) <rick.b@hpe.com>\n"
            + "    > Cc: openvpn.community@lists.com\n"
            + "    > Subject: Re: Had to reimage my ubuntu workstation.....\n"
            + "    >\n"
            + "    > Looks like we need to find the equivalents for the following hp.com servers and replace them at https://hpedia.osp.com/wiki/Open\n"
            + "    > ospra0.linux.hp.com\n"
            + "    > ospra1.linux.hp.com\n"
            + "    > http://autocache.com/\n"
            + "    > http://remoteaccess.com\n"
            + "    > intranet.com\n"
            + "    > http://itsupport.com/portal/site/documentdetail?docid=KM32\n"
            + "    > vwpc.austin.com\n"
            + "    >\n"
            + "    > I do not know the new servers and looking for them. If some one has please do replace.\n"
            + "    >\n"
            + "    > Regards\n"
            + "    > Mohan\n"
            + "    >\n"
            + "    >\n"
            + "    > On Mon, 2016-10-03 at 08:36 -0400, Rick B wrote:\n"
            + "    > > This tends to be the most up-to-date resource for information:\n"
            + "    > >\n"
            + "    > > https://hpedia.osp.com/wiki/Open\n"
            + "    > >\n"
            + "    > > If you find something different,  please update the wiki.\n"
            + "    > >\n"
            + "    > > I use both openvpn on 16.76 as well as openconnect with the juniper\n"
            + "    > > gateways:\n"
            + "    > >\n"
            + "    > > https://hpedia.osp.com/wiki/OpenConnect\n"
            + "    > >\n"
            + "    > > I find openconnect to have somewhat better latency but it does seem\n"
            + "    > > susceptible to 'noise' that causes connections to occasionally drop.\n"
            + "    > >\n"
            + "    > > Rick\n"
            + "    > >\n"
            + "    > >\n"
            + "    > >\n"
            + "    > > On 10/02/2016 05:07 PM, L, Benson wrote:\n"
            + "    > > > Mike,\n"
            + "    > > >\n"
            + "    > > >\n"
            + "    > > >\n"
            + "    > > > Which certificate are you referring to?  Is this the Class B digital\n"
            + "    > > > badge certificate or is it the SSL Certificate used for servers?\n"
            + "    > > > Both have been migrated to http://mydigi.com/ now.\n"
            + "    > > >\n"
            + "    > > >\n"
            + "    > > >\n"
            + "    > > > Thank you!\n"
            + "    > > >\n"
            + "    > > >\n"
            + "    > > >\n"
            + "    > > > Benson\n"
            + "    > > >\n"
            + "    > > >\n"
            + "    > > >\n"
            + "    > >\n"
            + "    >\n"
            + "    >");
    }

    /**
     * Given a message(which may consist of an email conversation thread with multiple emails), mark the lines to identify split lines,
     * content lines and empty lines.
     *
     * Correct the split line markers inside header blocks. Header blocks are identified by the regular expression RE_HEADER.
     *
     * @param msgBody The email's message body
     * @return The corrected markers
     */
    public static String splitEmails(final String msgBody)
    {
        // Given a message(which may consist of an email conversation thread with multiple emails), mark the lines to identify split
        // lines content lines and empty lines.
        //
        // Correct the split line markers inside header blocks. Header blocks are identified by the regular expression RE_HEADER.
        //
        // Return the corrected markers.

        final String msgBodyWithLinkBracketsReplaced = replaceLinkBrackets(msgBody);

        // Don't process too long messages
        final String[] linesArray = msgBodyWithLinkBracketsReplaced.split("\\r?\\n");
        final String[] lines = (String[]) Arrays.copyOfRange(linesArray, 0, Math.min(linesArray.length, MAX_LINES_COUNT));

        final String markers = removeInitialSpacesAndMarkMessageLines(lines);

        final String markersWithQuotedEmailSplitlines = markQuotedEmailSplitlines(markers, lines);

        // We don't want splitlines in header blocks
        final String markersWithCorrectedSplitlinesInHeaders = correctSplitlinesInHeaders(markersWithQuotedEmailSplitlines, lines);

        return markersWithCorrectedSplitlinesInHeaders;
    }

    private static String replaceLinkBrackets(final String msgBody)
    {
        // Normalize links i.e. replace '<', '>' wrapping the link with some symbols so that '>' closing the link couldn't be mistakenly
        // taken for quotation marker.
        //
        // Converts msg_body into a unicode.

        final StringBuffer stringBuffer = new StringBuffer();
        final Matcher matcher = RE_LINK.matcher(msgBody);
        while (matcher.find()) { // TODO check
            final int newlineIndex = msgBody.substring(0, matcher.start()).lastIndexOf("\n");
            if (msgBody.charAt(newlineIndex + 1) == '>') {
                matcher.appendReplacement(stringBuffer, matcher.group());
            } else {
                matcher.appendReplacement(stringBuffer, String.format("@@%s@@", matcher.group(1)));
            }
        }
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    private static String removeInitialSpacesAndMarkMessageLines(final String[] lines)
    {
        // Removes the initial spaces in each line before marking message lines.
        //
        // This ensures headers can be identified if they are indented with spaces
        for (int i = 0; i < lines.length; i++) {
            lines[i] = CharMatcher.whitespace().trimLeadingFrom(lines[i]); // TODO check
        }

        return markMessageLines(lines);
    }

    private static String markMessageLines(final String[] lines)
    {
        // Mark message lines with markers to distinguish quotation lines.
        //
        // Markers:
        //
        // e - empty line
        // m - line that starts with quotation marker '>'
        // s - splitter line
        // t - presumably lines from the last message in the conversation
        final char[] markers = new char[lines.length];
        Arrays.fill(markers, 'm');

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().isEmpty()) { // TODO check
                markers[i] = 'e';  // empty line 6,8,10,15,16,18,20,23,26,27,28
            } else if (QUOT_PATTERN.matcher(lines[i]).lookingAt()) {
                markers[i] = 'm'; // line with quotation marker 30-102
            } else if (RE_FWD.matcher(lines[i]).lookingAt()) {
                markers[i] = 'f'; // ---- Forwarded message ---- // 24,38,58,76
            } else {
                // In case splitter is spread across several lines
                final String[] lineAppendedWithFollowingLinesArray = (String[]) Arrays.copyOfRange(lines, i, i + SPLITTER_MAX_LINES);
                final String lineAppendedWithFollowingLines = String.join("\n", Arrays.asList(lineAppendedWithFollowingLinesArray));
                final Matcher splitter = isSplitter(lineAppendedWithFollowingLines);
                if (splitter != null) {
                    // Append as many splitter markers as lines in splitter
                    System.out.println("Is splitter");
                    final String[] splitterLines = splitter.group().split("\\r?\\n"); // TODO check
                    for (int j = 0; j < splitterLines.length; j++) {
                        markers[i + j] = 's';
                    }
                    // Skip splitter lines
                    i += splitterLines.length - 1;
                } else {
                    // Probably the line from the last message in the conversation
                    markers[i] = 't';
                }
            }
        }

        return new String(markers);
    }

    private static Matcher isSplitter(final String line)
    {
        // Returns Matcher object if provided string is a splitter and null otherwise
        for (final Pattern splitterPattern : SPLITTER_PATTERNS) {
            final Matcher matcher = splitterPattern.matcher(line);
            if (matcher.lookingAt()) {
                return matcher;
            }
        }

        return null;
    }

    private static String markQuotedEmailSplitlines(final String markers, final String[] lines)
    {
        // When there are headers indented with '>' characters, this method will attempt to identify if the header is a splitline header.
        // If it is, then we mark it with 's' instead of leaving it as 'm' and return the new markers.

        // Create a list of markers to easily alter specific characters
        final char[] markerList = markers.toCharArray();

        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];

            if (markerList[i] != 'm') {
                continue;
            }

            for (final Pattern splitterPattern : SPLITTER_PATTERNS) {
                final Matcher matcher = splitterPattern.matcher(line);
                if (matcher.find()) {
                    markerList[i] = 's';
                    break;
                }
            }
        }

        return new String(markerList);
    }

    private static String correctSplitlinesInHeaders(final String markers, final String[] lines)
    {
        // Corrects markers by removing splitlines deemed to be inside header blocks.
        final StringBuilder updatedMarkers = new StringBuilder();
        int i = 0;
        boolean inHeaderBlock = false;

        for (char m : markers.toCharArray()) {
            // Only set inHeaderBlock flag when we hit an 's' and line is a header
            if (m == 's') {
                if (!inHeaderBlock) {
                    if (RE_HEADER.matcher(lines[i]).find()) {
                        inHeaderBlock = true;
                    }
                } else {
                    if (QUOT_PATTERN.matcher(lines[i]).lookingAt()) {
                        m = 'm';
                    } else {
                        m = 't';
                    }
                }
            }

            // If the line is not a header line, set in_header_block false.
            if (!RE_HEADER.matcher(lines[i]).find()) {
                inHeaderBlock = false;
            }

            // Add the marker to the new updated markers string.
            updatedMarkers.append(m);
            i += 1;
        }

        return updatedMarkers.toString();
    }
}
