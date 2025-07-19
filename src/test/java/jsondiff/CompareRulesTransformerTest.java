package jsondiff;

import org.junit.Test;
import org.testtools.jsondiff.CompareRule;
import org.testtools.jsondiff.CompareRules;
import org.testtools.jsondiff.CompareRulesTransformer;
import org.testtools.jsondiff.comparator.CustomComparator;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for CompareRulesTransformer optimizations
 */
public class CompareRulesTransformerTest {

    @Test
    public void testStringToCompareRulesWithNullInput() {
        try {
            CompareRulesTransformer.stringToCompareRules(null);
            fail("Should throw IllegalArgumentException for null input");
        } catch (IllegalArgumentException e) {
            assertEquals("Rules JSON string cannot be null", e.getMessage());
        }
    }

    @Test
    public void testGetComparatorWithNullCompareRule() {
        try {
            CompareRulesTransformer.getComparator((CompareRule) null);
            fail("Should throw IllegalArgumentException for null CompareRule");
        } catch (IllegalArgumentException e) {
            assertEquals("CompareRule cannot be null", e.getMessage());
        }
    }

    @Test
    public void testGetComparatorWithNullString() {
        try {
            CompareRulesTransformer.getComparator((String) null);
            fail("Should throw IllegalArgumentException for null string");
        } catch (IllegalArgumentException e) {
            assertEquals("Compare rules string cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testGetComparatorWithEmptyString() {
        try {
            CompareRulesTransformer.getComparator("");
            fail("Should throw IllegalArgumentException for empty string");
        } catch (IllegalArgumentException e) {
            assertEquals("Compare rules string cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testCompareRuleWithMissingBooleanFields() {
        // Test that CompareRule can handle missing Boolean fields without throwing NullPointerException
        Map<String, Object> ruleMap = new HashMap<>();
        ruleMap.put("jsonPath", "$.test");
        // Don't add Boolean fields to test default values
        
        try {
            CompareRule rule = new CompareRule(ruleMap);
            assertNotNull("CompareRule should be created successfully", rule);
            assertNotNull("CompareContext should be created", rule.getCompareContext());
            assertEquals("jsonPath should be set correctly", "$.test", rule.getJsonPath());
        } catch (Exception e) {
            fail("CompareRule creation should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCompareRuleWithNullCustomRules() {
        // Test that CompareRule can handle null customRules
        Map<String, Object> ruleMap = new HashMap<>();
        ruleMap.put("jsonPath", "$.test");
        ruleMap.put("extensible", true);
        ruleMap.put("strictOrder", true);
        ruleMap.put("fastFail", false);
        ruleMap.put("ignoreNull", false);
        // Don't add customRules to test null handling
        
        try {
            CompareRule rule = new CompareRule(ruleMap);
            assertNotNull("CompareRule should be created successfully", rule);
            assertNotNull("customRules should be initialized as empty list", rule.getCustomRules());
            assertTrue("customRules should be empty", rule.getCustomRules().isEmpty());
        } catch (Exception e) {
            fail("CompareRule creation should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testStringToCompareRulesWithInvalidJson() {
        // Test that invalid JSON returns empty CompareRules instead of throwing exception
        CompareRules result = CompareRulesTransformer.stringToCompareRules("invalid json");
        assertNotNull("Result should not be null", result);
        assertNotNull("Custom rules should be initialized", result.getCustomRules());
    }
} 