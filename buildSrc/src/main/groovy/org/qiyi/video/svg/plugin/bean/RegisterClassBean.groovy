package org.qiyi.video.svg.plugin.bean

import org.qiyi.video.svg.plugin.bean.RegisterMethodBean

public class RegisterClassBean {

    private String registerClassName

    private List<RegisterMethodBean> methodBeans

    String getRegisterClassName() {
        return registerClassName
    }

    void setRegisterClassName(String registerClassName) {
        this.registerClassName = registerClassName
    }

    List<RegisterMethodBean> getMethodBeans() {
        return methodBeans
    }

    void setMethodBeans(List<RegisterMethodBean> methodBeans) {
        this.methodBeans = methodBeans
    }
}