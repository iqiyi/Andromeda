package org.qiyi.video.starbridge_compiler.impl.remote;

import org.qiyi.video.starbridge_annotations.remote.RBind;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.impl.BindProcessor;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Created by wangallen on 2018/3/5.
 */

public class RBindProcessor extends BindProcessor {

    public RBindProcessor(Map<String, RegisterClassBean> registerClassBeanMap) {
        super(registerClassBeanMap);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return RBind.class;
    }

    @Override
    public void addBindField(RegisterClassBean registerClassBean, String serviceCanonicalName, String fieldName) throws ProcessingException {
        registerClassBean.addRemoteBindField(serviceCanonicalName, fieldName);
    }
}
