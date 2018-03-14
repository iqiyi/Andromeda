package org.qiyi.video.svg.plugin.bean

import javassist.CtMethod


public class MethodWrapper{

    private String methodName
    private List<String>parameterTypeNames

    private CtMethod method

    String getMethodName() {
        return methodName
    }

    void setMethodName(String methodName) {
        this.methodName = methodName
    }

    List<String> getParameterTypeNames() {
        return parameterTypeNames
    }

    void setParameterTypeNames(List<String> parameterTypeNames) {
        this.parameterTypeNames = parameterTypeNames
    }

    CtMethod getMethod() {
        return method
    }

    void setMethod(CtMethod method) {
        this.method = method
    }
}