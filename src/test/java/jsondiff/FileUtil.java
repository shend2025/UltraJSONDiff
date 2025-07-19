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
     * 读取文件内容
     * @param fileName 文件路径，可以是相对路径或绝对路径
     * @return 文件内容字符串
     */
    public static String readFile(String fileName) {
        try{
            String path;
            // 如果是绝对路径，直接使用；否则拼接工作目录
            if (new File(fileName).isAbsolute()) {
                path = fileName;
            } else {
                path = DIR_PATH + fileName;
            }
            
            File file = new File(path);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
//            System.out.println("Reading text file using FileReader");
            StringBuffer sb = new StringBuffer();
            while((line = br.readLine()) != null){
                //逐行读取
//                System.out.println(line);
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
     * 获取文件的绝对路径
     * @param filePath 文件路径，可以是相对路径或绝对路径
     * @return 文件的绝对路径
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
     * 检查文件是否存在
     * @param filePath 文件路径
     * @return 如果文件存在返回true，否则返回false
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
     * 获取文件所在目录的绝对路径
     * @param filePath 文件路径
     * @return 文件所在目录的绝对路径
     */
    public static String getParentPath(String filePath) {
        return new File(getAbsolutePath(filePath)).getParent();
    }
}
