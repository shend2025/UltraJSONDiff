/*
 * ImpreciseAngleValueMatcher.java
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

import com.nezha.jsondiff.CompareContext;
import com.nezha.jsondiff.comparator.JSONCompareUtil;

import java.math.BigDecimal;
import java.util.Objects;


public class DegreePreciseMatcher<T> implements ValueMatcher<T> {
    private double tolerance;

    public DegreePreciseMatcher() { this (10e-5); }

    public DegreePreciseMatcher(double tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public boolean equal(T actual, T expected){
        try {
            BigDecimal actualNum = new BigDecimal(actual.toString());
            BigDecimal expectedNum = new BigDecimal(expected.toString());

            if (actualNum.doubleValue() > 360 || actualNum.doubleValue() < -360
                    || expectedNum.doubleValue() >360 || expectedNum.doubleValue() <-360){
                return false;
            }

            Double diffAngle = (expectedNum.doubleValue() + 180) % 180 - (actualNum.doubleValue() + 180) % 180;

            return Math.abs(diffAngle) <= tolerance;
        } catch (NumberFormatException e) {
            return actual.equals(expected);
        }
    }

    @Override
    public void matcherInit(String param, CompareContext compareContext) {
        this.tolerance =
                Double.parseDouble(Objects.requireNonNull(JSONCompareUtil.getParamValue(param)));
    }

}

