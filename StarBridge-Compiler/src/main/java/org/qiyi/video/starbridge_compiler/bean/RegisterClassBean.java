package org.qiyi.video.starbridge_compiler.bean;

import org.qiyi.video.starbridge_compiler.ProcessingException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.VariableElement;

/**
 * Created by wangallen on 2018/3/1.
 */

public class RegisterClassBean implements Serializable {

    private String registerClassName;

    //key是serviceCanonicalName, value为serviceFieldName
    private transient Map<String, String> localBindInfoMap = new HashMap<>();
    private transient Map<String, String> localInjectInfoMap = new HashMap<>();


    private transient Map<String, String> remoteBindInfoMap = new HashMap<>();
    private transient Map<String, String> remoteInjectInfoMap = new HashMap<>();

    private List<RegisterMethodBean> methodBeans = new ArrayList<>();

    public String getRegisterClassName() {
        return registerClassName;
    }

    public void setRegisterClassName(String registerClassName) {
        this.registerClassName = registerClassName;
    }

    public List<RegisterMethodBean> getMethodBeans() {
        return methodBeans;
    }

    public void setMethodBeans(List<RegisterMethodBean> methodBeans) {
        this.methodBeans = methodBeans;
    }

    public void addLocalBindField(String serviceCanonicalName, String serviceFieldName) throws ProcessingException {
        if (localBindInfoMap.get(serviceCanonicalName) != null) {
            throw new ProcessingException("Only one field whose type is " + serviceCanonicalName + " is allowed in one class!");
        }
        localBindInfoMap.put(serviceCanonicalName, serviceFieldName);
    }

    public void addLocalInjectField(String serviceCanonicalName, String injectFieldName) throws ProcessingException {
        if (localInjectInfoMap.get(serviceCanonicalName) != null) {
            throw new ProcessingException("Only one field whose type is " + serviceCanonicalName + " is allowed in one class!");
        }
        localInjectInfoMap.put(serviceCanonicalName, injectFieldName);
    }

    //TODO 这其实是目前的方案还不是很完美，所以一个类中同一个类型的注入field只能有一个!
    public void addRemoteBindField(String serviceCanonicalName, String serviceFieldName) throws ProcessingException {
        if (remoteBindInfoMap.get(serviceCanonicalName) != null) {
            throw new ProcessingException("Only one field whose type is " + serviceCanonicalName + " is allowed in a class!");
        }
        remoteBindInfoMap.put(serviceCanonicalName, serviceFieldName);
    }

    public void addRemoteInjectField(String serviceCanonicalName, String injectFieldName) throws ProcessingException {
        if (remoteInjectInfoMap.get(serviceCanonicalName) != null) {
            throw new ProcessingException("Only ONE field whose type is " + serviceCanonicalName + " is allowed in ONE class!");
        }
        remoteInjectInfoMap.put(serviceCanonicalName, injectFieldName);
    }

    public void addLocalRegisterInfo(String serviceCanonicalName, String methodName, List<? extends VariableElement> parameterTypes) throws ProcessingException {
        RegisterMethodBean methodBean = initOrSetInfo4RegisterMethodBean(localBindInfoMap, serviceCanonicalName, methodName, parameterTypes);
        String serviceFieldName = localBindInfoMap.get(serviceCanonicalName);
        methodBean.addLocalRegisterInfo(new ServiceInfo(serviceCanonicalName, serviceFieldName));
    }

    public void addLocalUnRegisterInfo(String serviceCanonicalName, String methodName,
                                       List<? extends VariableElement> parameterTypes) throws ProcessingException {
        List<String> parameterTypeNames = createParameterTypeNames(parameterTypes);
        RegisterMethodBean registerMethodBean = chooseMethodBean(methodName, parameterTypeNames);
        if (registerMethodBean == null) {
            registerMethodBean = createRegisterMethodBean(methodName, parameterTypeNames);
            methodBeans.add(registerMethodBean);
        }
        registerMethodBean.addLocalUnRegisterInfo(serviceCanonicalName);
    }

    public void addRemoteUnRegisterInfo(String serviceCanonicalName, String methodName,
                                        List<? extends VariableElement> parameterTypes) throws ProcessingException {
        List<String> parameterTypeNames = createParameterTypeNames(parameterTypes);
        RegisterMethodBean registerMethodBean = chooseMethodBean(methodName, parameterTypeNames);
        if (registerMethodBean == null) {
            registerMethodBean = createRegisterMethodBean(methodName, parameterTypeNames);
            methodBeans.add(registerMethodBean);
        }
        registerMethodBean.addRemoteUnRegisterInfo(serviceCanonicalName);
    }

    private RegisterMethodBean initOrSetInfo4RegisterMethodBean(Map<String, String> fieldMap, String serviceCanonicalName,
                                                                String methodName, List<? extends VariableElement> parameterTypes) throws ProcessingException {
        String serviceFieldName = fieldMap.get(serviceCanonicalName);
        if (serviceFieldName == null) {
            throw new ProcessingException("No field whose type is " + serviceCanonicalName + " in " + registerClassName);
        }
        List<String> parameterTypeNames = createParameterTypeNames(parameterTypes);
        RegisterMethodBean registerMethodBean = chooseMethodBean(methodName, parameterTypeNames);
        if (registerMethodBean == null) {
            registerMethodBean = createRegisterMethodBean(methodName, parameterTypeNames);
            methodBeans.add(registerMethodBean);
        }
        return registerMethodBean;
        //registerMethodBean.addRemoteRegisterInfo(new ServiceInfo(serviceCanonicalName, serviceFieldName));
    }


    public void addRemoteRegisterInfo(String serviceCanonicalName, String methodName, List<? extends VariableElement> parameterTypes) throws ProcessingException {
        RegisterMethodBean methodBean = initOrSetInfo4RegisterMethodBean(remoteBindInfoMap, serviceCanonicalName, methodName, parameterTypes);
        String serviceFieldName = remoteBindInfoMap.get(serviceCanonicalName);
        methodBean.addRemoteRegisterInfo(new ServiceInfo(serviceCanonicalName, serviceFieldName));
    }


    public void addLocalGetInfo(String serviceCanonicalName, String methodName, List<? extends VariableElement> parameterTypes) throws ProcessingException {
        RegisterMethodBean methodBean = initOrSetInfo4RegisterMethodBean(localInjectInfoMap, serviceCanonicalName, methodName, parameterTypes);
        String localInjectFieldName = localInjectInfoMap.get(serviceCanonicalName);
        methodBean.addLocalGetInfo(new ServiceInfo(serviceCanonicalName, localInjectFieldName));

    }

    public void addRemoteGetInfo(String serviceCanonicalName, String methodName, List<? extends VariableElement> parameterTypes) throws ProcessingException {
        RegisterMethodBean methodBean = initOrSetInfo4RegisterMethodBean(remoteInjectInfoMap, serviceCanonicalName, methodName, parameterTypes);
        String remoteInjectFieldName = remoteInjectInfoMap.get(serviceCanonicalName);
        methodBean.addRemoteGetInfo(new ServiceInfo(serviceCanonicalName, remoteInjectFieldName));
    }

    private List<String> createParameterTypeNames(List<? extends VariableElement> parameterTypes) {
        List<String> parameterTypeNames = null;
        if (parameterTypes != null) {
            parameterTypeNames = new ArrayList<>();
            for (VariableElement element : parameterTypes) {
                parameterTypeNames.add(element.asType().toString());
            }
        }
        return parameterTypeNames;
    }

    private RegisterMethodBean createRegisterMethodBean(String methodName, List<String> parameterTypeNames) {
        RegisterMethodBean registerMethodBean = new RegisterMethodBean();
        registerMethodBean.setMethodName(methodName);
        registerMethodBean.setParameterTypeNames(parameterTypeNames);
        return registerMethodBean;
    }

    private RegisterMethodBean chooseMethodBean(String methodName, List<String> parameterTypeNames) {
        for (RegisterMethodBean bean : methodBeans) {
            if (!methodName.equals(bean.getMethodName())) {
                continue;
            }
            if (isSameParameters(parameterTypeNames, bean)) {
                return bean;
            }
        }
        return null;
    }

    private boolean isSameParameters(List<String> parameterTypeNames, RegisterMethodBean bean) {
        if (parameterTypeNames == null && bean.getParameterTypeNames() == null) {
            return true;
        }
        if (parameterTypeNames == null || bean.getParameterTypeNames() == null) {
            return false;
        }
        if (parameterTypeNames.size() != bean.getParameterTypeNames().size()) {
            return false;
        }
        for (int i = 0; i < parameterTypeNames.size(); ++i) {
            if (!parameterTypeNames.get(i).equals(bean.getParameterTypeNames().get(i))) {
                return false;
            }
        }
        return true;
    }


}
