package com.test4x.penknife.entity;

import java.util.ArrayList;
import java.util.List;

public class Parameter {
    private String key;
    private List<String> values;

    public Parameter(String key, String value) {
        this.key = key;
        this.values = new ArrayList<>();
        values.add(value);
    }

    public Parameter add(Parameter parameter) {
        if (!this.key.equals(parameter.key)) {
            throw new UnsupportedOperationException("Parameter key should be same");
        }
        this.values.addAll(parameter.values);
        return this;
    }


    public String getKey() {
        return key;
    }

    public String getValue() {
        if (values.isEmpty()) {
            return null;
        }
        return String.join(",", values);
    }

    public List<String> getValues() {
        return values;
    }
}
