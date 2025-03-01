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

package com.nezha.jsondiff;

import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSONCompareConf {
    List<Map<String, Object>> yamlData = new ArrayList<Map<String, Object>>();
    List<CompareRule> compareRules = new ArrayList<CompareRule>();

    private static void processSubRule(Map<String, Object> subRule) {
        String forPath = (String) subRule.get("forPath");
        String type = (String) subRule.get("type");
        List<Map<String, Object>> customRules = (List<Map<String, Object>>) subRule.get("customRules");
        Map<String, Object> preProcess = (Map<String, Object>) subRule.get("preProcess");
        Map<String, Object> postProcess = (Map<String, Object>) subRule.get("postProcess");

        System.out.println("forPath: " + forPath);
        System.out.println("type: " + type);

        System.out.println("Custom Rules:");
        for (Map<String, Object> rule : customRules) {
            for (Map.Entry<String, Object> entry : rule.entrySet()) {
                System.out.println("  Rule Type: " + entry.getKey());
                Map<String, Object> ruleDetails = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Object> detail : ruleDetails.entrySet()) {
                    System.out.println("    " + detail.getKey() + ": " + detail.getValue());
                }
            }
        }

        System.out.println("Pre Process:");
        for (Map.Entry<String, Object> entry : preProcess.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("Post Process:");
        for (Map.Entry<String, Object> entry : postProcess.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }

    public List<Map<String, Object>> getYamlData() {
        return yamlData;
    }

    public void readNodeFromYaml(String yamlString) throws Exception {
        // 验证 YAML 字符串是否为空
        if (yamlString == null || yamlString.trim().isEmpty()) {
            throw new IllegalArgumentException("YAML string cannot be null or empty");
        }

        try {
            Yaml yaml = new Yaml();
            this.yamlData = yaml.load(yamlString);
            for (Map<String, Object> rule : this.yamlData) {
                if (rule.containsKey("subRule")) {
                    Map<String, Object> subRule = (Map<String, Object>) rule.get("subRule");
                    compareRules.add(new CompareRule(subRule));
                }

            }

        } catch (Exception e) {
            throw new Exception("Failed to parse or process YAML string", e);
        }
    }

    public void convertToCompareMatcher() {
        for (Map<String, Object> rule : yamlData) {
            compareRules.add(new CompareRule(rule));
        }
    }

    public List<CompareRule> getCompareRules() {
        return compareRules;
    }


}
