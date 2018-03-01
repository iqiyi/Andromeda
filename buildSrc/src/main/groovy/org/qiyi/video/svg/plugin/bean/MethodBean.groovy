package org.qiyi.video.svg.plugin.bean

import javax.lang.model.element.VariableElement

public class MethodBean {

    /////////////////////用空间换时间，所以加上这两个字段//////////////////
    private String serviceImplField
    private String serviceCanonicalName
    //////////////////////////////////////////////////////////////////

    private String registerClassName;
    private String methodName;
    private List<String> parameterTypeNames;

    public MethodBean(String methodName, List<? extends VariableElement> parameters) {
        this.methodName = methodName;
        if (parameters == null || parameters.size() < 1) {
            return;
        }
        parameterTypeNames = new ArrayList<>();
        for (VariableElement element : parameters) {
            parameterTypeNames.add(element.asType().toString());
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getParameterTypeNames() {
        return parameterTypeNames;
    }

    public void setParameterTypeNames(List<String> parameterTypeNames) {
        this.parameterTypeNames = parameterTypeNames;
    }

    public String getRegisterClassName() {
        return registerClassName;
    }

    public void setRegisterClassName(String registerClassName) {
        this.registerClassName = registerClassName;
    }

    String getServiceImplField() {
        return serviceImplField
    }

    void setServiceImplField(String serviceImplField) {
        this.serviceImplField = serviceImplField
    }

    String getServiceCanonicalName() {
        return serviceCanonicalName
    }

    void setServiceCanonicalName(String serviceCanonicalName) {
        this.serviceCanonicalName = serviceCanonicalName
    }
}

