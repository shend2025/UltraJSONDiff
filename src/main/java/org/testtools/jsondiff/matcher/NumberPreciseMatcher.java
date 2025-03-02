/*
 * NumberPreciseMatcher.java
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
import java.math.RoundingMode;
import java.util.Objects;


public class NumberPreciseMatcher<T> implements ValueMatcher<T>{
    private int newScale;
    private int roundingMode;

    public NumberPreciseMatcher() {
        this(6, 4);
    }

    public NumberPreciseMatcher(int newScale) {
        this(newScale, 4);
    }

    public NumberPreciseMatcher(int newScale, int roundingMode) {
        this.newScale = newScale;
        this.roundingMode = roundingMode;
    }

    @Override
    public boolean equal(T actual, T expected) {
        try {
            BigDecimal actualNum = new BigDecimal(actual.toString());
            BigDecimal expectedNum = new BigDecimal(expected.toString());

            BigDecimal actualNumNew = actualNum.setScale(newScale, RoundingMode.valueOf(roundingMode));
            BigDecimal expectedNumNew = expectedNum.setScale(newScale,
                    RoundingMode.valueOf(roundingMode));

            return actualNumNew.equals(expectedNumNew);
        } catch (NumberFormatException e) {
            return actual.equals(expected);
        }
    }

    @Override
    public void matcherInit(String param, CompareContext compareContext) {
        newScale = Integer.parseInt(Objects.requireNonNull(JSONCompareUtil.getParamValue(
                param.replaceAll(" ", "").trim().split(",")[0])));

        roundingMode = Integer.parseInt(Objects.requireNonNull(JSONCompareUtil.getParamValue(
                param.replaceAll(" ", "").trim().split(",")[1])));
    }
}
