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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.testtools.jsondiff.comparator.CustomComparator;
import org.testtools.jsondiff.comparator.DefaultComparator;
import org.testtools.jsondiff.comparator.JSONComparator;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.List;

/**
 * Provides API to compare two JSON entities using YAML configuration.
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
    public static JSONCompareDetailResult compareJSONInternal(String expectedStr, String actualStr, JSONComparator comparator)
            throws JSONException {
        Object expected = JSONParser.parseJSON(expectedStr);
        Object actual = JSONParser.parseJSON(actualStr);
        if ((expected instanceof JSONObject) && (actual instanceof JSONObject)) {
            return compareJSONInternal((JSONObject) expected, (JSONObject) actual, comparator);
        } else if ((expected instanceof JSONArray) && (actual instanceof JSONArray)) {
            return compareJSONInternal((JSONArray) expected, (JSONArray) actual, comparator);
        } else if (expected instanceof JSONString && actual instanceof JSONString) {
            return compareJSONInternal((JSONString) expected, (JSONString) actual);
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
    public static JSONCompareDetailResult compareJSONInternal(JSONObject expected, JSONObject actual, JSONComparator comparator)
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
    public static JSONCompareDetailResult compareJSONInternal(JSONArray expected, JSONArray actual, JSONComparator comparator)
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
    public static JSONCompareDetailResult compareJSONInternal(final JSONString expected, final JSONString actual) {
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
    public static JSONCompareDetailResult compareJSONInternal(String expectedStr, String actualStr, CompareContext mode)
            throws JSONException {
        return compareJSONInternal(expectedStr, actualStr, getComparatorForMode(mode));
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
    public static JSONCompareDetailResult compareJSONInternal(JSONObject expected, JSONObject actual, CompareContext mode)
            throws JSONException {
        return compareJSONInternal(expected, actual, getComparatorForMode(mode));
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
    public static JSONCompareDetailResult compareJSONInternal(JSONArray expected, JSONArray actual, CompareContext mode)
            throws JSONException {
        return compareJSONInternal(expected, actual, getComparatorForMode(mode));
    }

    /**
     * Compares JSON using YAML configuration rules.
     *
     * @param expectedStr Expected JSON string
     * @param actualStr   JSON string to compare
     * @param yamlRule    YAML configuration string containing comparison rules
     * @return result of the comparison
     * @throws Exception if YAML parsing or comparison fails
     */
    public static JSONCompareResult compareJSON(String expectedStr, String actualStr, String yamlRule)
            throws Exception {
        JSONCompareConf yamlRuleObj = new JSONCompareConf();
        yamlRuleObj.readNodeFromYaml(yamlRule);
        List<CompareRule> compareRules = yamlRuleObj.getCompareRules();

        JSONCompareResult result = new JSONCompareResult();
        // Parse JSON strings
        DocumentContext contextExpect = JsonPath.parse(expectedStr);
        DocumentContext contextActual = JsonPath.parse(actualStr);

        // Use path expressions to read values
        String expectedByJsonPath = expectedStr;
        String actualByJsonPath = actualStr;

        for (CompareRule compareRule : compareRules) {
            CustomComparator comparator = CompareRulesTransformer.getComparator(compareRule);
            if (compareRule.getJsonPath() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                expectedByJsonPath = objectMapper.writeValueAsString(contextExpect.read(compareRule.getJsonPath()));
                actualByJsonPath = objectMapper.writeValueAsString(contextActual.read(compareRule.getJsonPath()));
                //If compareRule has preprocess and removeNode's jsonPath is not empty, perform preprocessing first
                if (compareRule.getPreProcesses() != null){
                    for (PreProcessItem preProcess : compareRule.getPreProcesses()) {
                        if ("removeNode".equals(preProcess.getAction())  && !StringUtils.isEmpty(preProcess.getPath())) {
                            expectedByJsonPath = removeNode(expectedByJsonPath, preProcess.getPath());
                            actualByJsonPath = removeNode(actualByJsonPath, preProcess.getPath());
                        }
                    }
                }
            }

            try {
                JSONCompareSimpleResult compareSimpleResult = compareJSONComparator(expectedByJsonPath, actualByJsonPath, comparator);
                result.addFailures(compareSimpleResult.getFailure());
            } catch (JSONException e) {
                FailureField failureField = new FailureField("", "", compareRule.getJsonPath(), e.getMessage());
                result.addFailure(failureField);
            }
        }
        return result;
    }


    /**
     * Compares JSON string provided to the expected JSON string using provided comparator, and returns the results of
     * the comparison.
     *
     * @param expectedStr Expected JSON string
     * @param actualStr   JSON string to compare
     * @param comparator  Comparator to use
     * @return result of the comparison
     * @throws JSONException JSON parsing error
     */
    public static JSONCompareSimpleResult compareJSONComparator(String expectedStr, String actualStr,
                                                            JSONComparator comparator)
            throws JSONException {
        Object expected = JSONParser.parseJSON(expectedStr);
        Object actual = JSONParser.parseJSON(actualStr);
        JSONCompareDetailResult result;
        if ((expected instanceof JSONObject) && (actual instanceof JSONObject)) {
            result = compareJSONInternal((JSONObject) expected, (JSONObject) actual, comparator);
        } else if ((expected instanceof JSONArray) && (actual instanceof JSONArray)) {
            result = compareJSONInternal((JSONArray) expected, (JSONArray) actual, comparator);
        } else if (expected instanceof JSONString && actual instanceof JSONString) {
            result = compareJSONInternal((JSONString) expected, (JSONString) actual);
        } else {
            result = new JSONCompareDetailResult();
            result.fail("", expected, actual);
        }
        return JSONCompareResultUtil.getSimpleResult(result);
    }

    //Add preprocess method to preprocess json, remove corresponding nodes, and return new json
    public static String removeNode(String json, String jsonPath) {
        DocumentContext context = JsonPath.parse(json);
        context.delete(jsonPath);
        return context.jsonString();
    }
}
