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

package org.testtools.jsondiff.comparator;

import org.testtools.jsondiff.JSONCompareDetailResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.testtools.jsondiff.comparator.JSONCompareUtil.*;

/**
 * This class provides a skeletal implementation of the {@link JSONComparator}
 * interface, to minimize the effort required to implement this interface.
 */
public abstract class AbstractComparator implements JSONComparator {

    /**
     * Compares JSONObject provided to the expected JSONObject, and returns the results of the comparison.
     *
     * @param expected Expected JSONObject
     * @param actual   JSONObject to compare
     * @throws JSONException JSON parsing error
     */
    @Override
    public final JSONCompareDetailResult compareJSON(JSONObject expected, JSONObject actual) throws JSONException {
        JSONCompareDetailResult result = new JSONCompareDetailResult();
        markResultFeature(result);
        compareJSON("", expected, actual, result);
        return result;
    }

    /**
     * Compares JSONArray provided to the expected JSONArray, and returns the results of the comparison.
     *
     * @param expected Expected JSONArray
     * @param actual   JSONArray to compare
     * @throws JSONException JSON parsing error
     */
    @Override
    public final JSONCompareDetailResult compareJSON(JSONArray expected, JSONArray actual) throws JSONException {
        JSONCompareDetailResult result = new JSONCompareDetailResult();
        markResultFeature(result);
        // 为了使JSONArray对象的根目录能进行自定义规则，在JSONArray对比入口改动为调用compareValues
        // compareJSONArray("", expected, actual, result);
        compareValues("", expected, actual, result);
        return result;
    }

    protected void checkJsonObjectKeysActualInExpected(String prefix, JSONObject expected, JSONObject actual, JSONCompareDetailResult result)
            throws JSONException {
        Set<String> actualKeys = getKeys(actual);
        for (String key : actualKeys) {
            if (result.quickFail()) return;
            if (!expected.has(key)) {
                // unexpected的value也需体现在result中，之前传入key
                markUnexpected(qualify(prefix, key), actual.get(key), result);
            }
        }
    }

    protected void checkJsonObjectKeysExpectedInActual(String prefix, JSONObject expected, JSONObject actual, JSONCompareDetailResult result) throws JSONException {
        Set<String> expectedKeys = getKeys(expected);
        for (String key : expectedKeys) {
            if (result.quickFail()) return;
            Object expectedValue = expected.get(key);
            if (actual.has(key)) {
                Object actualValue = actual.get(key);
                compareValues(qualify(prefix, key), expectedValue, actualValue, result);
            } else {
                // missing的value也需体现在result中，之前传入key
                markMissing(qualify(prefix, key), expectedValue, result);
            }
        }
    }

    protected void compareJSONArrayOfJsonObjects(String key, JSONArray expected, JSONArray actual, JSONCompareDetailResult result) throws JSONException {
        String uniqueKey = findUniqueKey(expected);
        if (uniqueKey == null || !isUsableAsUniqueKey(uniqueKey, actual)) {
            // An expensive last resort
            recursivelyCompareJSONArray(key, expected, actual, result);
            return;
        }
        Map<Object, JSONObject> expectedValueMap = arrayOfJsonObjectToMap(expected, uniqueKey);
        Map<Object, JSONObject> actualValueMap = arrayOfJsonObjectToMap(actual, uniqueKey);
        if (!isUsableAsUniqueKeyForBothMap(expectedValueMap, actualValueMap)) {
            recursivelyCompareJSONArray(key, expected, actual, result);
            return;
        }
        for (Object id : expectedValueMap.keySet()) {
            if (result.quickFail()) return;
            if (!actualValueMap.containsKey(id)) {
                result.missing(formatUniqueKey(key, uniqueKey, id), expectedValueMap.get(id));
                continue;
            }
            JSONObject expectedValue = expectedValueMap.get(id);
            JSONObject actualValue = actualValueMap.get(id);
            compareValues(formatUniqueKey(key, uniqueKey, id), expectedValue, actualValue, result);
        }
        for (Object id : actualValueMap.keySet()) {
            if (result.quickFail()) return;
            if (!expectedValueMap.containsKey(id)) {
                result.unexpected(formatUniqueKey(key, uniqueKey, id), actualValueMap.get(id));
            }
        }
    }

    protected void compareJSONArrayOfSimpleValues(String key, JSONArray expected, JSONArray actual, JSONCompareDetailResult result) throws JSONException {
        Map<Object, Integer> expectedCount = JSONCompareUtil.getCardinalityMap(jsonArrayToList(expected));
        Map<Object, Integer> actualCount = JSONCompareUtil.getCardinalityMap(jsonArrayToList(actual));
        for (Object o : expectedCount.keySet()) {
            if (result.quickFail()) return;
            if (!actualCount.containsKey(o)) {
                result.missing(key + "[]", o);
            } else if (!actualCount.get(o).equals(expectedCount.get(o))) {
                // 改动：加入JSONCompareResult._fieldFailures字段中
                result.fail(key + "[]", "Expected " + expectedCount.get(o) + " occurrence(s) " +
                        "of " + o, " but got " + actualCount.get(o) + " occurrence(s)");
            }
        }
        for (Object o : actualCount.keySet()) {
            if (result.quickFail()) return;
            if (!expectedCount.containsKey(o)) {
                result.unexpected(key + "[]", o);
            }
        }
    }

    protected void compareJSONArrayWithStrictOrder(String key, JSONArray expected, JSONArray actual, JSONCompareDetailResult result) throws JSONException {
        // 20220825 支持不同长度的json array进行对比
        int length = Math.min(expected.length(), actual.length());
        for (int i = length; i < expected.length(); i++) {
            if (result.quickFail()) return;
            Object expectedValue = expected.get(i);
            result.missing(key + "[" + i + "]", expectedValue);
        }
        for (int i = length; i < actual.length(); i++) {
            if (result.quickFail()) return;
            Object actualValue = actual.get(i);
            result.unexpected(key + "[" + i + "]", actualValue);
        }
        // 修改对比的范围
        for (int i = 0; i < length; ++i) {
            if (result.quickFail()) return;
            Object expectedValue = expected.get(i);
            Object actualValue = actual.get(i);
            compareValues(key + "[" + i + "]", expectedValue, actualValue, result);
        }
    }

    // This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose array ordering, and no
    // easy way to uniquely identify each element.
    // This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose array ordering, and no
    // easy way to uniquely identify each element.
    protected void recursivelyCompareJSONArray(String key, JSONArray expected, JSONArray actual,
                                               JSONCompareDetailResult result) throws JSONException {
        Set<Integer> matched = new HashSet<Integer>();
        for (int i = 0; i < expected.length(); ++i) {
            if (result.quickFail()) return;
            Object expectedElement = expected.get(i);
            boolean matchFound = false;
            for (int j = 0; j < actual.length(); ++j) {
                Object actualElement = actual.get(j);
                if (matched.contains(j) || !actualElement.getClass().equals(expectedElement.getClass())) {
                    continue;
                }
                if (expectedElement instanceof JSONObject) {
                    if (compareJSON((JSONObject) expectedElement, (JSONObject) actualElement).passed()) {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                } else if (expectedElement instanceof JSONArray) {
                    if (compareJSON((JSONArray) expectedElement, (JSONArray) actualElement).passed()) {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                } else if (expectedElement.equals(actualElement)) {
                    matched.add(j);
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                // 没有匹配上的expected element, 记为missing
//                result.fail(key + "[" + i + "] Could not find match for element " + expectedElement);
                result.missing(key + "[" + i + "]", expectedElement);
            }
        }
        // 未匹配上的actual，记为unexpected
        for (int j = 0; j < actual.length(); ++j) {
            if (result.quickFail()) return;
            Object actualElement = actual.get(j);
            if (matched.contains(j)) {
                continue;
            }
            result.unexpected(key + "[" + j + "]", actualElement);
        }
    }
}
