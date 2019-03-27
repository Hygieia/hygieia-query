package com.capitalone.dashboard.query.model;

import java.util.List;
import java.util.Map;


public class SomeTestClass {
    String field1;
    String field_1;     // for testing words with "_"
    Integer field2;
    Double field3;

    Object $id;

    Map noTypeMap;
    Map<String, Object> stringObjectMap;
    Map<String, String> stringStringMap;
    Map<String, List<String>> stringListStringMap;
    Map<String, Map<String, Object>> stringMapMapStringString;
    Map<String, Map<String, List<String>>> stringMapMapStringListString;
    Map<String, Integer> stringIntegerMap;
    Map<String, List<ClassOne>> stringListClassOne;
    List<ClassOne> classOneList;

    public static class ClassOne {
        String stringOne;
        Integer integerOne;
        Map<String, ClassTwo> mapOne;

        public String getStringOne() {
            return stringOne;
        }

        public void setStringOne(String stringOne) {
            this.stringOne = stringOne;
        }

        public Integer getIntegerOne() {
            return integerOne;
        }

        public void setIntegerOne(Integer integerOne) {
            this.integerOne = integerOne;
        }

        public Map<String, ClassTwo> getMapOne() {
            return mapOne;
        }

        public void setMapOne(Map<String, ClassTwo> mapOne) {
            this.mapOne = mapOne;
        }
    }

    public class ClassTwo {
        String stringTwo;
        Integer integerTwo;
        Map<String, Object> mapTwo;

        public String getStringTwo() {
            return stringTwo;
        }

        public void setStringTwo(String stringTwo) {
            this.stringTwo = stringTwo;
        }

        public Integer getIntegerTwo() {
            return integerTwo;
        }

        public void setIntegerTwo(Integer integerTwo) {
            this.integerTwo = integerTwo;
        }

        public Map<String, Object> getMapTwo() {
            return mapTwo;
        }

        public void setMapTwo(Map<String, Object> mapTwo) {
            this.mapTwo = mapTwo;
        }
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public Integer getField2() {
        return field2;
    }

    public void setField2(Integer field2) {
        this.field2 = field2;
    }

    public Double getField3() {
        return field3;
    }

    public void setField3(Double field3) {
        this.field3 = field3;
    }

    public String getField_1() {
        return field_1;
    }

    public void setField_1(String field_1) {
        this.field_1 = field_1;
    }

    public Object get$id() {
        return $id;
    }

    public void set$id(Object $id) {
        this.$id = $id;
    }

    public Map getNoTypeMap() {
        return noTypeMap;
    }

    public void setNoTypeMap(Map noTypeMap) {
        this.noTypeMap = noTypeMap;
    }

    public Map<String, Object> getStringObjectMap() {
        return stringObjectMap;
    }

    public void setStringObjectMap(Map<String, Object> stringObjectMap) {
        this.stringObjectMap = stringObjectMap;
    }

    public Map<String, String> getStringStringMap() {
        return stringStringMap;
    }

    public void setStringStringMap(Map<String, String> stringStringMap) {
        this.stringStringMap = stringStringMap;
    }

    public Map<String, List<String>> getStringListStringMap() {
        return stringListStringMap;
    }

    public void setStringListStringMap(Map<String, List<String>> stringListStringMap) {
        this.stringListStringMap = stringListStringMap;
    }

    public Map<String, Map<String, Object>> getStringMapMapStringString() {
        return stringMapMapStringString;
    }

    public void setStringMapMapStringString(Map<String, Map<String, Object>> stringMapMapStringString) {
        this.stringMapMapStringString = stringMapMapStringString;
    }

    public Map<String, Map<String, List<String>>> getStringMapMapStringListString() {
        return stringMapMapStringListString;
    }

    public void setStringMapMapStringListString(Map<String, Map<String, List<String>>> stringMapMapStringListString) {
        this.stringMapMapStringListString = stringMapMapStringListString;
    }

    public Map<String, Integer> getStringIntegerMap() {
        return stringIntegerMap;
    }

    public void setStringIntegerMap(Map<String, Integer> stringIntegerMap) {
        this.stringIntegerMap = stringIntegerMap;
    }

    public Map<String, List<ClassOne>> getStringListClassOne() {
        return stringListClassOne;
    }

    public void setStringListClassOne(Map<String, List<ClassOne>> stringListClassOne) {
        this.stringListClassOne = stringListClassOne;
    }

    public List<ClassOne> getClassOneList() {
        return classOneList;
    }

    public void setClassOneList(List<ClassOne> classOneList) {
        this.classOneList = classOneList;
    }
}
