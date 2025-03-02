/*
 * JSONCompareResultUtil.java
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class JSONCompareResultUtil {
    private static final Pattern patternArray = Pattern.compile("^\\[(.*?)\\]");
    private static final Pattern patternArrayFull = Pattern.compile("(.+?)\\[(.*?)\\]");
    private static final Pattern patternAEqualsB = Pattern.compile("([^=\\s]+)=([^=\\s]+)");
    private JSONCompareResultUtil() {
    }

    public static void getAbsolutePath(Object expected, Object actual,
                                       JSONCompareDeepDetailResult result) throws JSONException {
        List<FieldComparisonFailure> _fieldFailure = result.getFieldFailures();
        List<FieldComparisonFailure> _fieldMissing = result.getFieldMissing();
        List<FieldComparisonFailure> _fieldUnexpected = result.getFieldUnexpected();

        for (FieldComparisonFailure f : _fieldFailure) {
            result.addExpectedFail(getAbsolutePath(expected, f.getField()));
            result.addActualFail(getAbsolutePath(actual, f.getField()));
        }

        for (FieldComparisonFailure f : _fieldMissing) {
            result.addMissing(getAbsolutePath(expected, f.getField()));
        }

        for (FieldComparisonFailure f : _fieldUnexpected) {
            result.addUnexpected(getAbsolutePath(actual, f.getField()));
        }
    }

    public static String getAbsolutePath(Object object, String relativePath) {

        try {
//            String[] path = relativePath.split("\\.");
            List<String> path = getSplitPath(relativePath);
            StringBuilder prefix = new StringBuilder();
            Object obj = object;

            if (obj instanceof JSONArray) {
                Matcher matcher = patternArray.matcher(path.get(0));
                if (matcher.find()) {
                    obj = getIndexAndUpdateRealIndex(prefix, getMiddleString(path.get(0)), obj);
                }
            } else {
                obj = getNodeAndUpdateRealKey(prefix, path.get(0), obj);
            }

            for (int i = 1; i < path.size(); i++) {
                String p = path.get(i);

                if (p.isEmpty()) return prefix.toString();
                if (obj == null) return null;
                obj = getNodeAndUpdateRealKey(prefix, p, obj);
            }

            return prefix.toString();
        } catch (Exception e) {
            return null;
        }

    }

    private static Object getNodeAndUpdateRealKey(StringBuilder prefix, String key, Object obj) throws JSONException {
        JSONObject o = (JSONObject) obj;
        Matcher matcher = patternArrayFull.matcher(key);
        if (prefix.length() > 0) {
            prefix.append(".");
        }
        // 如果是json array路径：a[XXX]
        if (matcher.find()) {
            // 先获取json array本身的key: a
            String arrayKey = matcher.group(1);
            prefix.append(arrayKey);
            // 再获取相对的index: [XXX]，获取array的绝对index，并更新obj
            String relativeIndex = getMiddleString(getTailString(key, arrayKey.length()));
            return getIndexAndUpdateRealIndex(prefix, relativeIndex, o.get(arrayKey));

        } else {
            prefix.append(key);
            return o.get(key);
        }
    }

    private static Object getIndexAndUpdateRealIndex(StringBuilder prefix, String relativeIndex,
                                                     Object obj) throws JSONException {
        JSONArray a = (JSONArray) obj;
        if (relativeIndex.isEmpty()) {
            return a;
        }
        int index;
        Matcher matcher = patternAEqualsB.matcher(relativeIndex);
        // 如果index为 key=value的形式
        if (matcher.find()) {
            // 获取key
            String key = matcher.group(1);
            // 获取value
            String value = getTailString(relativeIndex, key.length() + 1);
            index = getArrayIndexFromKeyValue(a, key, value);
            if (index == -1) return null;
        } else {
            index = Integer.parseInt(relativeIndex);
        }
        prefix.append("[").append(index).append("]");
        return a.get(index);
    }

    private static int getArrayIndexFromKeyValue(JSONArray array, String key, String value) {
        try {
            for (int i = 0; i < array.length(); i++) {
                Object item = array.get(i);
                if (item instanceof JSONObject) {
                    JSONObject o = (JSONObject) item;
                    if (o.has(key) && String.valueOf(o.get(key)).equals(value)) {
                        return i;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }

    private static List<String> getSplitPath(String s) {
        List<String> splitPath = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int num = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '[') {
                num++;
            } else if (ch == ']') {
                num--;
            } else if (num == 0 && ch == '.') {
                splitPath.add(sb.toString());
                sb.setLength(0);
                continue;
            }
            sb.append(ch);
        }
        splitPath.add(sb.toString());
        return splitPath;
    }

    private static String getMiddleString(String s) {
        int len = s.length();
        if (len >= 2) {
            return s.substring(1, len - 1);
        }
        return s;
    }

    private static String getTailString(String s, int lenHead) {
        int len = s.length();
        if (len >= lenHead) {
            return s.substring(lenHead, len);
        }
        return s;
    }

    //获取原json对比结果的简化版
    public static JSONCompareSimpleResult getSimpleResult(JSONCompareDetailResult result) {
        List<FieldComparisonFailure> _fieldFailure = result.getFieldFailures();
        List<FieldComparisonFailure> _fieldMissing = result.getFieldMissing();
        List<FieldComparisonFailure> _fieldUnexpected = result.getFieldUnexpected();
        JSONCompareSimpleResult simpleResult = new JSONCompareSimpleResult();

        for (FieldComparisonFailure f : _fieldFailure) {
            simpleResult.addFailure(new FailureField(describe(f.getExpected()),
                    describe(f.getActual()), f.getField(), "actual unequals to expected"));
        }

        for (FieldComparisonFailure f : _fieldMissing) {
            simpleResult.addFailure(new FailureField(describe(f.getExpected()), null, f.getField(), "only in expected"));
        }

        for (FieldComparisonFailure f : _fieldUnexpected) {
            simpleResult.addFailure(new FailureField(null, describe(f.getActual()), f.getField(), "only in actual"));
        }

        return simpleResult;
    }

    private static Object describe(Object value) {
        if (value instanceof JSONArray) {
            return "a JSON array: " + getShortenString(value.toString());
        } else if (value instanceof JSONObject) {
            return "a JSON object: " + getShortenString(value.toString());
        } else {
            return value;
        }
    }

    private static String getShortenString(String s) {
        if (s.length() > 100) {
            return s.substring(0, 60) + "<...>" + s.substring(s.length() - 40);
        }
        return s;
    }
}
