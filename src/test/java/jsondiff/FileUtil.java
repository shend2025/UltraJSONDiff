/*
 * FileUtil.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {
    public static String DIR_PATH = System.getProperty("user.dir");

    public FileUtil() {
    }

    /**
     * Read file content
     * @param fileName File path, can be relative path or absolute path
     * @return File content string
     */
    public static String readFile(String fileName) {
        try{
            String path;
            // If it's an absolute path, use it directly; otherwise concatenate with working directory
            if (new File(fileName).isAbsolute()) {
                path = fileName;
            } else {
                path = DIR_PATH + fileName;
            }
            
            File file = new File(path);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;

            StringBuffer sb = new StringBuffer();
            while((line = br.readLine()) != null){

                sb.append(line).append("\n");
            }
            br.close();
            fr.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the absolute path of the file
     * @param filePath File path, can be relative path or absolute path
     * @return The absolute path of the file
     */
    public static String getAbsolutePath(String filePath) {
        File file = new File(filePath);
        if (file.isAbsolute()) {
            return file.getAbsolutePath();
        } else {
            return new File(DIR_PATH, filePath).getAbsolutePath();
        }
    }

    /**
     * Check if the file exists
     * @param filePath File path
     * @return Returns true if the file exists, otherwise returns false
     */
    public static boolean fileExists(String filePath) {
        String path;
        if (new File(filePath).isAbsolute()) {
            path = filePath;
        } else {
            path = DIR_PATH + filePath;
        }
        return new File(path).exists();
    }

    /**
     * Get the absolute path of the directory containing the file
     * @param filePath File path
     * @return The absolute path of the directory containing the file
     */
    public static String getParentPath(String filePath) {
        return new File(getAbsolutePath(filePath)).getParent();
    }
}
