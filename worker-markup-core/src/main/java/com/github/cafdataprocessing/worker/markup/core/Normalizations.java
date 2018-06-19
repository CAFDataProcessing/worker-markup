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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.Locale;
import java.util.Map;

/**
 * Normalization methods for values.
 */
public class Normalizations {
    private static final Map<String, String> PRIORITY_NORMALIZATIONS = ImmutableMap.<String, String> builder()
            .put("0", "Low")
            .put("1", "Normal")
            .put("2", "High")
            .put("lowest", "Low")
            .put("low", "Low")
            .put("normal", "Normal")
            .put("high", "High")
            .put("highest", "High")
            .build();

    /**
     * Remove all whitespace and newlines from a String.
     *
     * @param str
     * @return String
     */
    public static String removeAllWhitespace(String str)
    {
        String normalizedString = str.replaceAll("\\s+", "");
        return normalizedString;
    }

    /**
     * Removes whitespace and then removes &lt; &gt; tags and content (links)
     */
    public static String removeAllWhitespaceAndLinks(String str)
    {
        // Remove whitespace characters.
        str = str.replaceAll("\\s+", "");
        // Search for a <, remove non whitespace characters until the next > character (including the < and >).
        str = str.replaceAll("<\\S+?>", "");
        // Remove reply email quotation marks (> characters)
        str = str.replaceAll(">", "");
        str = str.replaceAll("‘", "'");
        str = str.replaceAll("’", "'");
        str = str.replaceAll("“", "\"");
        str = str.replaceAll("”", "\"");
        str = str.replaceAll("–", "-");
        return str;
    }

    /**
     * If the string begins with ' it assumes it is of the type '&lt;user@email.xxx&gt;' and so removes the '&lt;' and '&gt;'.
     *
     * Otherwise it removes these tags and their content and strips out ' and " punctuation.
     */
    public static String nameOnly(String str)
    {
        // Remove whitespace.
        str = str.replaceAll("\\s+", "");
        // Search for a < character, remove all non-whitespace and whitespace characters until the next >, including the < and >.
        str = str.replaceAll("<[^,;]+>", "");
        // Remove double quotation marks.
        str = str.replaceAll("\"", "");
        // Remove single quotation marks.
        str = str.replaceAll("\'", "");
        // Remove reply email quotation marks (> characters)
        str = str.replaceAll(">", "");
        // Replace semicolons to comas
        str = str.replaceAll(";", ",");
        return str;
    }

    /**
     * Returns a normalized case version of the provided string.
     * @param str Value to normalize case for.
     * @return Normalized case version of provided string value.
     */
    public static String normalizeCase(final String str){
        return str.toLowerCase(Locale.ENGLISH);
    }

    /**
     * If the supplied string is a recognized email priority value then this is converted to a standard form.
     *
     * Otherwise the supplied string is returned.
     *
     * @param str priority value to normalize
     * @return standard form representation of passed value
     */
    public static String normalizePriority(final String str)
    {
        final String normalizedPriority = PRIORITY_NORMALIZATIONS.get(str.trim().toLowerCase());
        return Strings.isNullOrEmpty(normalizedPriority) ? str : normalizedPriority;
    }
}
