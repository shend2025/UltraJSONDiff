/*
 * CompareRules.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompareRules {
    private final String defaultMode;
    private final CustomCompareRule[] customRules;

    public CompareRules() {
        this("STRICT_ORDER", new ArrayList<CustomCompareRule>());
    }

    public CompareRules(String defaultMode) {
        this(defaultMode, new ArrayList<CustomCompareRule>());
    }

    public CompareRules(ArrayList<CustomCompareRule> customRules) {
        this("STRICT_ORDER", customRules);
    }

    public CompareRules(String defaultMode, List<CustomCompareRule> customRules) {
        this.defaultMode = defaultMode;
        this.customRules = customRules.toArray(new CustomCompareRule[0]);
    }

    public CompareRules(String defaultMode, CustomCompareRule[] customRules) {
        this.defaultMode = defaultMode;
        this.customRules = customRules;
    }

    public String getDefaultMode() {
        return defaultMode;
    }

    public List<CustomCompareRule> getCustomRules() {
        List<CustomCompareRule> rules = new ArrayList<CustomCompareRule>();
        Collections.addAll(rules, customRules);
        return rules;
    }
}
