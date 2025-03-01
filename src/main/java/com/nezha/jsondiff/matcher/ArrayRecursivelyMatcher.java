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

package com.nezha.jsondiff.matcher;

/**
 * Function:
 *  
 */

import com.nezha.jsondiff.JSONCompareDetailResult;
import com.nezha.jsondiff.CompareContext;
import com.nezha.jsondiff.comparator.JSONComparator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>A value matcher for arrays of JsonObjects. This operates like
 * AbstractComparator.compareJSONArrayOfJsonObjects</p>*
 */
public class ArrayRecursivelyMatcher<T> implements CustomValueMatcher<T> {

    private boolean extensible;

    public ArrayRecursivelyMatcher() {
        this(false);
    }

    public ArrayRecursivelyMatcher(boolean extensible) {
        this.extensible = extensible;
    }

    public ArrayRecursivelyMatcher(String extensible) {
        this.extensible = extensible.equals("true");
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

            Set<Integer> matched = new HashSet<Integer>();
            for (int i = 0; i < expectedArray.length(); ++i) {
                if (result.quickFail()) return true;
                Object expectedArrayElement = expectedArray.get(i);
                boolean matchFound = false;
                for (int j = 0; j < actualArray.length(); ++j) {
                    Object actualArrayElement = actualArray.get(j);
                    if (matched.contains(j) || !actualArrayElement.getClass().equals(expectedArrayElement.getClass())) {
                        continue;
                    }
                    if (expectedArrayElement instanceof JSONObject) {
                        if (comparator.compareJSON((JSONObject) expectedArrayElement,
                                (JSONObject) actualArrayElement).passed()) {
                            matched.add(j);
                            matchFound = true;
                            break;
                        }
                    } else if (expectedArrayElement instanceof JSONArray) {
                        if (comparator.compareJSON((JSONArray) expectedArrayElement,
                                (JSONArray) actualArrayElement).passed()) {
                            matched.add(j);
                            matchFound = true;
                            break;
                        }
                    } else if (expectedArrayElement.equals(actualArrayElement)) {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {

//                result.fail(key + "[" + i + "] Could not find match for element " + expectedArrayElement);
                    result.missing(prefix + "[" + i + "]", expectedArrayElement);
                }
            }

            // If it's loose mode, you don't need to record the extra elements in the actualArray
            if (extensible) return true;

            // not match actualArray，as unexpectedArray
            for(int j = 0; j < actualArray.length(); ++j) {
                if (result.quickFail()) return true;
                Object actualArrayElement = actualArray.get(j);
                if (matched.contains(j)) {
                    continue;
                }
                result.unexpected(prefix + "[" + j + "]", actualArrayElement);
            }

            // any failures have already been passed to result, so return true
            return true;
        }
        catch (JSONException e) {
            return false;
        }
    }

    @Override
    public void matcherInit(String param, CompareContext compareContext) {
        this.extensible = compareContext.getExtensible();
    }

}
