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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.nezha.jsondiff.comparator.CustomComparator;
import com.nezha.jsondiff.comparator.DefaultComparator;
import com.nezha.jsondiff.comparator.JSONComparator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.List;

/**
 * Provides API to compare two JSON entities.  but it can
 * be programmed against directly to access the functionality.  (eg, to make something that works with a
 * non-JUnit test framework)
 */
public final class JSONCompare {
    private JSONCompare() {
    }

    private static JSONComparator getComparatorForMode(CompareContext mode) {
        return new DefaultComparator(mode);
    }

    /**
     * Compares JSON string provided to the expected JSON string using provided comparator, and returns the results of
     * the comparison.
     *
     * @param expectedStr Expected JSON string
     * @param actualStr   JSON string to compare
     * @param comparator  Comparator to use
     * @return result of the comparison
     * @throws JSONException            JSON parsing error
     * @throws IllegalArgumentException when type of expectedStr doesn't match the type of actualStr
     */
    public static JSONCompareDetailResult compareJSON(String expectedStr, String actualStr, JSONComparator comparator)
            throws JSONException {
        Object expected = JSONParser.parseJSON(expectedStr);
        Object actual = JSONParser.parseJSON(actualStr);
        if ((expected instanceof JSONObject) && (actual instanceof JSONObject)) {
            return compareJSON((JSONObject) expected, (JSONObject) actual, comparator);
        } else if ((expected instanceof JSONArray) && (actual instanceof JSONArray)) {
            return compareJSON((JSONArray) expected, (JSONArray) actual, comparator);
        } else if (expected instanceof JSONString && actual instanceof JSONString) {
            return compareJson((JSONString) expected, (JSONString) actual);
        } else if (expected instanceof JSONObject) {
            return new JSONCompareDetailResult().fail("", expected, actual);
        } else {
            return new JSONCompareDetailResult().fail("", expected, actual);
        }
    }

    /**
     * Compares JSON object provided to the expected JSON object using provided comparator, and returns the results of
     * the comparison.
     *
     * @param expected   expected json object
     * @param actual     actual json object
     * @param comparator comparator to use
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareDetailResult compareJSON(JSONObject expected, JSONObject actual, JSONComparator comparator)
            throws JSONException {
        return comparator.compareJSON(expected, actual);
    }

    /**
     * Compares JSON object provided to the expected JSON object using provided comparator, and returns the results of
     * the comparison.
     *
     * @param expected   expected json array
     * @param actual     actual json array
     * @param comparator comparator to use
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareDetailResult compareJSON(JSONArray expected, JSONArray actual, JSONComparator comparator)
            throws JSONException {
        return comparator.compareJSON(expected, actual);
    }

    /**
     * Compares {@link JSONString} provided to the expected {@code JSONString}, checking that the
     * {@link JSONString#toJSONString()} are equal.
     *
     * @param expected Expected {@code JSONstring}
     * @param actual   {@code JSONstring} to compare
     * @return result of the comparison
     */
    public static JSONCompareDetailResult compareJson(final JSONString expected, final JSONString actual) {
        final JSONCompareDetailResult result = new JSONCompareDetailResult();
        final String expectedJson = expected.toJSONString();
        final String actualJson = actual.toJSONString();
        if (!expectedJson.equals(actualJson)) {
            result.fail("");
        }
        return result;
    }

    /**
     * Compares JSON string provided to the expected JSON string, and returns the results of the comparison.
     *
     * @param expectedStr Expected JSON string
     * @param actualStr   JSON string to compare
     * @param mode        Defines comparison behavior
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareDetailResult compareJSON(String expectedStr, String actualStr, CompareContext mode)
            throws JSONException {
        return compareJSON(expectedStr, actualStr, getComparatorForMode(mode));
    }

    /**
     * Compares JSONObject provided to the expected JSONObject, and returns the results of the comparison.
     *
     * @param expected Expected JSONObject
     * @param actual   JSONObject to compare
     * @param mode     Defines comparison behavior
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareDetailResult compareJSON(JSONObject expected, JSONObject actual, CompareContext mode)
            throws JSONException {
        return compareJSON(expected, actual, getComparatorForMode(mode));
    }


    /**
     * Compares JSONArray provided to the expected JSONArray, and returns the results of the comparison.
     *
     * @param expected Expected JSONArray
     * @param actual   JSONArray to compare
     * @param mode     Defines comparison behavior
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareDetailResult compareJSON(JSONArray expected, JSONArray actual, CompareContext mode)
            throws JSONException {
        return compareJSON(expected, actual, getComparatorForMode(mode));
    }


    /**
     * Compares JSONArray provided to the expected JSONArray, and returns the results of the comparison.
     *
     * @param expectedStr  Expected JSON string
     * @param actualStr    JSON string to compare
     * @param compareRules Compare rules in JSON string
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareSimpleResult compareJSONSimple(String expectedStr, String actualStr,
                                                            String compareRules)
            throws JSONException {
        CustomComparator comparator = CompareRulesTransformer.getComparator(compareRules);
        return compareJSONSimple(expectedStr, actualStr, comparator);
    }


    public static JSONCompareResult compareJSONYaml(String expectedStr, String actualStr, String yamlRule)
            throws Exception {
        JSONCompareConf yamlRuleObj = new JSONCompareConf();
        yamlRuleObj.readNodeFromYaml(yamlRule);
        List<CompareRule> compareRules = yamlRuleObj.getCompareRules();

        JSONCompareResult result = new JSONCompareResult();
        // 解析 JSON 字符串
        DocumentContext contextExpect = JsonPath.parse(expectedStr);
        DocumentContext contextActual = JsonPath.parse(actualStr);

        // 使用路径表达式读取值
        String expectedByJsonPath = expectedStr;
        String actualByJsonPath = actualStr;

        for (CompareRule compareRule : compareRules) {
            CustomComparator comparator = CompareRulesTransformer.getComparator(compareRule);
            if (compareRule.getJsonPath() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                expectedByJsonPath = objectMapper.writeValueAsString(contextExpect.read(compareRule.getJsonPath()));
                actualByJsonPath = objectMapper.writeValueAsString(contextActual.read(compareRule.getJsonPath()));
            }

            try {
                JSONCompareSimpleResult compareSimpleResult = compareJSONSimpleYaml(expectedByJsonPath, actualByJsonPath, comparator);
                result.addFailures(compareSimpleResult.getFailure());
            } catch (JSONException e) {
                FailureField failureField = new FailureField("", "", compareRule.getJsonPath(), e.getMessage());
                result.addFailure(failureField);
            }

        }
        return result;
    }

    public static JSONCompareDeepDetailResult compareJSONDeep(String expectedStr, String actualStr,
                                                              JSONComparator comparator)
            throws JSONException {
        Object expected = JSONParser.parseJSON(expectedStr);
        Object actual = JSONParser.parseJSON(actualStr);
        JSONCompareDeepDetailResult result;
        if ((expected instanceof JSONObject) && (actual instanceof JSONObject)) {
            result = new JSONCompareDeepDetailResult(compareJSON((JSONObject) expected,
                    (JSONObject) actual, comparator));
        } else if ((expected instanceof JSONArray) && (actual instanceof JSONArray)) {
            result = new JSONCompareDeepDetailResult(compareJSON((JSONArray) expected, (JSONArray) actual
                    , comparator));
        } else if (expected instanceof JSONString && actual instanceof JSONString) {
            result = new JSONCompareDeepDetailResult(compareJson((JSONString) expected,
                    (JSONString) actual));
        } else {
            result = new JSONCompareDeepDetailResult();
            result.fail("", expected, actual);
            return result;
        }
        JSONCompareResultUtil.getAbsolutePath(expected, actual, result);
        return result;
    }


    public static JSONCompareSimpleResult compareJSONSimpleYaml(String expectedStr, String actualStr, CustomComparator comparator)
            throws JSONException {
        return compareJSONSimple(expectedStr, actualStr, comparator);
    }

    public static JSONCompareSimpleResult compareJSONSimple(String expectedStr, String actualStr,
                                                            JSONComparator comparator)
            throws JSONException {
        Object expected = JSONParser.parseJSON(expectedStr);
        Object actual = JSONParser.parseJSON(actualStr);
        JSONCompareDetailResult result;
        if ((expected instanceof JSONObject) && (actual instanceof JSONObject)) {
            result = compareJSON((JSONObject) expected, (JSONObject) actual, comparator);
        } else if ((expected instanceof JSONArray) && (actual instanceof JSONArray)) {
            result = compareJSON((JSONArray) expected, (JSONArray) actual, comparator);
        } else if (expected instanceof JSONString && actual instanceof JSONString) {
            result = compareJson((JSONString) expected, (JSONString) actual);
        } else {
            result = new JSONCompareDetailResult();
            result.fail("", expected, actual);
        }
        JSONCompareSimpleResult simpleResult = JSONCompareResultUtil.getSimpleResult(result);
        return simpleResult;
    }
}
