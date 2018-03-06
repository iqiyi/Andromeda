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
 * //TODO 考虑一个问题，就是如果有多个地方出现对于同一个接口的@LBind和@LRegister怎么办？
 * //TODO 考虑到这个问题的话，必须引入Scope的概念，即在同一个Scope中对于一个接口，只能有一个@LBind和@LRegister
 * //TODO 如果一个Scope中对于一个接口，出现多个@LRegister和@LBind,那就有问题，需要抛出异常提示!
 * //TODO 对于这个问题，lancet应该也遇到了，看下它是怎么解决的。
 * //TODO 第一期就强制只允许出现一次好了，Scope的问题比较复杂，到第二期再解决！
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
