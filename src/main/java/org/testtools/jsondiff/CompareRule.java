package org.testtools.jsondiff;

import org.testtools.jsondiff.constant.Param;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CompareRule {
    private final CompareContext compareContext;
    private String jsonPath;

    private List<PreProcessItem> preProcesses;
    private List<CompareMatcherItem> customRules;


    public CompareRule(Map<String, Object> rule) {
        // Default constructor
        this.jsonPath = (String) rule.get(Param.JSON_PATH_KEY);
        boolean extensible = (Boolean) rule.get(Param.EXTENSIBLE_KEY);
        boolean strictOrder = (Boolean) rule.get(Param.STRICT_ORDER);
        boolean fastFail = (Boolean) rule.get(Param.FAST_FAIL_KEY);
        boolean ignoreNull = (Boolean) rule.get(Param.IGNORE_NULL_KEY);
        this.compareContext = new CompareContext(extensible, strictOrder, ignoreNull, fastFail);

//        Map<String, Object> preProcess = (Map<String, Object>) rule.get(Param.PRE_PROCESS_KEY);
        List<Map<String, Object>> customRulesMaps = (List<Map<String, Object>>) rule.get(Param.CUSTOM_RULES_KEY);
        //conver customRulesMap to CompareMatcherItem
        this.customRules = customRulesMaps.stream()
                .map(ruleMap -> {
                    return new CompareMatcherItem((String) ruleMap.get(Param.NAME_KEY),
                            (String) ruleMap.get(Param.JSON_PATH_KEY),
                            (String) ruleMap.get(Param.PARAM_KEY));
                })
                .collect(Collectors.toList());
    }

    public CompareContext getCompareContext() {
        return this.compareContext;
    }

    public String getJsonPath() {
        return this.jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    // Getter and Setter for preProcesses
    public List<PreProcessItem> getPreProcesses() {
        return preProcesses;
    }

    public void setPreProcesses(List<PreProcessItem> preProcesses) {
        this.preProcesses = preProcesses;
    }

    // Getter and Setter for customRules
    public List<CompareMatcherItem> getCustomRules() {
        return customRules;
    }

    public void setCustomRules(List<CompareMatcherItem> customRules) {
        this.customRules = customRules;
    }
}
