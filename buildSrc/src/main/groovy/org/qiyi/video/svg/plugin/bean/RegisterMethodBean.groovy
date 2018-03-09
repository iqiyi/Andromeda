package org.qiyi.video.svg.plugin.bean

public class RegisterMethodBean{

    private String methodName;
    private List<String> parameterTypeNames;

    private List<ServiceInfo> localRegisterInfos;
    private List<ServiceInfo> localGetInfos;
    //名称为serviceCanonicalName
    private List<String> localUnregisterInfos;

    private List<ServiceInfo> remoteRegisterInfos;
    private List<ServiceInfo> remoteGetInfos;String getMethodName() {
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

    List<ServiceInfo> getLocalRegisterInfos() {
        return localRegisterInfos
    }

    void setLocalRegisterInfos(List<ServiceInfo> localRegisterInfos) {
        this.localRegisterInfos = localRegisterInfos
    }

    List<ServiceInfo> getLocalGetInfos() {
        return localGetInfos
    }

    void setLocalGetInfos(List<ServiceInfo> localGetInfos) {
        this.localGetInfos = localGetInfos
    }

    List<String> getLocalUnregisterInfos() {
        return localUnregisterInfos
    }

    void setLocalUnregisterInfos(List<String> localUnregisterInfos) {
        this.localUnregisterInfos = localUnregisterInfos
    }

    List<ServiceInfo> getRemoteRegisterInfos() {
        return remoteRegisterInfos
    }

    void setRemoteRegisterInfos(List<ServiceInfo> remoteRegisterInfos) {
        this.remoteRegisterInfos = remoteRegisterInfos
    }

    List<ServiceInfo> getRemoteGetInfos() {
        return remoteGetInfos
    }

    void setRemoteGetInfos(List<ServiceInfo> remoteGetInfos) {
        this.remoteGetInfos = remoteGetInfos
    }
}