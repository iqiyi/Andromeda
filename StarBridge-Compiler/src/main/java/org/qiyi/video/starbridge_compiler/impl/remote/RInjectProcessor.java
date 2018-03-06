package org.qiyi.video.starbridge_compiler.impl.remote;

import org.qiyi.video.starbridge_annotations.remote.RInject;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.impl.InjectProcessor;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Created by wangallen on 2018/3/5.
 */

public class RInjectProcessor extends InjectProcessor {

    public RInjectProcessor(Map<String, RegisterClassBean> beanMap) {
        super(beanMap);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return RInject.class;
    }

    @Override
    public void addField(RegisterClassBean registerClassBean, String serviceCanonicalName, String fieldName) throws ProcessingException {
        registerClassBean.addRemoteInjectField(serviceCanonicalName, fieldName);
    }
}
