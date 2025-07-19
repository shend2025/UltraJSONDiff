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

import org.testtools.jsondiff.JSONCompareDetailResult;
import org.testtools.jsondiff.JSONParser;
import org.testtools.jsondiff.CompareContext;
import org.testtools.jsondiff.comparator.JSONComparator;
import org.json.JSONException;

import java.text.MessageFormat;

/**
 * <p>A value matcher for arrays of JsonObjects. This operates like
 * AbstractComparator.compareJSONArrayOfJsonObjects, of which the
 * unique key provided by member variables</p>*
 */
public class EscapedJsonMatcher<T> implements CustomValueMatcher<T> {

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
        String newPrefix = MessageFormat.format("{0}.", prefix);
        String expectedStr = String.valueOf(expected);
        String actualStr = String.valueOf(actual);
        try {
            Object expectedJSON = JSONParser.parseEscapedJSON(expectedStr);
            Object actualJSON = JSONParser.parseEscapedJSON(actualStr);

            comparator.compareValues(newPrefix, expectedJSON, actualJSON, result);

            // any failures have already been passed to result, so return true
            return true;
        }
        catch (JSONException e) {
            return expectedStr.equals(actualStr);
        }
    }

    @Override
    public void matcherInit(String param, CompareContext compareContext) {
    }

}
