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

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MarkupHeadersAndBody
{
    private static final Logger LOG = LoggerFactory.getLogger(MarkupHeadersAndBody.class);

    public static final String UNREADABLE_HEADER = "UnreadableHeader";
    public static final String HEADERS_WITH_ASTERISKS = "\\*(From|Sent|To|Subject):\\*";
    public static final String FROM_FIELD_WITH_ASTERISKS_SPLIT_ONTO_TWO_LINES_REGEX =
            "(?<FirstPartFromField>[> ]{0,}\\*From:\\*[A-z0-9][-A-z0-9_\\+\\.]*[A-z0-9]@[A-z0-9][-A-z0-9\\.]*[A-z0-9]\\.[A-z0-9]{1,3})\\n" +
            "(?<SecondPartFromField>[> ]{0,}\\[mailto:[A-z0-9][-A-z0-9_\\+\\.]*[A-z0-9]@[A-z0-9][-A-z0-9\\.]*[A-z0-9]\\.[A-z0-9]{1,3}\\].*)";

    /*
      The following GROUP_IDs correspond to the capturing groups in the RE_ON_DATE_SMB_WROTE regular expression.
      - ON_GROUP_ID corresponds to the capturing group that obtains the "On " text (or equivalent in the condensedHeaderMultilangMappings
      configuration).
      - SEPARATOR_GROUP_ID corresponds to the capturing group that contains the date-name separator (usually a "," but the
      condensedHeaderMultilangMappings can supply other separators based on the language).
      - WROTE_GROUP_ID corresponds to the capturing group that contains the "wrote:" text (or equivalent in the
      condensedHeaderMultilangMappings configuration).
     */
    private static final int ON_GROUP_ID = 1;
    private static final int SEPARATOR_GROUP_ID = 4;
    private static final int WROTE_GROUP_ID = 7;

    private final Map<String, List<String>> emailHeaderMappings;
    private final Pattern condensedHeaderRegEx;
    private final Pattern fromFieldSplitOntoTwoLines;
    private final Pattern headersWithAsterisks;

    public MarkupHeadersAndBody(
        final Map<String, List<String>> emailHeaderMappings,
        final Map<String, List<String>> condensedHeaderMultilangMappings
    )
    {
        this.emailHeaderMappings = emailHeaderMappings;

        // Set up the regular expression that matches the condensed header
        final String onDateSomebodyWroteRegEx = regexSetup(condensedHeaderMultilangMappings);
        this.condensedHeaderRegEx = Pattern.compile(onDateSomebodyWroteRegEx);
        this.fromFieldSplitOntoTwoLines = Pattern.compile(FROM_FIELD_WITH_ASTERISKS_SPLIT_ONTO_TWO_LINES_REGEX);
        this.headersWithAsterisks = Pattern.compile(HEADERS_WITH_ASTERISKS);
    }

    /**
     * Parse out specific email tags and use these to create XML Headers and a XML Body that can be added to the original XML document.
     *
     * @param document XML document to be parsed and marked up with Headers and a Body
     * @param emailHeaderMappings Map providing the mapping of Standard Header Names from Non-Standard Header Names
     * @param condensedHeaderMultilangMappings Map providing multilingual mappings used in RE_ON_DATE_SMB_WROTE regex to identify
     * condensed header
     */
    public static void markUpHeadersAndBody(final Document document, final Map<String, List<String>> emailHeaderMappings,
                                            final Map<String, List<String>> condensedHeaderMultilangMappings)
    {
        final MarkupHeadersAndBody markupEngine = new MarkupHeadersAndBody(emailHeaderMappings, condensedHeaderMultilangMappings);
        markupEngine.markUpHeadersAndBody(document);
    }

    /**
     * Marks up the e-mail text that is in the CONTENT fields of the specified xml document.
     *
     * @param document the XML document to be marked up
     */
    public void markUpHeadersAndBody(final Document document)
    {
        final Element rootElement = document.getRootElement();

        for (final Element contentElement : rootElement.getChildren("CONTENT")) {
            markUpHeadersAndBody(contentElement);
        }
    }

    /**
     * Parse out specific email tags and use these to create XML Headers and a XML Body that can be added to the original XML document.
     *
     * @param parentElement the element which contains the email elements
     */
    private void markUpHeadersAndBody(final Element parentElement)
    {
        LOG.info("Starting markup of Headers and Body");

        final List<Element> emailElements = parentElement.getChildren("email");

        final Parser nattyParser = new Parser(TimeZone.getTimeZone("UTC"));

        for (final Element emailElement : emailElements) {
            final String emailText = emailElement.getText();
            final String emailValueAtStart = emailElement.getValue();

            // Removal of existing content from email element
            emailElement.removeContent();

            // Creation of headers element
            Element headersElement = new Element("headers");
            emailElement.addContent(headersElement);

            // Creation of body element
            Element bodyElement = new Element("body");
            emailElement.addContent(bodyElement);

            int bodyIndex = 0;

            // Creating headers
            String[] lines = emailText.split("\n", -1);

            // Check to see if the there is a header field surrounded with '*'
            final Matcher asterisksHeaderMatcher = headersWithAsterisks.matcher(emailText);
            //Handle headers surrounded with asterisks separately
            if (asterisksHeaderMatcher.find()) {
                bodyIndex = handleHeaderWithAsterisks(emailText, lines, nattyParser, headersElement, bodyIndex);
            } else {
                for (String line : lines) {
                    // Compile the regex and evaluate to get matches for the line
                    Matcher matcher = condensedHeaderRegEx.matcher(line);

                    // Only enter this block if we get a match i.e. line is "On xxx, abc wrote:"
                    if (matcher.find()) {
                        bodyIndex++;
                        addCondensedHeader(nattyParser, headersElement, line, matcher);
                    } // Check if the line is a header i.e. TO: xxx, making sure it is not a "On x smb wrote:" with a space after the ":"
                    else if (line.contains(": ")) {
                        bodyIndex++;
                        addStandardisedHeader(nattyParser, headersElement, lines, line, ": ");
                    } else {
                        break;
                    }
                }
            }
            // Set the body text
            setBodyText(bodyElement, bodyIndex, lines);

            // Throw an error if the value of the email element has changed
            final String emailValueAtEnd = emailElement.getValue();
            if (!emailValueAtStart.equals(emailValueAtEnd)) {
                throw new RuntimeException("Logic error detected.  E-mail fidelity has been lost!");
            }
        }
        LOG.debug("Markup of Headers and Body complete");
    }

    /**
     * Handle the correct markup of header fields which are surrounded with '*', i.e *Sent:* xxx.
     *
     * @param emailText the email text that is to be marked up.
     * @param lines the emailText as separate lines.
     * @param nattyParser the natty parser to detect the date language.
     * @param headersElement the header element to add headers to.
     * @return the email body index.
     */
    private int handleHeaderWithAsterisks(final String emailText, final String[] lines, final Parser nattyParser,
                                          final Element headersElement, int bodyIndex)
    {
        // Check to see if the the from field is split onto two lines.
        final Matcher splitFromFieldMatcher = fromFieldSplitOntoTwoLines.matcher(emailText);
        final List<String> fromHeaderFieldValues = new ArrayList<>();

        if (splitFromFieldMatcher.find()) {
            for (final String line : lines) {
                // For a line which matches the format:
                // "*From:*example.emailaddress.com
                // [mailto:example.emailaddress.com] *On Behalf Of *Bloggs, Joe"
                // Add these lines to a list so that they can be marked up together later.
                if (line.equals(splitFromFieldMatcher.group("FirstPartFromField"))) {
                    fromHeaderFieldValues.add(line);
                } else if (line.equals(splitFromFieldMatcher.group("SecondPartFromField"))) {
                    fromHeaderFieldValues.add(line);
                }
            }
        }

        for (final String line : lines) {
            // If the the From field is split onto two lines, these lines need to be concatenated and marked up together.
            // Once there is a match for the second part of the <From> value just increase the bodyIndex.
            if (fromHeaderFieldValues.contains(line)) {
                bodyIndex++;
                if (line.equals(splitFromFieldMatcher.group("FirstPartFromField"))) {
                    final String fullFromFieldValue = line + "\n" + splitFromFieldMatcher.group("SecondPartFromField");
                    addStandardisedHeader(nattyParser, headersElement, lines, fullFromFieldValue, ":\\*");
                }
            }// Markup the remaining headers
            else if (line.contains(":*")) {
                bodyIndex++;
                addStandardisedHeader(nattyParser, headersElement, lines, line, ":\\*");
            } else {
                break;
            }
        }

        return bodyIndex;
    }

    /**
     * Marks up a line which is the condensed header (a header of the form "On date, person wrote:" will be marked up as the following:
     * "<headers>On <Sent>date</Sent>,<From> person </From>wrote:</headers>").
     *
     * Find the "On", "wrote" and the separator indexes in the line. Use these to identify the date and from text, in order to mark the
     * line up. Add the headers to the headersElement.
     *
     * @param nattyParser the natty parser used to attempt to detect the date text.
     * @param headersElement the header element to add the marked up headers.
     * @param line the line containing the condensed header to be marked up.
     * @param matcher the pattern matcher containing the result of our regex evaluated against the line.
     */
    private static void addCondensedHeader(Parser nattyParser, Element headersElement, String line, Matcher matcher)
    {
        // Declare the Substring object for the separator identifying indexes in the line for start and end of the separator.
        final Substring substring_separator;
        String dateTextIdentifiedByNatty = null; //setting to null - use this to make a decision later

        // Parse the line in natty to a date. If no date is detected, fall back on using regex to identify the separator.
        final List<DateGroup> dateGroups = nattyParser.parse(line);
        if (getDateFromNattyGroups(dateGroups) == null) {
            // If the separator could not be found, then just skip this line.
            if (matcher.group(SEPARATOR_GROUP_ID) == null) {
                LOG.warn("Couldn't find the date-name separator so we will skip the line: \"" + line + "\"");
                return;
            }
            // Try to use the separator returned in the regex to separate the date and name
            substring_separator = getSubstringForGroup(matcher, SEPARATOR_GROUP_ID);
        } else {
            // Use the date text identified by Natty to identify the location of the date-name separator.
            dateTextIdentifiedByNatty = dateGroups.get(0).getText();
            substring_separator = getSeparatorSubstringFromNattyText(line, dateTextIdentifiedByNatty);
        }

        // Find the substring returned which identifies the "On "
        final Substring substring_on = getSubstringForGroup(matcher, ON_GROUP_ID);

        // Find the substring returned which identifies the "wrote:"
        final Substring substring_wrote = getSubstringForGroup(matcher, WROTE_GROUP_ID);

        // Add to the headersElement the "Sent" and "From" elements and surrounding text
        compileHeaderElementForCondensedHeader(nattyParser, headersElement, line, substring_separator,
                                               dateTextIdentifiedByNatty, substring_on, substring_wrote);
    }

    /**
     * Marks up a line which is a normal header (a header of the form "From: person" which will be marked up as the following:
     * "<headers><from>From: person</from></headers>").
     *
     * Split the header line by the colon, standardise the header name using the emailHeaderMappings and add the header to the headers
     * element. Also, set a date attribute if natty can detect a date in the header.
     *
     * @param nattyParser the natty parser to detect the date language.
     * @param headersElement the header element to add headers to.
     * @param lines the lines remaining of the email.
     * @param line the current line containing the header.
     * @param valueToSplitOn the value that the line parameter should be split on.
     */
    private void addStandardisedHeader(Parser nattyParser, Element headersElement, String[] lines, String line, String valueToSplitOn)
    {
        // Split out the header name and value
        final String[] colonSplit = line.split(valueToSplitOn, 2);
        final String headerName = colonSplit[0];
        final String headerValue = colonSplit[1];

        // Header Names are standardised against a supplied set of names
        final String standardHeaderName = HeaderFieldNameMapper.standardiseHeaderName(headerName, emailHeaderMappings);

        // Invalid character are removed from XML Element Names to ensure names are XML
        // Production [4] and Production [5] compliant
        final String elementName = XmlParsingHelper.removeInvalidXmlElementNameChars(standardHeaderName, UNREADABLE_HEADER);
        Element header = createElementAndSetText(elementName, line);
        headersElement.addContent(header);
        if (lines.length > 1) {
            headersElement.addContent(new Text("\n"));
        }

        setDateAttributeIfExists(nattyParser, headerValue, elementName, header);
    }

    /**
     * Create a new element and set its text.
     *
     * @param elementName the name of the element
     * @param text the element's text
     * @return the element
     */
    private static Element createElementAndSetText(String elementName, String text)
    {
        Element element = new Element(elementName);
        element.setText(text);
        return element;
    }

    /**
     * Set the body text into the body element. The lines which make up the body text are those lines after the email headers until the
     * end of the email.
     *
     * @param bodyElement the body element to add the body text.
     * @param bodyIndex the index in the lines array where the body starts.
     * @param lines the array containing all the lines of the email.
     */
    private static void setBodyText(Element bodyElement, int bodyIndex, String[] lines)
    {
        // Setting the body text
        final StringBuilder bodyTextBuilder = new StringBuilder();

        if (bodyIndex < lines.length) {
            for (;;) {
                bodyTextBuilder.append(lines[bodyIndex]);
                bodyIndex++;
                if (bodyIndex >= lines.length) {
                    break;
                }
                bodyTextBuilder.append("\n");
            }
        }

        final String bodyText = bodyTextBuilder.toString();
        bodyElement.setText(bodyText);
    }

    /**
     * Based on the text discovered by Natty which it determines constitutes the date text, use this date text to identify the start and
     * end indexes of the separator.
     *
     * @param line the line from the email which we want to get the Substring from.
     * @param dateTextIdentifiedByNatty the text identified by Natty to be part of the date.
     * @return Substring object for the separator.
     */
    private static Substring getSeparatorSubstringFromNattyText(String line, String dateTextIdentifiedByNatty)
    {
        Substring substring_separator = new Substring();
        substring_separator.startIndex = line.indexOf(dateTextIdentifiedByNatty) + dateTextIdentifiedByNatty.length();
        substring_separator.endIndex = substring_separator.startIndex + 1; //assuming the separator is just a comma.
        return substring_separator;
    }

    /**
     * Get the MarkupHeadersAndBody.Substring object indicating the start and end of the group id for the matcher.
     *
     * @param matcher matcher which has been evaluated against a line with the regex
     * @param groupId the group id of the group you are looking for in the regex to create a Substring
     * @return Substring index markers for the group in the line
     */
    private static Substring getSubstringForGroup(Matcher matcher, int groupId)
    {
        Substring substring = new Substring();
        substring.startIndex = matcher.start(groupId);
        substring.endIndex = matcher.end(groupId);
        return substring;
    }

    /**
     * Add the Sent and From headers onto the existing headersElement, and also the surrounding text.
     *
     * @param nattyParser the parser used to attempt to find a date attribute
     * @param headersElement the headersElement to add the new Sent and From headers onto
     * @param line the line we have ran the regex against, we get the substring from this line
     * @param substring_separator Substring object for the date-name separator
     * @param dateTextIdentifiedByNatty the text of the date identified by natty. If null, we will use the on and separator substrings to
     * obtain the substring text
     * @param substring_on the substring in the line to obtain the "On " text
     * @param substring_wrote the substring in the line to obtain the "wrote:" text
     */
    private static void compileHeaderElementForCondensedHeader(Parser nattyParser, Element headersElement, String line,
                                                               Substring substring_separator, String dateTextIdentifiedByNatty,
                                                               Substring substring_on, Substring substring_wrote)
    {
        // Get the "On " prefix from the start of the line
        final String onPrefix = line.substring(0, substring_on.endIndex);

        // Get the date, either a substring determined by the regex or the date text identified by Natty date parser
        final String sent;
        if (dateTextIdentifiedByNatty == null) {
            // Natty did not identify the date so we fall back to using the substring between on and separator generated from the regex
            sent = line.substring(substring_on.endIndex, substring_separator.startIndex);
        } else {
            // Natty has already identified the date so we just use its text.
            sent = dateTextIdentifiedByNatty;
        }

        // Get the substring for the separator (usually a ",")
        final String separator = line.substring(substring_separator.startIndex, substring_separator.endIndex);

        // Get the name, which is between the date-name separator and the start of "wrote:"
        final String from = line.substring(substring_separator.endIndex, substring_wrote.startIndex);

        // Get the "wrote:" suffix determined by the regex, until the end of the line
        final String wroteSuffix = line.substring(substring_wrote.startIndex, line.length());

        // Create the sent and from header elements
        Element headerSent = createElementAndSetText("Sent", sent);
        Element headerFrom = createElementAndSetText("From", from);

        headersElement.addContent(new Text(onPrefix));
        headersElement.addContent(headerSent);
        headersElement.addContent(new Text(separator));
        headersElement.addContent(headerFrom);
        headersElement.addContent(new Text(wroteSuffix));
        headersElement.addContent(new Text("\n"));

        setDateAttributeIfExists(nattyParser, sent, headerSent.getName(), headerSent);
    }

    /**
     * Set up the regex to find if a line is of the form "On xxx abc wrote:"
     *
     * @param condensedHeaderMultilangMappings
     * @return
     */
    private static String regexSetup(Map<String, List<String>> condensedHeaderMultilangMappings)
    {
        List<String> on_pattern_quoted = new ArrayList<>();
        List<String> separator_pattern_quoted = new ArrayList<>();
        List<String> wrote_pattern_quoted = new ArrayList<>();

        // if no mapping is supplied in the configuration, the regex will still have default English mapping.
        on_pattern_quoted.add(Pattern.quote("On"));
        separator_pattern_quoted.add(Pattern.quote(","));
        wrote_pattern_quoted.add(Pattern.quote("wrote"));
        wrote_pattern_quoted.add(Pattern.quote("sent"));

        if(condensedHeaderMultilangMappings != null) {
            List<String> on_list = condensedHeaderMultilangMappings.get("On");
            List<String> separator_list = condensedHeaderMultilangMappings.get("Separator");
            List<String> wrote_list = condensedHeaderMultilangMappings.get("Wrote");

            if(on_list != null) on_pattern_quoted.addAll(on_list.stream().map(Pattern::quote).collect(Collectors.toList()));
            if(separator_list != null) separator_pattern_quoted.addAll(separator_list.stream().map(Pattern::quote).collect(Collectors.toList()));
            if(wrote_list != null) wrote_pattern_quoted.addAll(wrote_list.stream().map(Pattern::quote).collect(Collectors.toList()));
        }

        // Join the arrays into one string separated with or separator "|".
        return "(-*[>]?[ ]?(" + String.join("|", on_pattern_quoted) + ")[ ])(.*)(" + String.join("|", separator_pattern_quoted)
            + ")((.*\\n){0,2}.*)((" + String.join("|", wrote_pattern_quoted) + "):?-*.*)";
    }

    /**
     * Inner class representing a substring of the line, within which we are evaluating a regex against.
     */
    static class Substring
    {
        int startIndex;
        int endIndex;

        public Substring()
        {
            startIndex = -1;
            endIndex = -1;
        }
    }

    /**
     * Set as an attribute on the header, the date if one is detected and parsed by the natty date parser.
     *
     * @param nattyParser the parser to detect and parse the date.
     * @param headerValue the text in the header which the parser will search.
     * @param elementName the name of the element for logging purposes.
     * @param header the header to which we want to add the date attribute.
     */
    public static void setDateAttributeIfExists(Parser nattyParser, String headerValue, String elementName, Element header)
    {
        // Parse the header value to see if it is a date
        List<DateGroup> nattyDateGroups = nattyParser.parse(headerValue);
        final String headerDateUtc = getDateFromNattyGroups(nattyDateGroups);

        if (headerDateUtc != null) {
            header.setAttribute(new Attribute("dateUtc", headerDateUtc));

            if (nattyDateGroups.size() > 1) {
                LOG.warn("Multiple dates have been recognized from Natty, "
                    + "setting the first date as the \"" + elementName + "\" attribute.");
            }
        }
    }

    /**
     * Returns the first date from the first group in the list returned by the Natty parser (as a String in ISO-8601 format).
     */
    private static String getDateFromNattyGroups(final List<DateGroup> dateGroups)
    {
        if (dateGroups == null || dateGroups.size() <= 0) {
            return null;
        }

        final DateGroup dateGroup = dateGroups.get(0);

        if (dateGroup == null) {
            return null;
        }

        if (dateGroup.isDateInferred() || dateGroup.isTimeInferred()) {
            return null;
        }

        final List<Date> dates = dateGroup.getDates();

        if (dates == null || dates.size() <= 0) {
            return null;
        }

        final Date date = dates.get(0);

        if (date == null) {
            return null;
        }

        final Instant instant = date.toInstant();

        return instant.toString();
    }
}
