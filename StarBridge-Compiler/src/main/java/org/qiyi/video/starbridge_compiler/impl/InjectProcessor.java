package org.qiyi.video.starbridge_compiler.impl;

import org.qiyi.video.starbridge_compiler.ElementProcessor;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.utils.ProcessorUtils;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

/**
 * Created by wangallen on 2018/3/5.
 */

public abstract class InjectProcessor implements ElementProcessor {

    private Map<String, RegisterClassBean> registerClassBeanMap;

    public abstract Class<? extends Annotation> getAnnotationClass();

    public abstract void addField(RegisterClassBean registerClassBean,
                                  String serviceCanonicalName, String fieldName) throws ProcessingException;

    public InjectProcessor(Map<String, RegisterClassBean> registerClassBeanMap) {
        this.registerClassBeanMap = registerClassBeanMap;
    }


    @Override
    public void process(Set<? extends Element> elements) throws ProcessingException {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.FIELD) {
                throw new ProcessingException(element, "Only fields can be annotated with @%s", getAnnotationClass().getSimpleName());
            }

            String serviceCanonicalName = ProcessorUtils.getServiceCanonicalName(element, getAnnotationClass());

            String enclosingClassName = ProcessorUtils.getEnclosingClassName(element);

            RegisterClassBean registerClassBean = ProcessorUtils.chooseOrCreateRegisterClassBean(registerClassBeanMap, enclosingClassName);

            addField(registerClassBean, serviceCanonicalName, element.toString());
        }
    }

}
