package org.qiyi.video.starbridge_compiler.impl.remote;

import org.qiyi.video.starbridge_annotations.remote.RUnRegister;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.impl.RegisterProcessor;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.VariableElement;

/**
 * Created by wangallen on 2018/3/12.
 */

public class RUnRegisterProcessor extends RegisterProcessor {

    public RUnRegisterProcessor(Map<String, RegisterClassBean> registerClassBeanMap) {
        super(registerClassBeanMap);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return RUnRegister.class;
    }

    @Override
    public void addRegisterInfo(RegisterClassBean registerClassBean, String serviceName, String methodName, List<? extends VariableElement> variableElements) throws ProcessingException {
        registerClassBean.addRemoteUnRegisterInfo(serviceName, methodName, variableElements);
    }
}
