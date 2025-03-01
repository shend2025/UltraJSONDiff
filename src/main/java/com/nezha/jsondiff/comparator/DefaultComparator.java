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

package com.nezha.jsondiff.comparator;

import com.nezha.jsondiff.CompareContext;
import com.nezha.jsondiff.JSONCompareDetailResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.nezha.jsondiff.comparator.JSONCompareUtil.allJSONObjects;
import static com.nezha.jsondiff.comparator.JSONCompareUtil.allSimpleValues;

/**
 * This class is the default json comparator implementation.
 * Comparison is performed according to {@link CompareContext} that is passed as constructor's argument.
 */
public class DefaultComparator extends AbstractComparator {

    CompareContext mode;

    public DefaultComparator(CompareContext mode) {
        this.mode = mode;
    }

    @Override
    public void compareJSON(String prefix, JSONObject expected, JSONObject actual, JSONCompareDetailResult result)
            throws JSONException {
        // Check that actual contains all the expected values
        checkJsonObjectKeysExpectedInActual(prefix, expected, actual, result);

        // If strict, check for vice-versa
        if (!mode.isExtensible()) {
            checkJsonObjectKeysActualInExpected(prefix, expected, actual, result);
        }
    }

    @Override
    public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareDetailResult result)
            throws JSONException {
        if (areNumbers(expectedValue, actualValue)) {
            if (areNotSameDoubles(expectedValue, actualValue)) {
                result.fail(prefix, expectedValue, actualValue);
            }
        } else if (expectedValue.getClass().isAssignableFrom(actualValue.getClass())) {
            if (expectedValue instanceof JSONArray) {
                compareJSONArray(prefix, (JSONArray) expectedValue, (JSONArray) actualValue, result);
            } else if (expectedValue instanceof JSONObject) {
                compareJSON(prefix, (JSONObject) expectedValue, (JSONObject) actualValue, result);
            } else if (!expectedValue.equals(actualValue)) {
                result.fail(prefix, expectedValue, actualValue);
            }
        } else {
            // Added special handling for NUll, serialization issue due to jackson not recognizing JSONObject.NULL,
            // It can only be converted to normal null; Just deal with it here, because only with different types, the null error will appear
            result.fail(prefix, JSONCompareUtil.getIfNull(expectedValue),
                    JSONCompareUtil.getIfNull(actualValue));
        }
    }

    @Override
    public void compareJSONArray(String prefix, JSONArray expected, JSONArray actual, JSONCompareDetailResult result)
            throws JSONException {
        // 删除Array长度不一致则失败的判断
//        if (mode.hasStrictOrder()) {
//            compareJSONArrayWithStrictOrder(prefix, expected, actual, result);
//        } else if (allSimpleValues(expected)) {
//            compareJSONArrayOfSimpleValues(prefix, expected, actual, result);
//        } else if (allJSONObjects(expected)) {
//            compareJSONArrayOfJsonObjects(prefix, expected, actual, result);
//        } else {
//            // An expensive last resort
//            recursivelyCompareJSONArray(prefix, expected, actual, result);
//        }
        compareJSONArray(mode, prefix, expected, actual, result);
    }

    @Override
    public void compareJSONArray(CompareContext mode, String prefix, JSONArray expected,
                                 JSONArray actual, JSONCompareDetailResult result)
            throws JSONException {
        // 删除Array长度不一致则失败的判断
        if (mode.hasStrictOrder()) {
            compareJSONArrayWithStrictOrder(prefix, expected, actual, result);
        } else if (allSimpleValues(expected)) {
            compareJSONArrayOfSimpleValues(prefix, expected, actual, result);
        } else if (allJSONObjects(expected)) {
            compareJSONArrayOfJsonObjects(prefix, expected, actual, result);
        } else {
            // An expensive last resort
            recursivelyCompareJSONArray(prefix, expected, actual, result);
        }
    }

    @Override
    public void markMissing(String prefix, Object expected, JSONCompareDetailResult result) {
        result.missing(prefix, JSONCompareUtil.getIfNull(expected));
    }

    @Override
    public void markUnexpected(String prefix, Object actual, JSONCompareDetailResult result) {
        result.unexpected(prefix, JSONCompareUtil.getIfNull(actual));
    }

    @Override
    public void markResultFeature(JSONCompareDetailResult result) {
        if (mode.needQuickFail()) result.setComplete(false);
        if (mode.needIgnoreNull()) result.setIgnoreNull(true);
    }

    protected boolean areNumbers(Object expectedValue, Object actualValue) {
        return expectedValue instanceof Number && actualValue instanceof Number;
    }

    protected boolean areNotSameDoubles(Object expectedValue, Object actualValue) {
        return ((Number) expectedValue).doubleValue() != ((Number) actualValue).doubleValue();
    }
}
