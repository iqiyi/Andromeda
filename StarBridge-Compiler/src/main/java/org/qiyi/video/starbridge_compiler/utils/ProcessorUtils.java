package org.qiyi.video.starbridge_compiler.utils;

import org.qiyi.video.starbridge_annotations.local.LBind;
import org.qiyi.video.starbridge_annotations.local.LGet;
import org.qiyi.video.starbridge_annotations.local.LInject;
import org.qiyi.video.starbridge_annotations.local.LRegister;
import org.qiyi.video.starbridge_annotations.local.LUnRegister;
import org.qiyi.video.starbridge_annotations.remote.RBind;
import org.qiyi.video.starbridge_annotations.remote.RGet;
import org.qiyi.video.starbridge_annotations.remote.RInject;
import org.qiyi.video.starbridge_annotations.remote.RRegister;
import org.qiyi.video.starbridge_annotations.remote.RUnRegister;
import org.qiyi.video.starbridge_compiler.ProcessingException;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

/**
 * Created by wangallen on 2018/3/5.
 */

public final class ProcessorUtils {
    private ProcessorUtils() {
    }

    public static String replaceSingleDot(Element outerElement, String registerClassName) {
        //if (outerElement != null && outerElement instanceof TypeElement) {
        //要把最后一个"."替换为"$"
        int index = registerClassName.lastIndexOf('.');
        if (index > 0) {
            registerClassName = registerClassName.substring(0, index) + "$" + registerClassName.substring(index + 1, registerClassName.length());
        }
        //}
        return registerClassName;
    }

    public static String getServiceCanonicalName(Element element, Class<? extends Annotation> annotationClass) throws ProcessingException {
        if (element.getKind() != ElementKind.FIELD) {
            throw new ProcessingException(element, "Only fields can be annotated with @%s", annotationClass.getSimpleName());
        }
        //TypeElement enclosingElement=(TypeElement)element.getEnclosingElement();
        String serviceCanonicalName = null;

        //debug发现tmp的值是变量名，类似"checkApple"这样的
        //String tmp = element.toString();

        try {
            //Class<?> service = element.getAnnotation(annotationClass).value();
            Class<?> service = null;
            if (annotationClass == LBind.class) {
                service = element.getAnnotation(LBind.class).value();
            } else if (annotationClass == LInject.class) {
                service = element.getAnnotation(LInject.class).value();
            } else if (annotationClass == RBind.class) {
                service = element.getAnnotation(RBind.class).value();
            } else if (annotationClass == RInject.class) {
                service = element.getAnnotation(RInject.class).value();
            } else {
                throw new ProcessingException(annotationClass.getSimpleName() + " is not supported yet!");
            }
            serviceCanonicalName = service.getCanonicalName();
        } catch (MirroredTypeException mte) {
            DeclaredType serviceType = (DeclaredType) mte.getTypeMirror();
            serviceCanonicalName = serviceType.toString();
            //tmp = serviceType.asElement().toString();  //打log发现tmp的值也是"wang.imallen.blog.moduleexportlib.apple.ICheckApple"
        }

        //debug发现tmp的值是wang.imallen.blog.servicemanager.MainActivity
        //tmp=element.getEnclosingElement().toString();

        if (serviceCanonicalName == null || serviceCanonicalName.equals("java.lang.Object")) {
            //debug发现采用这种方式可以获取到成员变量的类名(全路径名)
            serviceCanonicalName = element.asType().toString();
        }
        return serviceCanonicalName;
    }

    public static String getEnclosingClassName(Element element) throws ProcessingException {
        Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement == null) {
            throw new ProcessingException(element.toString() + " must enclosed by Class!");
        }
        return enclosingElement.toString();
    }

    public static RegisterClassBean chooseOrCreateRegisterClassBean(Map<String, RegisterClassBean> registerClassBeanMap,
                                                                    String enclosingClassName) {
        RegisterClassBean registerClassBean;
        if (registerClassBeanMap.get(enclosingClassName) == null) {
            registerClassBean = new RegisterClassBean();
            registerClassBean.setRegisterClassName(enclosingClassName);
            registerClassBeanMap.put(enclosingClassName, registerClassBean);
        } else {
            registerClassBean = registerClassBeanMap.get(enclosingClassName);
        }
        return registerClassBean;
    }

    public static Set<String> getServiceNameSet(Element element, Class<?> annotationClass) throws ProcessingException {
        if (element.getKind() != ElementKind.METHOD) {
            throw new ProcessingException(element, "Only methods can be annotated with @%s", annotationClass.getSimpleName());
        }
        Set<String> serviceNameSet = new HashSet<>();
        try {
            //Class<?>[] services = element.getAnnotation(LRegister.class).value();
            Class<?>[] services = null;
            if (annotationClass == LRegister.class) {
                services = element.getAnnotation(LRegister.class).value();
            } else if (annotationClass == LUnRegister.class) {
                services = element.getAnnotation(LUnRegister.class).value();
            } else if (annotationClass == RRegister.class) {
                services = element.getAnnotation(RRegister.class).value();
            } else if (annotationClass == RUnRegister.class) {
                services = element.getAnnotation(RUnRegister.class).value();
            } else if (annotationClass == LGet.class) {
                services = element.getAnnotation(LGet.class).value();
            } else if (annotationClass == RGet.class) {
                services = element.getAnnotation(RGet.class).value();
            } else {
                throw new ProcessingException(annotationClass.getSimpleName() + " is not supported yet!");
            }
            for (Class<?> clazz : services) {
                serviceNameSet.add(clazz.getCanonicalName());
            }
        } catch (MirroredTypesException mte) {  //注意:由于LRegister的value对应的是Class<?>[],所以这里是MirroredTypesException而不是MirroredTypeException
            List<? extends TypeMirror> typeMirrors = mte.getTypeMirrors();
            for (TypeMirror typeMirror : typeMirrors) {
                serviceNameSet.add(typeMirror.toString());
            }
        }
        return serviceNameSet;
    }

    /**
     * 如果是内部类的话，会将.替换为$，这样之后给gradle插件使用时才有效
     *
     * @return
     */
    public static String getRegisterClassName(Element enclosingElement) {
        Element outerElement = enclosingElement.getEnclosingElement();
        String registerClassName = enclosingElement.toString();
        while (outerElement != null && outerElement instanceof TypeElement) {
            registerClassName = ProcessorUtils.replaceSingleDot(outerElement, registerClassName);
            outerElement = outerElement.getEnclosingElement();
        }
        return registerClassName;
    }


}
