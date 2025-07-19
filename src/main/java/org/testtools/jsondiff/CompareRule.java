package org.testtools.jsondiff;

import org.testtools.jsondiff.constant.Param;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class CompareRule {
    private final CompareContext compareContext;
    private String jsonPath;

    private List<PreProcessItem> preProcesses;
    private List<CompareMatcherItem> customRules;

    public CompareRule(Map<String, Object> rule) {
        // Handle jsonPath with null safety
        Object jsonPathObj = rule.get(Param.JSON_PATH_KEY);
        this.jsonPath = jsonPathObj != null ? String.valueOf(jsonPathObj) : null;
        
        // Provide default values for Boolean fields to prevent NullPointerException
        boolean extensible = getBooleanValue(rule, Param.EXTENSIBLE_KEY, true);
        boolean strictOrder = getBooleanValue(rule, Param.STRICT_ORDER, true);
        boolean fastFail = getBooleanValue(rule, Param.FAST_FAIL_KEY, false);
        boolean ignoreNull = getBooleanValue(rule, Param.IGNORE_NULL_KEY, false);
        
        this.compareContext = new CompareContext(extensible, strictOrder, ignoreNull, fastFail);

        List<Map<String, Object>> customRulesMaps = (List<Map<String, Object>>) rule.get(Param.CUSTOM_RULES_KEY);
        // Convert customRulesMap to CompareMatcherItem
        if (customRulesMaps != null) {
            this.customRules = customRulesMaps.stream()
                    .map(ruleMap -> new CompareMatcherItem(
                            getStringValue(ruleMap, Param.NAME_KEY, ""),
                            getStringValue(ruleMap, Param.JSON_PATH_KEY, ""),
                            getStringValue(ruleMap, Param.PARAM_KEY, "")))
                    .collect(Collectors.toList());
        } else {
            this.customRules = new ArrayList<>();
        }
    }

    /**
     * Safely extracts a Boolean value from a map with a default fallback.
     * 
     * @param map the map to extract from
     * @param key the key to look for
     * @param defaultValue the default value if the key is not found or value is null
     * @return the Boolean value or the default value
     */
    private boolean getBooleanValue(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    /**
     * Safely extracts a String value from a map with a default fallback.
     * 
     * @param map the map to extract from
     * @param key the key to look for
     * @param defaultValue the default value if the key is not found or value is null
     * @return the String value or the default value
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    public CompareContext getCompareContext() {
        return compareContext;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public List<PreProcessItem> getPreProcesses() {
        return preProcesses;
    }

    public void setPreProcesses(List<PreProcessItem> preProcesses) {
        this.preProcesses = preProcesses;
    }

    public List<CompareMatcherItem> getCustomRules() {
        return customRules;
    }

    public void setCustomRules(List<CompareMatcherItem> customRules) {
        this.customRules = customRules;
    }
}
