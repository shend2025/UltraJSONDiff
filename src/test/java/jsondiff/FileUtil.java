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

    public static String readFile(String fileName) {
        try{
            String path = DIR_PATH + fileName;
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
            e.printStackTrace();;
            return null;
        }

    }
}
