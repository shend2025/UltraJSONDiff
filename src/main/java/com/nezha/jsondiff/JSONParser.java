/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nezha.jsondiff;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple JSON parsing utility.
 */
public class JSONParser {
    // regular expression to match a number in JSON format.  see http://www.json.org/fatfree.html.
    // "A number can be represented as integer, real, or floating point. JSON does not support octal or hex
    // ... [or] NaN or Infinity".
    private static final String NUMBER_REGEX = "-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?";

    private JSONParser() {
    }

    /**
     * Takes a JSON string and returns either a {@link JSONObject} or {@link JSONArray},
     * depending on whether the string represents an object or an array.
     *
     * @param s Raw JSON string to be parsed
     * @return JSONObject or JSONArray
     * @throws JSONException JSON parsing error
     */
    public static Object parseJSON(final String s) throws JSONException {
        if (s.trim().startsWith("{")) {
            return new JSONObject(s);
        } else if (s.trim().startsWith("[")) {
            return new JSONArray(s);
        } else if (s.trim().startsWith("\"")
                || s.trim().matches(NUMBER_REGEX)) {
            return new JSONString() {
                @Override
                public String toJSONString() {
                    return s;
                }
            };
        }
        throw new JSONException("Unparsable JSON string: " + s);
    }

    /**
     * Takes an escaped JSON string and returns either a {@link JSONObject} or
     * {@link JSONArray},
     * depending on whether the string represents an object or an array.
     *
     * @param s Raw JSON string to be parsed
     * @return JSONObject or JSONArray
     * @throws JSONException JSON parsing error
     */
    public static Object parseEscapedJSON(final String s) throws JSONException {
        String unescapedStr = s;
        unescapedStr = getEscapedJSONInUnescapedJSONObject(unescapedStr);
        unescapedStr = getEscapedJSONInUnescapedJSONArray(unescapedStr);
        return parseJSON(unescapedStr);
    }

    private static String getEscapedJSONInUnescapedJSONObject(String s) {
        Pattern pattern = Pattern.compile("\"\\{(.*?)\\}\"");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            String matchedStr = matcher.group();
            String replacedStr = StringEscapeUtils.unescapeJavaScript(matchedStr.substring(1,
                    matchedStr.length() - 1));
            s = s.replace(matchedStr, replacedStr);
        }
        return s;
    }

    private static String getEscapedJSONInUnescapedJSONArray(String s) {
        Pattern pattern = Pattern.compile("\"\\[(.*?)\\]\"");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            String matchedStr = matcher.group();
            String replacedStr = StringEscapeUtils.unescapeJavaScript(matchedStr.substring(1,
                    matchedStr.length() - 1));
            s = s.replace(matchedStr, replacedStr);
        }
        return s;
    }
}
