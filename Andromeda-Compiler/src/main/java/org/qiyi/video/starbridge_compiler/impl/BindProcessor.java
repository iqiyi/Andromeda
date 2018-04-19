package org.qiyi.video.starbridge_compiler.impl;

import org.qiyi.video.starbridge_compiler.ElementProcessor;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.utils.ProcessorUtils;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;

/**
 * Created by wangallen on 2018/3/5.
 */

public abstract class BindProcessor implements ElementProcessor {

    public abstract Class<? extends Annotation> getAnnotationClass();

    public abstract void addBindField(RegisterClassBean registerClassBean,
                                      String serviceCanonicalName, String fieldName) throws ProcessingException;

    private Map<String, RegisterClassBean> registerClassBeanMap;

    public BindProcessor(Map<String, RegisterClassBean> map) {
        this.registerClassBeanMap = map;
    }

    @Override
    public void process(Set<? extends Element> elements) throws ProcessingException {
        for (Element element : elements) {

            String serviceCanonicalName = ProcessorUtils.getServiceCanonicalName(element, getAnnotationClass());

            //包裹@LBind修饰的域的类，比如checkApple所在的类MainActivity
            String enclosingClassName = ProcessorUtils.getEnclosingClassName(element);

            RegisterClassBean registerClassBean = ProcessorUtils.chooseOrCreateRegisterClassBean(registerClassBeanMap,
                    enclosingClassName);

            addBindField(registerClassBean, serviceCanonicalName, element.toString());

        }
    }

}
