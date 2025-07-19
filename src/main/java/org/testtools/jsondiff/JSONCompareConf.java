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

import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for JSON comparison rules loaded from YAML.
 * Handles parsing YAML configuration and converting it to CompareRule objects.
 */
public class JSONCompareConf {
    
    private final List<Map<String, Object>> yamlData = new ArrayList<>();
    private final List<CompareRule> compareRules = new ArrayList<>();

    /**
     * Gets the parsed YAML data.
     * 
     * @return List of parsed YAML data as maps
     */
    public List<Map<String, Object>> getYamlData() {
        return new ArrayList<>(yamlData);
    }

    /**
     * Gets the converted comparison rules.
     * 
     * @return List of CompareRule objects
     */
    public List<CompareRule> getCompareRules() {
        return new ArrayList<>(compareRules);
    }

    /**
     * Reads and parses YAML configuration from a string.
     * 
     * @param yamlString The YAML configuration string
     * @throws IllegalArgumentException if the YAML string is null or empty
     * @throws RuntimeException if YAML parsing fails
     */
    public void readNodeFromYaml(String yamlString) {
        validateYamlString(yamlString);
        
        try {
            Yaml yaml = new Yaml();
            List<Map<String, Object>> parsedData = yaml.load(yamlString);
            
            if (parsedData == null) {
                throw new RuntimeException("YAML parsing resulted in null data");
            }
            
            yamlData.clear();
            yamlData.addAll(parsedData);
            
            convertToCompareRules();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse or process YAML string", e);
        }
    }

    /**
     * Converts YAML data to CompareRule objects.
     * This method processes both direct rules and sub-rules.
     */
    public void convertToCompareRules() {
        compareRules.clear();
        
        for (Map<String, Object> rule : yamlData) {
            if (rule == null) {
                continue;
            }
            
            // Handle sub-rule case
            if (rule.containsKey("subRule")) {
                Map<String, Object> subRule = (Map<String, Object>) rule.get("subRule");
                if (subRule != null) {
                    compareRules.add(new CompareRule(subRule));
                }
            } else {
                // Handle direct rule case
                compareRules.add(new CompareRule(rule));
            }
        }
    }

    /**
     * Validates that the YAML string is not null or empty.
     * 
     * @param yamlString The YAML string to validate
     * @throws IllegalArgumentException if the string is null or empty
     */
    private void validateYamlString(String yamlString) {
        if (yamlString == null || yamlString.trim().isEmpty()) {
            throw new IllegalArgumentException("YAML string cannot be null or empty");
        }
    }
}
