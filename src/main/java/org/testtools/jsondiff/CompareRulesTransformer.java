/*
 * CompareRulesTranformer.java
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testtools.jsondiff.comparator.CustomComparator;
import org.testtools.jsondiff.matcher.ValueMatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transformer for converting comparison rules to comparators.
 * Provides utility methods for creating CustomComparator instances from various rule formats.
 */
public final class CompareRulesTransformer {
    
    private static final Logger LOGGER = Logger.getLogger(CompareRulesTransformer.class.getName());
    private static final String MATCHER_PACKAGE = "org.testtools.jsondiff.matcher.";
    private static final String MATCHER_SUFFIX = "Matcher";
    private static final String MATCHER_INIT_METHOD = "matcherInit";
    
    // Cache for reflection operations to improve performance
    private static final ConcurrentHashMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    
    // Reusable ObjectMapper instance
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CompareRulesTransformer() {
        // Utility class, prevent instantiation
    }

    /**
     * Converts a JSON string representation of comparison rules to a CompareRules object.
     *
     * @param rulesInJsonStr JSON string containing comparison rules
     * @return CompareRules object, or empty CompareRules if parsing fails
     * @throws IllegalArgumentException if rulesInJsonStr is null
     */
    public static CompareRules stringToCompareRules(final String rulesInJsonStr) {
        if (rulesInJsonStr == null) {
            throw new IllegalArgumentException("Rules JSON string cannot be null");
        }
        
        try {
            return OBJECT_MAPPER.readValue(rulesInJsonStr, CompareRules.class);
        } catch (final JsonProcessingException e) {
            LOGGER.log(Level.WARNING, "Failed to parse rules JSON string: {0}", e.getMessage());
            return new CompareRules();
        }
    }

    /**
     * Creates a CustomComparator from a CompareRule.
     *
     * @param compareRule the comparison rule to convert
     * @return CustomComparator instance
     * @throws IllegalArgumentException if compareRule is null
     * @throws RuntimeException if comparator creation fails
     */
    public static CustomComparator getComparator(final CompareRule compareRule) {
        if (compareRule == null) {
            throw new IllegalArgumentException("CompareRule cannot be null");
        }
        
        try {
            final List<Customization> customizations = getCustomizations(compareRule);
            return new CustomComparator(compareRule.getCompareContext(),
                    customizations.toArray(new Customization[0]));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create comparator from CompareRule", e);
        }
    }

    /**
     * Creates a CustomComparator from a JSON string representation of comparison rules.
     *
     * @param compareRules JSON string containing comparison rules
     * @return CustomComparator instance
     * @throws IllegalArgumentException if compareRules is null or empty
     * @throws RuntimeException if comparator creation fails
     */
    public static CustomComparator getComparator(final String compareRules) {
        if (compareRules == null || compareRules.trim().isEmpty()) {
            throw new IllegalArgumentException("Compare rules string cannot be null or empty");
        }
        
        try {
            CompareRules rules = stringToCompareRules(compareRules);
            List<CustomCompareRule> customRules = rules.getCustomRules();
            
            if (customRules.isEmpty()) {
                // If no custom rules, create a default comparator
                CompareContext defaultContext = new CompareContext(true, true, false, false);
                return new CustomComparator(defaultContext);
            }
            
            // Convert the first custom rule to a CompareRule format
            return getComparator(convertToCompareRule(customRules.get(0)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create comparator from rules string", e);
        }
    }

    /**
     * Converts a CompareRule to a list of Customization objects.
     *
     * @param compareRule the comparison rule to convert
     * @return List of Customization objects
     * @throws ClassNotFoundException if matcher class cannot be found
     * @throws NoSuchMethodException if matcherInit method cannot be found
     * @throws InvocationTargetException if matcherInit method invocation fails
     * @throws InstantiationException if matcher instantiation fails
     * @throws IllegalAccessException if matcher method access is denied
     */
    private static List<Customization> getCustomizations(final CompareRule compareRule) 
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, 
                   InstantiationException, IllegalAccessException {
        
        if (compareRule.getCustomRules() == null || compareRule.getCustomRules().isEmpty()) {
            return new ArrayList<>();
        }
        
        final List<Customization> customizationList = new ArrayList<>();
        final CompareContext compareContext = compareRule.getCompareContext();
        
        for (CompareMatcherItem rule : compareRule.getCustomRules()) {
            try {
                ValueMatcher<Object> matcher = createMatcher(rule, compareContext);
                customizationList.add(new Customization(rule.getJsonPath(), matcher));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create matcher for rule {0}: {1}", 
                          new Object[]{rule.getName(), e.getMessage()});
                // Continue with other rules instead of failing completely
            }
        }
        
        return customizationList;
    }

    /**
     * Creates a ValueMatcher instance for the given rule.
     *
     * @param rule the matcher rule
     * @param compareContext the comparison context
     * @return ValueMatcher instance
     * @throws ClassNotFoundException if matcher class cannot be found
     * @throws NoSuchMethodException if matcherInit method cannot be found
     * @throws InvocationTargetException if matcherInit method invocation fails
     * @throws InstantiationException if matcher instantiation fails
     * @throws IllegalAccessException if matcher method access is denied
     */
    private static ValueMatcher<Object> createMatcher(CompareMatcherItem rule, CompareContext compareContext)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                   InstantiationException, IllegalAccessException {
        
        String ruleName = rule.getName();
        String param = rule.getParam();
        
        if (ruleName == null || ruleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule name cannot be null or empty");
        }
        
        // Ensure param is not null
        if (param == null) {
            param = "";
        }
        
        String matcherClassName = MATCHER_PACKAGE + ruleName + MATCHER_SUFFIX;
        
        // Get or create class from cache
        Class<?> clazz = CLASS_CACHE.computeIfAbsent(matcherClassName, className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Matcher class not found: " + className, e);
            }
        });
        
        // Get or create constructor from cache
        Constructor<?> constructor = CONSTRUCTOR_CACHE.computeIfAbsent(matcherClassName, className -> {
            try {
                return clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("No default constructor found for: " + className, e);
            }
        });
        
        // Create matcher instance
        Object matcher = constructor.newInstance();
        
        // Get or create matcherInit method from cache
        Method matcherInitMethod = METHOD_CACHE.computeIfAbsent(matcherClassName, className -> {
            try {
                return clazz.getMethod(MATCHER_INIT_METHOD, String.class, CompareContext.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("matcherInit method not found for: " + className, e);
            }
        });
        
        // Initialize matcher
        matcherInitMethod.invoke(matcher, param, compareContext);
        
        return (ValueMatcher<Object>) matcher;
    }

    /**
     * Converts a CustomCompareRule to a CompareRule.
     * This method creates a CompareRule with default context and converts the matcher array
     * to CompareMatcherItem objects.
     *
     * @param customRule the custom compare rule to convert
     * @return CompareRule instance
     */
    private static CompareRule convertToCompareRule(CustomCompareRule customRule) {
        if (customRule == null) {
            throw new IllegalArgumentException("CustomCompareRule cannot be null");
        }
        
        // Create a default CompareContext
        CompareContext compareContext = new CompareContext(true, true, false, false);
        
        // Convert matcher array to CompareMatcherItem list
        List<CompareMatcherItem> compareMatcherItems = new ArrayList<>();
        if (customRule.getMatcher() != null) {
            for (String matcherName : customRule.getMatcher()) {
                if (matcherName != null && !matcherName.trim().isEmpty()) {
                    // Create a CompareMatcherItem with the matcher name as the rule name
                    // and the path from the custom rule
                    CompareMatcherItem item = new CompareMatcherItem(
                        matcherName.trim(),
                        customRule.getPath(),
                        "" // Default empty parameter
                    );
                    compareMatcherItems.add(item);
                }
            }
        }
        
        // Create a temporary map to simulate the rule structure
        // This is a workaround since CompareRule constructor expects a Map
        // In a real implementation, you might want to add a constructor to CompareRule
        // that accepts individual parameters
        java.util.Map<String, Object> ruleMap = new java.util.HashMap<>();
        ruleMap.put(org.testtools.jsondiff.constant.Param.JSON_PATH_KEY, customRule.getPath());
        ruleMap.put(org.testtools.jsondiff.constant.Param.EXTENSIBLE_KEY, true);
        ruleMap.put(org.testtools.jsondiff.constant.Param.STRICT_ORDER, true);
        ruleMap.put(org.testtools.jsondiff.constant.Param.FAST_FAIL_KEY, false);
        ruleMap.put(org.testtools.jsondiff.constant.Param.IGNORE_NULL_KEY, false);
        
        // Convert CompareMatcherItems to the format expected by CompareRule constructor
        List<java.util.Map<String, Object>> customRulesMaps = new ArrayList<>();
        for (CompareMatcherItem item : compareMatcherItems) {
            java.util.Map<String, Object> ruleItemMap = new java.util.HashMap<>();
            ruleItemMap.put(org.testtools.jsondiff.constant.Param.NAME_KEY, item.getName());
            ruleItemMap.put(org.testtools.jsondiff.constant.Param.JSON_PATH_KEY, item.getJsonPath());
            ruleItemMap.put(org.testtools.jsondiff.constant.Param.PARAM_KEY, item.getParam());
            customRulesMaps.add(ruleItemMap);
        }
        ruleMap.put(org.testtools.jsondiff.constant.Param.CUSTOM_RULES_KEY, customRulesMaps);
        
        return new CompareRule(ruleMap);
    }
}

