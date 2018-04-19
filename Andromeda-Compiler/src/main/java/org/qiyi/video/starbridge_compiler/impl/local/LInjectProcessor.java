package org.qiyi.video.starbridge_compiler.impl.local;

import org.qiyi.video.starbridge_annotations.local.LInject;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.impl.InjectProcessor;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Created by wangallen on 2018/3/5.
 */

public class LInjectProcessor extends InjectProcessor {

    public LInjectProcessor(Map<String, RegisterClassBean> map) {
        super(map);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return LInject.class;
    }

    @Override
    public void addField(RegisterClassBean registerClassBean,
                         String serviceCanonicalName, String fieldName) throws ProcessingException {
        registerClassBean.addLocalInjectField(serviceCanonicalName, fieldName);
    }
}
