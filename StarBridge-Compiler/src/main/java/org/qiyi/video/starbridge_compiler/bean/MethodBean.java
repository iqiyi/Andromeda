package org.qiyi.video.starbridge_compiler.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.VariableElement;

/**
 * Created by wangallen on 2018/2/12.
 */
//TODO 考虑干脆把LocalServiceBean中的serviceImplField和serviceCanonicalName这两个属性也放到MethodBean中，这样一来逻辑也简单多了。
public class MethodBean implements Serializable {

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

}
