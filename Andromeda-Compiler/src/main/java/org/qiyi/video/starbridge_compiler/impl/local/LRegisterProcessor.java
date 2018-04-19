package org.qiyi.video.starbridge_compiler.impl.local;

import org.qiyi.video.starbridge_annotations.local.LRegister;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.impl.RegisterProcessor;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.VariableElement;

/**
 * Created by wangallen on 2018/3/5.
 */

public class LRegisterProcessor extends RegisterProcessor {

    public LRegisterProcessor(Map<String, RegisterClassBean> registerClassBeanMap) {
        super(registerClassBeanMap);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return LRegister.class;
    }

    @Override
    public void addRegisterInfo(RegisterClassBean registerClassBean, String serviceName, String methodName, List<? extends VariableElement> variableElements) throws ProcessingException {
        registerClassBean.addLocalRegisterInfo(serviceName, methodName, variableElements);
    }

}
