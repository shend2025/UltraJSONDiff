/*
 * JSONCompareDeepDetailResult.java
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

public class JSONCompareDeepDetailResult extends JSONCompareDetailResult {

    private final List<String> actualFailureFields = new ArrayList<>();
    private final List<String> expectedFailureFields = new ArrayList<>();
    private final List<String> unexpectedFields = new ArrayList<>();
    private final List<String> missingFields = new ArrayList<>();

    public JSONCompareDeepDetailResult() {
        super();
    }

    public JSONCompareDeepDetailResult(JSONCompareDetailResult o) {
        super(o);
    }

    public List<String> getActualFailureFields() {
        return actualFailureFields;
    }

    public List<String> getExpectedFailureFields() {
        return expectedFailureFields;
    }

    public List<String> getUnexpectedFields() {
        return unexpectedFields;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void addActualFail(String prefix) {
        addToList(actualFailureFields, prefix);
    }

    public void addExpectedFail(String prefix) {
        addToList(expectedFailureFields, prefix);
    }

    public void addMissing(String prefix) {
        addToList(missingFields, prefix);
    }

    public void addUnexpected(String prefix) {
        addToList(unexpectedFields, prefix);
    }

    private void addToList(List<String> list, String s) {
        if (s == null) return;
        list.add(s);
    }
}
