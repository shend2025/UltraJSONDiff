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

package org.testtools.jsondiff.matcher;



import org.testtools.jsondiff.CompareContext;
import org.testtools.jsondiff.JSONCompareDetailResult;
import org.testtools.jsondiff.comparator.JSONComparator;
import org.testtools.jsondiff.comparator.JSONCompareUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static org.testtools.jsondiff.comparator.JSONCompareUtil.arrayOfJsonObjectToMap;
import static org.testtools.jsondiff.comparator.JSONCompareUtil.formatUniqueKey;

/**
 * <p>A value matcher for arrays of JsonObjects. This operates like
 * AbstractComparator.compareJSONArrayOfJsonObjects, of which the
 * unique key provided by member variables</p>*
 */
public class ArrayWithKeyMatcher<T> implements CustomValueMatcher<T> {
    private  String uniqueKey;

    private  boolean extensible;

    public ArrayWithKeyMatcher(){}

    /**
     * Create ArrayObjectValueMatcher to match every JsonObject by unique
     * key.
     * @param uniqueKey the unique key to identify JsonObject
     * @param extensible if true, allows keys in actual that don't appear in expected
     */
    public ArrayWithKeyMatcher(String uniqueKey, boolean extensible) {
        assert uniqueKey != null && !uniqueKey.isEmpty() : "uniqueKey null or empty";
        this.uniqueKey = uniqueKey;
        this.extensible = extensible;
    }

    public ArrayWithKeyMatcher(String path, String param, CompareContext compareContext){
        this.uniqueKey = param;
        this.extensible = compareContext.getExtensible();
    }

    @Override
    public void matcherInit(String param, CompareContext compareContext) {
        this.extensible = compareContext.getExtensible();
        this.uniqueKey = JSONCompareUtil.getParamValue(param);
    }

    /**
     * Create ArrayObjectValueMatcher to match every JsonObject by unique
     * key.
     * @param uniqueKey the unique key to identify JsonObject
     * @param extensible if true, allows keys in actual that don't appear in expected
     */
    public ArrayWithKeyMatcher(String uniqueKey, String extensible) {
        this (uniqueKey, extensible.equals("true"));
    }

    /**
     * Create ArrayObjectValueMatcher to match every JsonObject by unique
     * key.
     *
     * @param uniqueKey the unique key to identify JsonObject
     */
    public ArrayWithKeyMatcher(String uniqueKey) {
        this (uniqueKey, false);
    }

    @Override
    /*
     * NOTE: method defined as required by ValueMatcher interface but will never
     * be called so defined simply to indicate match failure
     */
    public boolean equal(T o1, T o2) {
        return false;
    }

    @Override
    public boolean equal(String prefix, T actual, T expected, JSONCompareDetailResult result, JSONComparator comparator) {
        if (!(actual instanceof JSONArray)) {
            throw new IllegalArgumentException("ArrayObjectValueMatcher applied to non-array actual value");
        }
        try {
            JSONArray actualArray = (JSONArray) actual;
            JSONArray expectedArray = expected instanceof JSONArray ? (JSONArray) expected: new JSONArray(new Object[] { expected });
            Map<Object, JSONObject> expectedValueMap = arrayOfJsonObjectToMap(expectedArray, uniqueKey);
            Map<Object, JSONObject> actualValueMap = arrayOfJsonObjectToMap(actualArray, uniqueKey);
            for (Object id : expectedValueMap.keySet()) {
                if (result.quickFail()) return true;
                if (!actualValueMap.containsKey(id)) {
                    result.missing(formatUniqueKey(prefix, uniqueKey, id), expectedValueMap.get(id));
                    continue;
                }
                JSONObject expectedValue = expectedValueMap.get(id);
                JSONObject actualValue = actualValueMap.get(id);
                comparator.compareValues(formatUniqueKey(prefix, uniqueKey, id), expectedValue,
                        actualValue, result);
            }
            for (Object id : actualValueMap.keySet()) {
                if (extensible || result.quickFail()) return true;
                if (!expectedValueMap.containsKey(id)) {
                    result.unexpected(formatUniqueKey(prefix, uniqueKey, id), actualValueMap.get(id));
                }
            }

            // any failures have already been passed to result, so return true
            return true;
        }
        catch (JSONException e) {
            return false;
        }
    }
}
