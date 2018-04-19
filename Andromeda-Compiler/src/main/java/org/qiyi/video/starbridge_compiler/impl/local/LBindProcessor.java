package org.qiyi.video.starbridge_compiler.impl.local;

import org.qiyi.video.starbridge_annotations.local.LBind;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.impl.BindProcessor;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 利用它来获取serviceCanonicalName
 * Created by wangallen on 2018/3/5.
 */

public class LBindProcessor extends BindProcessor {

    @Override
    public void addBindField(RegisterClassBean registerClassBean,
                             String serviceCanonicalName, String fieldName) throws ProcessingException {
        registerClassBean.addLocalBindField(serviceCanonicalName, fieldName);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return LBind.class;
    }

    public LBindProcessor(Map<String, RegisterClassBean> registerClassBeanMap) {
        super(registerClassBeanMap);
    }
}
