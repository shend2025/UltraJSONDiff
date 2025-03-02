/*
 * LenientValueMatcher.java
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
import org.testtools.jsondiff.comparator.DefaultComparator;
import org.testtools.jsondiff.comparator.JSONComparator;
import org.json.JSONException;

/**
 * Function: 
 *
 *  
 */
public class ComparatorValueMatcher<T> implements
        LocationAwareValueMatcher<T> {
    private JSONComparator comparator;

    ComparatorValueMatcher() {
        CompareContext mode = new CompareContext(true, true, false, false);
        comparator = new DefaultComparator(mode);
    }

    ComparatorValueMatcher(CompareContext mode) {
        comparator = new DefaultComparator(mode);
    }


    ComparatorValueMatcher(JSONComparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean equal(T o1, T o2) {
        return false;
    }

    @Override
    public boolean equal(String prefix, T actual, T expected,
            JSONCompareDetailResult result)
            throws ValueMatcherException {
        try {
            comparator.compareValues(prefix, actual, expected, result);
            return true;
        }catch (JSONException e) {
            return false;
        }
    }
    @Override
    public void matcherInit(String param, CompareContext compareContext) {

    }
}
