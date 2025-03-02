package org.testtools.jsondiff;

import java.util.Map;

public class CompareMatcherItem {
    private String name;
    private String jsonPath;
    private String param;
    private Map<String, String> pararms;

    //创建构造函数
    public CompareMatcherItem(String name, String jsonPath, String param) {
        this.name = name;
        this.jsonPath = jsonPath;
        this.param = param;
    }

    // Getter and Setter for path
    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String path) {
        this.jsonPath = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParam() {
        return this.param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}

