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

/**
 * Interface for comparison handler.
 *
 * @author <a href="mailto:aiveeen@gmail.com">Ivan Zaytsev</a>
 * 2013-01-04
 */
public interface JSONComparator {

    /**
     * Compares two {@link JSONObject}s and returns the result of the comparison in a {@link JSONCompareDetailResult} object.
     *
     * @param expected the expected JSON object
     * @param actual   the actual JSON object
     * @return the result of the comparison
     * @throws JSONException JSON parsing error
     */
    JSONCompareDetailResult compareJSON(JSONObject expected, JSONObject actual) throws JSONException;

    /**
     * Compares two {@link JSONArray}s and returns the result of the comparison in a {@link JSONCompareDetailResult} object.
     *
     * @param expected the expected JSON array
     * @param actual   the actual JSON array
     * @return the result of the comparison
     * @throws JSONException JSON parsing error
     */
    JSONCompareDetailResult compareJSON(JSONArray expected, JSONArray actual) throws JSONException;

    /**
     * Compares two {@link JSONObject}s on the provided path represented by {@code prefix} and
     * updates the result of the comparison in the {@code result} {@link JSONCompareDetailResult} object.
     *
     * @param prefix   the path in the json where the comparison happens
     * @param expected the expected JSON object
     * @param actual   the actual JSON object
     * @param result   stores the actual state of the comparison result
     * @throws JSONException JSON parsing error
     */
    void compareJSON(String prefix, JSONObject expected, JSONObject actual, JSONCompareDetailResult result) throws JSONException;

    /**
     * Compares two {@link Object}s on the provided path represented by {@code prefix} and
     * updates the result of the comparison in the {@code result} {@link JSONCompareDetailResult} object.
     *
     * @param prefix        the path in the json where the comparison happens
     * @param expectedValue the expected value
     * @param actualValue   the actual value
     * @param result        stores the actual state of the comparison result
     * @throws JSONException JSON parsing error
     */
    void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareDetailResult result) throws JSONException;

    /**
     * Compares two {@link JSONArray}s on the provided path represented by {@code prefix} and
     * updates the result of the comparison in the {@code result} {@link JSONCompareDetailResult} object.
     *
     * @param prefix   the path in the json where the comparison happens
     * @param expected the expected JSON array
     * @param actual   the actual JSON array
     * @param result   stores the actual state of the comparison result
     * @throws JSONException JSON parsing error
     */
    void compareJSONArray(String prefix, JSONArray expected, JSONArray actual, JSONCompareDetailResult result) throws JSONException;

    /**
     * Compares two {@link JSONArray}s on the provided path represented by {@code prefix} and
     * updates the result of the comparison in the {@code result} {@link JSONCompareDetailResult} object.
     *
     * @param mode     define different behavior for the comparison of JSON
     * @param prefix   the path in the json where the comparison happens
     * @param expected the expected JSON array
     * @param actual   the actual JSON array
     * @param result   stores the actual state of the comparison result
     * @throws JSONException JSON parsing error
     */
    void compareJSONArray(CompareContext mode, String prefix, JSONArray expected, JSONArray actual, JSONCompareDetailResult result)
            throws JSONException;

    /**
     * Mark missing field of expected {@link Object}s on the provided path represented by {@code
     * prefix} and
     * updates the result of the comparison in the {@code result} {@link JSONCompareDetailResult} object.
     *
     * @param prefix   the path in the json where the comparison happens
     * @param expected the expected element
     * @param result   stores the actual state of the comparison result
     */
    void markMissing(String prefix, Object expected, JSONCompareDetailResult result);

    /**
     * Mark unexpected field of actual {@link Object}s on the provided path represented by {@code
     * prefix} and
     * updates the result of the comparison in the {@code result} {@link JSONCompareDetailResult} object.
     *
     * @param prefix the path in the json where the comparison happens
     * @param actual the expected element
     * @param result stores the actual state of the comparison result
     */
    void markUnexpected(String prefix, Object actual, JSONCompareDetailResult result);

    /**
     * Mark the result of the comparison in the {@code result} {@link JSONCompareDetailResult} if
     * it should have completed comparison.
     *
     * @param result stores the actual state of the comparison result
     */
    void markResultFeature(JSONCompareDetailResult result);
}
