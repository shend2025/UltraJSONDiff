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

/**
 * Function:
 *  
 */

import org.testtools.jsondiff.CompareContext;
import org.testtools.jsondiff.JSONCompareDetailResult;
import org.testtools.jsondiff.comparator.JSONComparator;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * <p>A value matcher for arrays of JsonObjects. This operates like
 * AbstractComparator.compareJSONArrayOfJsonObjects</p>*
 */
public class ArrayDisorderMatcher<T> implements CustomValueMatcher<T> {
    /**
     * Create ArrayDisorderMatcher to match every JsonObject by order
     *
     */
    public ArrayDisorderMatcher() {}

    @Override
    /*
     * NOTE: method defined as required by ValueMatcher interface but will never
     * be called so defined simply to indicate match failure
     */
    public boolean equal(T o1, T o2) {
        return false;
    }

    public  void matcherInit(String param, CompareContext compareContext) {}

    @Override
    public boolean equal(String prefix, T actual, T expected, JSONCompareDetailResult result, JSONComparator comparator) {
        if (!(actual instanceof JSONArray)) {
            throw new IllegalArgumentException("ArrayObjectValueMatcher applied to non-array actual value");
        }
        try {
            JSONArray actualArray = (JSONArray) actual;
            JSONArray expectedArray = expected instanceof JSONArray ? (JSONArray) expected: new JSONArray(new Object[] { expected });
            CompareContext mode = new CompareContext(false,false, false, false);
            comparator.compareJSONArray(mode, prefix, expectedArray,
                    actualArray, result);

            // any failures have already been passed to result, so return true
            return true;
        }
        catch (JSONException e) {
            return false;
        }
    }

}
