package org.qiyi.video.starbridge_compiler.impl;

import org.qiyi.video.starbridge_compiler.ElementProcessor;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.utils.ProcessorUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Created by wangallen on 2018/3/5.
 */

public abstract class GetProcessor implements ElementProcessor {

    private Map<String, RegisterClassBean> registerClassBeanMap;

    public GetProcessor(Map<String, RegisterClassBean> registerClassBeanMap) {
        this.registerClassBeanMap = registerClassBeanMap;
    }

    public abstract Class<? extends Annotation> getAnnotationClass();

    public abstract void addGetInfo(RegisterClassBean registerClassBean, String serviceName,
                                    String methodName, List<? extends VariableElement> variableElements) throws ProcessingException;

    @Override
    public void process(Set<? extends Element> elements) throws ProcessingException {
        for (Element element : elements) {

            Set<String> serviceNameSet = ProcessorUtils.getServiceNameSet(element, getAnnotationClass());

            String registerClassName = ProcessorUtils.getRegisterClassName(element.getEnclosingElement());

            ExecutableElement methodElement = (ExecutableElement) element;
            String methodName = methodElement.getSimpleName().toString();
            List<? extends VariableElement> variableElements = methodElement.getParameters();

            for (String serviceName : serviceNameSet) {
                RegisterClassBean registerClassBean = registerClassBeanMap.get(registerClassName);
                if (registerClassBean == null) {
                    throw new ProcessingException("no matched field annotated by @LInject(" + serviceName + ") int " + registerClassName);
                }
                addGetInfo(registerClassBean, serviceName, methodName, variableElements);
            }
        }
    }

}
