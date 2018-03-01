package org.qiyi.video.starbridge_compiler.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangallen on 2018/3/1.
 */

public class RegisterClassBean implements Serializable {

    private String registerClassName;

    private List<RegisterMethodBean> methodBeans;

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
}
