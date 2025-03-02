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
package org.testtools.jsondiff;

public class FailureField {
    private final Object expected;
    private final Object actual;
    private final String diffKey;
    private final String reason;

    public FailureField(Object expected, Object actual, String diffKey, String reason) {
        this.expected = expected;
        this.actual = actual;
        this.diffKey = diffKey;
        this.reason = reason;
    }

    public Object getExpected() {
        return expected;
    }

    public Object getActual() {
        return actual;
    }

    public String getDiffKey() {
        return diffKey;
    }

    public String getReason() {
        return reason;
    }
}
