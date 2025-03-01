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

package com.nezha.jsondiff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nezha.jsondiff.comparator.CustomComparator;
import com.nezha.jsondiff.matcher.ValueMatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public final class CompareRulesTransformer {

    private CompareRulesTransformer() {
    }

    public static CompareRules stringToCompareRules(final String rulesInJsonStr) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(rulesInJsonStr, CompareRules.class);
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
            return new CompareRules();
        }
    }

    public static CustomComparator getComparator(final CompareRule compareRule) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        final List<Customization> customizations = getCustomizationsRe(compareRule);
        final int size = customizations.size();
        return new CustomComparator(compareRule.getCompareContext(),
                customizations.toArray(new Customization[size]));

    }

    public static CustomComparator getComparator(final String compareRules) {
        return getComparator(String.valueOf(stringToCompareRules(compareRules).getCustomRules().get(0)));
    }

    private static List<Customization> getCustomizationsRe(final CompareRule compareRule) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        CompareContext compareContext = compareRule.getCompareContext();
        List<CompareMatcherItem> customRules = compareRule.getCustomRules();

        final List<Customization> customizationList = new ArrayList<Customization>();
        for (CompareMatcherItem rule : customRules) {
            //根据获取到类，调用构造函数
            String path = rule.getJsonPath();
            String param = rule.getParam();
            String ruleName = rule.getName();
            String matchClassName = ruleName + "Matcher";
            //使用反射从ruleName获取到实际的matcher类，ruleName为类名称，matcher的package为com.nezha.jsondiff.matcher
            Class<?> clazz = Class.forName("com.nezha.jsondiff.matcher." + matchClassName);
            Constructor<?> constructor = clazz.getConstructor();

            Object matcher = constructor.newInstance();
            // 调用matcherInit方法
            Method matcherInitMethod = clazz.getMethod("matcherInit", String.class, CompareContext.class);
            matcherInitMethod.invoke(matcher, param, compareContext);
            // 将matcher添加到customizationList中
            customizationList.add(new Customization(path, (ValueMatcher<Object>) matcher));

        }
        return customizationList;
    }
}

