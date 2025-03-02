/*
 * ImprecisePositionMatcher.java
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
 * Function: Imprecise position comparison (front-end model data)
 * position the value is in the form of a string，eg： {"position": "-300.0,-250.0"}
 */
public class ImprecisePositionMatcher <T> implements ValueMatcher<T> {
    private double tolerance;
    private String separator;

    public ImprecisePositionMatcher() { this (10e-3, ","); }

    public ImprecisePositionMatcher(double tolerance) {
        this (tolerance, ",");
    }

    public ImprecisePositionMatcher(double tolerance, String seperator) {
        this.tolerance = tolerance;
        this.separator = seperator;
    }

    @Override
    public boolean equal(T actual, T expected){
        return compare(actual.toString(), expected.toString(), separator);
    }

    private boolean compare(String actual, String expected, String sep) {
        if (sep.isEmpty()) return compareNums(actual, expected);

        String curSep = sep.substring(0, 1);
        String[] actualArr = actual.split(curSep);
        String[] expectedArr = expected.split(curSep);

        if (expectedArr.length != actualArr.length ){
            return false;
        }

        for (int i = 0; i < actualArr.length; i++) {
            if (!compare(actualArr[i], expectedArr[i], sep.substring(1))) return false;
        }

        return true;

    }

    private boolean compareNums(String actual, String expected) {
        try {
            BigDecimal actualNum = new BigDecimal(actual);
            BigDecimal expectedNum = new BigDecimal(expected);
            BigDecimal diffValue = actualNum.subtract(expectedNum).abs();
            if (diffValue.compareTo(BigDecimal.valueOf(tolerance)) == 1 ){
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return actual.equals(expected);
        }
    }

    @Override
    public void matcherInit(String param, CompareContext compareContext) {
        //format tolerance=1e-3;separator=,
        tolerance = Double.parseDouble(Objects.requireNonNull(JSONCompareUtil.getParamValue(
                param.replaceAll(" ", "").trim().split(";")[0])));

        separator = Objects.requireNonNull(JSONCompareUtil.getParamValue(
                param.replaceAll(" ", "").trim().split(";")[1]));
    }
}
