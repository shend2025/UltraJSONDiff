/*
 * TolerantValueMatcher.java
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
import org.testtools.jsondiff.comparator.JSONCompareUtil;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Function: tolerance matching
 */
public class TolerantValueMatcher<T> implements ValueMatcher<T> {
    private double tolerance;

    public TolerantValueMatcher() { this (10e-5); }

    public TolerantValueMatcher(double tolerance) { this.tolerance = tolerance; }

    @Override
    public boolean equal(T actual, T expected){
        try {
            BigDecimal actualNum = new BigDecimal(actual.toString());
            BigDecimal expectedNum = new BigDecimal(expected.toString());
            BigDecimal diffValue = actualNum.subtract(expectedNum).abs();

            return diffValue.compareTo(BigDecimal.valueOf(tolerance)) != 1;
        } catch (NumberFormatException e) {
            return actual.equals(expected);
        }
    }
    
    @Override
    public void matcherInit(String param, CompareContext compareContext) {
        tolerance = Double.parseDouble(Objects.requireNonNull(JSONCompareUtil.getParamValue(param)));
    }
}
