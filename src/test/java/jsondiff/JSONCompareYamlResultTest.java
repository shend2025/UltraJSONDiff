/*
 * JSONCompareYamlResultTest.java
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

package jsondiff;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testtools.jsondiff.JSONCompare;
import org.testtools.jsondiff.JSONCompareResult;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Unit test class for testing JSON comparison functionality
 * Reads test cases from test resources folder, executes comparison and validates results
 */
public class JSONCompareYamlResultTest {

    private static final String TEST_RESOURCES_PATH = "src/test/resources/";
    private static final String OUTPUT_PATH = "testoutput/";
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        // Ensure output directory exists
        createOutputDirectory();
    }


    /**
     * Run the specified test case
     * @param caseNumber Test case number
     * @throws Exception if test execution fails
     */
    private void runTestCase(String caseNumber) throws Exception {
        System.out.println("=== Starting test case execution: case_" + caseNumber + " ===");
        
        // Build file paths
        String expectedJsonPath = TEST_RESOURCES_PATH + "case_" + caseNumber + "_e.json";
        String actualJsonPath = TEST_RESOURCES_PATH + "case_" + caseNumber + "_a.json";
        String rulesPath = TEST_RESOURCES_PATH + "rule_case" + caseNumber + ".yaml";
        String expectedResultPath = TEST_RESOURCES_PATH + "case_" + caseNumber + "_result.json";
        String outputFilePath = OUTPUT_PATH + "case_" + caseNumber + "_diff.json";

        // Validate test files exist
        validateTestFiles(caseNumber, expectedJsonPath, actualJsonPath, rulesPath, expectedResultPath);

        // Read test data
        String expectedJSON = readFileContent(expectedJsonPath);
        String actualJSON = readFileContent(actualJsonPath);
        String rules = readFileContent(rulesPath);
        String expectedResult = readFileContent(expectedResultPath);

        System.out.println("Test data reading completed:");
        System.out.println("- Expected JSON file: " + expectedJsonPath);
        System.out.println("- Actual JSON file: " + actualJsonPath);
        System.out.println("- Rules file: " + rulesPath);
        System.out.println("- Expected result file: " + expectedResultPath);

        // Execute JSON comparison
        System.out.println("Starting JSON comparison...");
        JSONCompareResult result = JSONCompare.compareJSON(expectedJSON, actualJSON, rules);

        // Serialize comparison result
        String actualResult = objectMapper.writeValueAsString(result.getFailure());
        
        // Write to output file
        writeToFile(outputFilePath, actualResult);
        System.out.println("Comparison result written to: " + outputFilePath);

        // Output comparison results for debugging
        System.out.println("Actual comparison result:");
        System.out.println(actualResult);
        System.out.println("Expected comparison result:");
        System.out.println(expectedResult);

        // Validate results
        assertEquals("Test case case_" + caseNumber + " comparison result does not match", 
                    expectedResult.trim(), actualResult.trim());
        
        System.out.println("=== Test case case_" + caseNumber + " executed successfully ===\n");
    }

    /**
     * Validate test files exist
     */
    private void validateTestFiles(String caseNumber, String... filePaths) {
        for (String filePath : filePaths) {
            // For test resource files, use classpath check
            if (filePath.startsWith(TEST_RESOURCES_PATH)) {
                String resourcePath = filePath.substring(TEST_RESOURCES_PATH.length());
                if (getClass().getClassLoader().getResource(resourcePath) == null) {
                    fail("Test resource file does not exist: " + resourcePath + " (test case: case_" + caseNumber + ")");
                }
            } else {
                // For other files, use FileUtil
                if (!FileUtil.fileExists(filePath)) {
                    fail("Test file does not exist: " + filePath + " (test case: case_" + caseNumber + ")");
                }
            }
        }
    }

    /**
     * Read file content
     */
    private String readFileContent(String filePath) throws IOException {
        // For test resource files, use classpath loading
        if (filePath.startsWith(TEST_RESOURCES_PATH)) {
            String resourcePath = filePath.substring(TEST_RESOURCES_PATH.length());
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    throw new IOException("Cannot find resource file: " + resourcePath);
                }
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } else {
            // For other files, use FileUtil
            String content = FileUtil.readFile(filePath);
            if (content == null) {
                throw new IOException("Cannot read file: " + filePath);
            }
            return content;
        }
    }

    /**
     * Create output directory
     */
    private void createOutputDirectory() {
        File outputDir = new File(OUTPUT_PATH);
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created) {
                System.err.println("Warning: Cannot create output directory: " + OUTPUT_PATH);
            }
        }
    }

    /**
     * Write to file
     */
    private void writeToFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }

    /**
     * Clean up test output files (optional)
     */
    private void cleanupTestOutput() {
        try {
            File outputDir = new File(OUTPUT_PATH);
            if (outputDir.exists()) {
                Files.walk(Paths.get(OUTPUT_PATH))
                     .filter(Files::isRegularFile)
                     .filter(path -> path.toString().endsWith("_diff.json"))
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             System.err.println("Cannot delete file: " + path);
                         }
                     });
            }
        } catch (IOException e) {
            System.err.println("Error cleaning up output files: " + e.getMessage());
        }
    }

    /**
     * Test case 01
     * Test basic JSON comparison functionality
     */
    @Test
    public void testCase01() throws Exception {
        runTestCase("01");
    }

    /**
     * Test file reading functionality
     */
    @Test
    public void testFileReading() {
        String testFilePath = TEST_RESOURCES_PATH + "case_01_a.json";
        // Use classpath to check test resource file
        String resourcePath = testFilePath.substring(TEST_RESOURCES_PATH.length());
        assertTrue("Test file should exist", getClass().getClassLoader().getResource(resourcePath) != null);
        
        try {
            String content = readFileContent(testFilePath);
            assertNotNull("File content should not be null", content);
            assertFalse("File content should not be empty", content.trim().isEmpty());
        } catch (IOException e) {
            fail("Exception occurred while reading file: " + e.getMessage());
        }
    }

    /**
     * Test basic JSON comparison functionality
     */
    @Test
    public void testBasicJSONCompare() throws Exception {
        String expectedJSON = "{\"name\":\"John\",\"age\":30}";
        String actualJSON = "{\"name\":\"John\",\"age\":30}";
        String rules = "[]";

        JSONCompareResult result = JSONCompare.compareJSON(expectedJSON, actualJSON, rules);
        assertNotNull("Comparison result should not be null", result);
        assertTrue("Identical JSON should compare successfully", result.getFailure().isEmpty());
    }
}
