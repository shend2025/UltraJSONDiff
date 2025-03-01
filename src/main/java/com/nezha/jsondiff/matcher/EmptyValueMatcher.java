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
 *  
 */

import com.nezha.jsondiff.CompareContext;
import com.nezha.jsondiff.comparator.JSONCompareUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * <p>A value matcher for arrays of JsonObjects. This operates like
 * AbstractComparator.compareJSONArrayOfJsonObjects</p>*
 */
public class EmptyValueMatcher<T> implements ValueMatcher<T> {
    /**
     * Create EmptyValueValueMatcher to match every empty elements,
     * eg. empty array, empty object, null, empty String
     *
     */
    public EmptyValueMatcher() {}

    @Override
    public boolean equal(T actual, T expected) {

        return isEmpty(actual) && isEmpty(expected);
    }

    private boolean isEmpty(T value) {
        if (value instanceof JSONArray && ((JSONArray) value).length() == 0) {
            return true;
        }

        if (value instanceof JSONObject && value.toString().equals("{}")) {
            return true;
        }

        if (value instanceof String && ((String) value).isEmpty()) {
            return true;
        }

        return JSONCompareUtil.getIfNull(value) == null;
    }

    @Override
    public void matcherInit(String param, CompareContext compareContext) {

    }

}
