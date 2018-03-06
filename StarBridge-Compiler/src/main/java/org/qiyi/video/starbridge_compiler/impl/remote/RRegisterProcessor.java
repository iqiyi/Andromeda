package org.qiyi.video.starbridge_compiler.impl.remote;

import org.qiyi.video.starbridge_annotations.remote.RRegister;
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

public class RRegisterProcessor extends RegisterProcessor {

    public RRegisterProcessor(Map<String, RegisterClassBean> registerClassBeanMap) {
        super(registerClassBeanMap);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return RRegister.class;
    }

    @Override
    public void addRegisterInfo(RegisterClassBean registerClassBean, String serviceName,
                                String methodName, List<? extends VariableElement> variableElements) throws ProcessingException {
        registerClassBean.addRemoteRegisterInfo(serviceName, methodName, variableElements);
    }
}
