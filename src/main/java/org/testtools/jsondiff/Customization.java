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

package org.testtools.jsondiff;


import org.testtools.jsondiff.comparator.JSONComparator;
import org.testtools.jsondiff.matcher.CustomValueMatcher;
import org.testtools.jsondiff.matcher.LocationAwareValueMatcher;
import org.testtools.jsondiff.matcher.ValueMatcher;
import org.testtools.jsondiff.matcher.ValueMatcherException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Associates a custom matcher to a specific jsonpath.
 */
public final class Customization {
    private final Pattern path;
    private final ValueMatcher<Object> matcher;
    private int matchLevel;
    private String minMatchStr;

    public Customization(String path, ValueMatcher<Object> matcher) {
        if (path == null) {
            path = "";
        }
        assert matcher != null;
        buildMatchParam(path);
        this.path = Pattern.compile(buildPattern(path));
        this.matcher = matcher;
    }

    /**
     * Creates a new {@link Customization} instance for {@code path} and {@code comparator}.
     *
     * @param path       the json path
     * @param comparator the comparator
     * @return a new Customization
     */
    public static Customization customization(String path, ValueMatcher<Object> comparator) {
        return new Customization(path, comparator);
    }

    private void buildMatchParam(String path) {
        Pattern p1 = Pattern.compile("^\\*\\*\\.(.+)");
        Pattern p1_e = Pattern.compile("^\\*\\*\\.(.+)\\*(.*)");
        Matcher m1 = p1.matcher(path);
        Matcher m1_e = p1_e.matcher(path);
        if (m1.matches() && !m1_e.matches()) {
            matchLevel = 1;
            minMatchStr = m1.group(1);
        } else if (m1_e.matches()) {
            matchLevel = 2;
            minMatchStr = m1_e.group(2);
        }
    }

    private boolean matchPath(String path) {
        switch (this.matchLevel) {
            case 1:
                return path.equals(minMatchStr) || path.endsWith("." + minMatchStr);
            case 2:
                return path.endsWith(minMatchStr) && this.path.matcher(path).matches();
            default:
                return this.path.matcher(path).matches();
        }
    }

    private String buildPattern(String path) {
        // If the path is $, it means that it matches an empty string, that is,
        // the root directory, and there is no need to perform path regularization conversion
        if (path.equals("$")) return path;
        return buildPatternLevel1(path);
    }

    private String buildPatternLevel1(String path) {
        String regex = "\\*\\*\\.";
        String replacement = "(?:.+\\.)?";

        return buildPattern(path, regex, replacement, 1);
    }

    private String buildPatternLevel2(String s) {
        if (s.isEmpty()) {
            return "";
        }
        String regex = "\\*\\*";
        String replacement = ".+";

        return buildPattern(s, regex, replacement, 2);
    }

    private String buildPatternLevel3(String s) {
        if (s.isEmpty()) {
            return "";
        }

        String regex = "\\*";
        String replacement = "[^\\.]+";

        return buildPattern(s, regex, replacement, 3);
    }

    private String buildPattern(String path, String regex, String replacement, int level) {
        StringBuilder sb = new StringBuilder();
        String[] parts = path.split(regex);
        for (int i = 0; i < parts.length; i++) {
            sb.append(buildPatternForLevel(level, parts[i]));
            if (i < parts.length - 1) {
                sb.append(replacement);
            }
        }
        return sb.toString();
    }

    private String buildPatternForLevel(int level, String part) {
        switch (level) {
            case 1:
                return buildPatternLevel2(part);
            case 2:
                return buildPatternLevel3(part);
            case 3:
                return Pattern.quote(part);
            default:
                return "Incorrect level.";
        }
    }

    public boolean appliesToPath(String path) {
        return matchPath(path);
//		return this.path.matcher(path).matches();
    }

    /**
     * Return true if actual value matches expected value using this
     * Customization's comparator. Calls to this method should be replaced by
     * calls to matches(String prefix, Object actual, Object expected,
     * JSONCompareDetailResult result).
     *
     * @param actual   JSON value being tested
     * @param expected expected JSON value
     * @return true if actual value matches expected value
     */
    @Deprecated
    public boolean matches(Object actual, Object expected) {
        return matcher.equal(actual, expected);
    }

    /**
     * Return true if actual value matches expected value using this
     * Customization's comparator. The equal method used for comparison depends
     * on type of comparator.
     *
     * @param prefix   JSON path of the JSON item being tested (only used if
     *                 comparator is a LocationAwareValueMatcher)
     * @param actual   JSON value being tested
     * @param expected expected JSON value
     * @param result   JSONCompareDetailResult to which match failure may be passed (only
     *                 used if comparator is a LocationAwareValueMatcher)
     * @return true if expected and actual equal or any difference has already
     * been passed to specified result instance, false otherwise.
     * @throws ValueMatcherException if expected and actual values not equal and ValueMatcher
     *                               needs to override default comparison failure message that
     *                               would be generated if this method returned false.
     */
    public boolean matches(String prefix, Object actual, Object expected,
                           JSONCompareDetailResult result, JSONComparator comparator) throws
            ValueMatcherException {
        if (matcher instanceof LocationAwareValueMatcher) {
            return ((LocationAwareValueMatcher<Object>) matcher).equal(prefix, actual, expected, result);
        } else if (matcher instanceof CustomValueMatcher) {
            return ((CustomValueMatcher<Object>) matcher).equal(prefix, actual, expected, result,
                    comparator);
        }
        return matcher.equal(actual, expected);
    }

    public String instanceOfMatcher() {
        return matcher.getClass().getSimpleName();
    }
}
