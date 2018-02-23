package org.qiyi.video.starbridge_compiler;

import org.qiyi.video.starbridge_compiler.bean.MethodBean;

/**
 * Created by wangallen on 2018/2/11.
 */

public class LocalServiceBean {

    private String serviceImplField;

    private String serviceCanonicalName;
    //private Class<?> service;

    //如果是匿名内部类就类似wang.imallen.blog.applemodule.MainActivity$OnClickListener$1这样的类
    private String registerClassName;
    //private Class<?>registerClass;

    private MethodBean methodBean;
    //TODO 这里要考虑到方法重载的情况!
    //private String registerMethod;

    public String getServiceCanonicalName() {
        return serviceCanonicalName;
    }

    public void setServiceCanonicalName(String serviceCanonicalName) {
        this.serviceCanonicalName = serviceCanonicalName;
    }

    public String getRegisterClassName() {
        return registerClassName;
    }

    public void setRegisterClassName(String registerClassName) {
        this.registerClassName = registerClassName;
    }

    public MethodBean getMethodBean() {
        return methodBean;
    }

    public void setMethodBean(MethodBean methodBean) {
        this.methodBean = methodBean;
    }

    public String getServiceImplField() {
        return serviceImplField;
    }

    public void setServiceImplField(String serviceImplField) {
        this.serviceImplField = serviceImplField;
    }
}
