package org.testtools.jsondiff;

public class CompareMatcherItem {
    private String name;
    private String jsonPath;
    private String param;

    public CompareMatcherItem(String name, String jsonPath, String param) {
        this.name = name;
        this.jsonPath = jsonPath;
        this.param = param;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}

