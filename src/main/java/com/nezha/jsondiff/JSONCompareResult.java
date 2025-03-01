/*
 * JSONCompareSimpleResult.java
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

package com.nezha.jsondiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Function: compare results in simple mode
 */
public class JSONCompareResult {

    private final List<FailureField> failures = new ArrayList<>();

    public JSONCompareResult() {
    }

    public List<FailureField> getFailure() {
        return failures;
    }

    public void addFailure(FailureField f) {
        failures.add(f);
    }

    public void addFailures(List<FailureField> newFailures) {
        failures.addAll(newFailures);
    }
}
