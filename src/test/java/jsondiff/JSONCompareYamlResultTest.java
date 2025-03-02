/*
 * ArrayObjectValueMatcherTest.java
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class JSONCompareYamlResultTest {

    private static final String TEST_PATH = "/src/test/resources/";

    private void runTestCase(String caseNumber) throws Exception {
        // Read the test file
        String expectedJSON = FileUtil.readFile(TEST_PATH + "case_" + caseNumber + "_a.json");
        String actualJSON = FileUtil.readFile(TEST_PATH + "case_" + caseNumber + "_e.json");
        String rules = FileUtil.readFile(TEST_PATH + "rule_case" + caseNumber + ".yaml");


        JSONCompareResult result = JSONCompare.compareJSONYaml(expectedJSON, actualJSON, rules);

        ObjectMapper objectMapper = new ObjectMapper();
        String actualResult = objectMapper.writeValueAsString(result.getFailure());
        String outputFilePath = "./testoutput/case_" + caseNumber + "_diff.json";
        writeToFile(outputFilePath, actualResult);
        actualResult=FileUtil.readFile(outputFilePath);
        System.out.println("case_" + caseNumber);
        System.out.println(actualResult);
        String expectResult = FileUtil.readFile(TEST_PATH + "case_" + caseNumber + "_result.json");

        assertEquals("case_" + caseNumber, actualResult,expectResult);

    }

    private void writeToFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // Create all necessary parent directories
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }

    @Test
    public void TestCase() throws Exception {
        runTestCase("01");
    }
}
