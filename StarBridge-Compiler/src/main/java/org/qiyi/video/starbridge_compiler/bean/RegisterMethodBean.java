package org.qiyi.video.starbridge_compiler.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 这个包含了本地注册、注销和获取信息，远程服务注册和获取信息
 * Created by wangallen on 2018/3/1.
 */

public class RegisterMethodBean implements Serializable {

    private String methodName;
    private List<String> parameterTypeNames;

    private List<ServiceInfo> localRegisterInfos;
    private List<ServiceInfo> localGetInfos;
    //名称为serviceCanonicalName
    private List<String> localUnregisterInfos;

    private List<ServiceInfo> remoteRegisterInfos;
    private List<ServiceInfo> remoteGetInfos;

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

    public List<ServiceInfo> getLocalRegisterInfos() {
        return localRegisterInfos;
    }

    public void setLocalRegisterInfos(List<ServiceInfo> localRegisterInfos) {
        this.localRegisterInfos = localRegisterInfos;
    }

    public List<ServiceInfo> getLocalGetInfos() {
        return localGetInfos;
    }

    public void setLocalGetInfos(List<ServiceInfo> localGetInfos) {
        this.localGetInfos = localGetInfos;
    }

    public List<String> getLocalUnregisterInfos() {
        return localUnregisterInfos;
    }

    public void setLocalUnregisterInfos(List<String> localUnregisterInfos) {
        this.localUnregisterInfos = localUnregisterInfos;
    }

    public List<ServiceInfo> getRemoteRegisterInfos() {
        return remoteRegisterInfos;
    }

    public void setRemoteRegisterInfos(List<ServiceInfo> remoteRegisterInfos) {
        this.remoteRegisterInfos = remoteRegisterInfos;
    }

    public List<ServiceInfo> getRemoteGetInfos() {
        return remoteGetInfos;
    }

    public void setRemoteGetInfos(List<ServiceInfo> remoteGetInfos) {
        this.remoteGetInfos = remoteGetInfos;
    }

    public void addLocalRegisterInfo(ServiceInfo serviceInfo) {
        if (localRegisterInfos == null) {
            localRegisterInfos = new ArrayList<>();
        }
        localRegisterInfos.add(serviceInfo);
    }

    public void addLocalGetInfo(ServiceInfo serviceInfo) {
        if (localGetInfos == null) {
            localGetInfos = new ArrayList<>();
        }
        localGetInfos.add(serviceInfo);
    }

    public void addLocalUnRegisterInfo(String serviceCanonicalName) {
        if (null == localUnregisterInfos) {
            localUnregisterInfos = new ArrayList<>();
        }
        localUnregisterInfos.add(serviceCanonicalName);
    }

    public void addRemoteRegisterInfo(ServiceInfo serviceInfo) {
        if (remoteRegisterInfos == null) {
            remoteRegisterInfos = new ArrayList<>();
        }
        remoteRegisterInfos.add(serviceInfo);
    }

    public void addRemoteGetInfo(ServiceInfo serviceInfo) {
        if (null == remoteGetInfos) {
            remoteGetInfos = new ArrayList<>();
        }
        remoteGetInfos.add(serviceInfo);
    }

}
