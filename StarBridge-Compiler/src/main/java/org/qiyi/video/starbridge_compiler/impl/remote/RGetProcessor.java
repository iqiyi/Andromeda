package org.qiyi.video.starbridge_compiler.impl.remote;

import org.qiyi.video.starbridge_annotations.remote.RGet;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.impl.GetProcessor;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.VariableElement;

/**
 * Created by wangallen on 2018/3/5.
 */

public class RGetProcessor extends GetProcessor {

    public RGetProcessor(Map<String, RegisterClassBean> registerClassBeanMap) {
        super(registerClassBeanMap);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return RGet.class;
    }

    @Override
    public void addGetInfo(RegisterClassBean registerClassBean, String serviceName, String methodName, List<? extends VariableElement> variableElements) throws ProcessingException {
        registerClassBean.addRemoteGetInfo(serviceName, methodName, variableElements);
    }
}
