package org.qiyi.video.starbridge_compiler;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;

import org.qiyi.video.starbridge_annotations.local.LBind;
import org.qiyi.video.starbridge_annotations.local.LInject;
import org.qiyi.video.starbridge_annotations.local.LRegister;
import org.qiyi.video.starbridge_annotations.local.LUnRegister;
import org.qiyi.video.starbridge_annotations.remote.RBind;
import org.qiyi.video.starbridge_annotations.remote.RInject;
import org.qiyi.video.starbridge_annotations.remote.RRegister;
import org.qiyi.video.starbridge_annotations.remote.RUnRegister;
import org.qiyi.video.starbridge_compiler.bean.MethodBean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class BridgeProcessor extends AbstractProcessor {

    private static final String DIR = "./app/build/";

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    boolean isFirst = true;
    private Gson gson;

    //最后根据这个数据生成一个配置文件，放在这个配置文件的static域中即可
    //TODO 应该是List<Entry<String,LocalServiceBean>>这样的更合适？因为一个服务有可能在多个地方注册!
    //TODO 另外，是不是还要考虑在一个类中，一个服务的对象有可能在多个方法中注册？
    //private Map<String, LocalServiceBean> localServiceBeanMap = new HashMap<>();
    //考虑到一个服务可能在多个类中注册，所以value是List<LocalServiceBean>, key为serviceCanonicalName, value为List<LocalServiceBean>
    private Map<String, List<LocalServiceBean>> localServiceBeanMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {

        Set<String> annotations = new LinkedHashSet<>();
        //local
        annotations.add(LBind.class.getCanonicalName());
        annotations.add(LRegister.class.getCanonicalName());
        annotations.add(LUnRegister.class.getCanonicalName());
        annotations.add(LInject.class.getCanonicalName());

        //remote
        annotations.add(RBind.class.getCanonicalName());
        annotations.add(RRegister.class.getCanonicalName());
        annotations.add(RUnRegister.class.getCanonicalName());
        annotations.add(RInject.class.getCanonicalName());

        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    //注意:process有可能被回调多次!那要如何区分呢?
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!isFirst) {
            return false;
        }
        isFirst = false;

        Set<? extends Element> lBindElements = roundEnv.getElementsAnnotatedWith(LBind.class);
        try {
            processLBind(lBindElements);
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unpected error in BridgeProcessor:" + ex);
            return false;
        }

        Set<? extends Element> lRegisterElements = roundEnv.getElementsAnnotatedWith(LRegister.class);
        try {
            processLRegister(lRegisterElements);
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unpected error in BridgeProcessor:" + ex);
            return false;
        }

        String fileName = "local_service_register_info.json";
        //TODO 然后是不是要生成json文件保存起来？到gradle插件时再使用
        saveLocalServiceInfo(fileName);

        //readLocalServiceInfo(fileName);

        Set<? extends Element> lUnRegisterElements = roundEnv.getElementsAnnotatedWith(LUnRegister.class);
        processLUnRegister(lUnRegisterElements);
        Set<? extends Element> lInjectElements = roundEnv.getElementsAnnotatedWith(LInject.class);
        processLInject(lInjectElements);

        return true;
    }

    //Debug发现permission denied,难道Processor中只能利用file来创建文件?
    //不对，应该也可以创建文件，permission不允许只是因为自己没有那个目录的权限，不信的话换到当前用户的Public目录下试一下，应该就可以!
    private void saveLocalServiceInfo(String fileName) {
        System.out.println("start of saveLocalServiceInfo");
        if (gson == null) {
            gson = new Gson();
        }
        //获取当前路径
        File directory = new File(DIR);

        File file = new File(directory, fileName);
        if (file.exists()) {
            file.delete();
        }
        BufferedWriter writer = null;
        try {
            //每次都创建新文件
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file, false);
            writer = new BufferedWriter(new OutputStreamWriter(fos));

            /*
            JsonWriter jsonWriter = new JsonWriter(writer);
            for (Map.Entry<String, LocalServiceBean> entry : localServiceBeanMap.entrySet()) {
                jsonWriter.beginObject();
                jsonWriter.jsonValue(gson.toJson(entry.getValue()));
                jsonWriter.endObject();
            }

            jsonWriter.close();
            */


            //TODO 但是一次性转换为字符串然后再写入，容易出现OOM吧？所以第二期需要优化，使用okio或者逐条记录写入。
            //TODO 不能这样写入，需要以list的方式写入，然后再以list的方式读出
            for (Map.Entry<String, List<LocalServiceBean>> entry : localServiceBeanMap.entrySet()) {
                //writer.write(gson.toJson)
                for (LocalServiceBean bean : entry.getValue()) {
                    writer.write(gson.toJson(bean));
                    writer.write("\n");
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        System.out.println("end of saveLocalServiceInfo");
    }

    /**
     * 利用它来获取serviceCanonicalName
     *
     * @param elements
     * @throws ProcessingException
     */
    private void processLBind(Set<? extends Element> elements) throws ProcessingException {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.FIELD) {
                throw new ProcessingException(element, "Only fields can be annotated with @%s", LBind.class.getSimpleName());
            }
            //TypeElement enclosingElement=(TypeElement)element.getEnclosingElement();
            String serviceCanonicalName = null;

            String tmp;
            //debug发现tmp的值是变量名，类似"checkApple"这样的
            tmp = element.toString();

            try {
                Class<?> service = element.getAnnotation(LBind.class).value();
                serviceCanonicalName = service.getCanonicalName();
            } catch (MirroredTypeException mte) {
                DeclaredType serviceType = (DeclaredType) mte.getTypeMirror();
                serviceCanonicalName = serviceType.toString();
                tmp = serviceType.asElement().toString();  //打log发现tmp的值也是"wang.imallen.blog.moduleexportlib.apple.ICheckApple"
            }

            //debug发现tmp的值是wang.imallen.blog.servicemanager.MainActivity
            //tmp=element.getEnclosingElement().toString();

            if (serviceCanonicalName == null || serviceCanonicalName.equals("java.lang.Object")) {
                //debug发现采用这种方式可以获取到成员变量的类名(全路径名)
                serviceCanonicalName = element.asType().toString();
            }

            //包裹@LBind修饰的域的类，比如checkApple所在的类MainActivity
            Element enclosingElement = element.getEnclosingElement();
            if (enclosingElement == null) {
                throw new ProcessingException(element.toString() + " must enclosed by Class!");
            }
            String enclosingClassName = enclosingElement.toString();
            /*
            try {
                Class<?> enclosingClass = element.getClass();
                enclosingClassName = enclosingClass.getCanonicalName();
            } catch (MirroredTypeException mte) {
                DeclaredType enclosingType = (DeclaredType) mte.getTypeMirrors();
                enclosingClassName = enclosingType.asElement().toString();
            }
            */

            List<LocalServiceBean> beanList;
            if (localServiceBeanMap.get(serviceCanonicalName) == null) {
                beanList = new ArrayList<>();
                localServiceBeanMap.put(serviceCanonicalName, beanList);
            } else {
                //要检查同一个类中是否有其他相同也是用同一个@LBind+serviceCanonicalName修饰的field，如果有则要抛出异常
                beanList = localServiceBeanMap.get(serviceCanonicalName);
                for (LocalServiceBean bean : beanList) {
                    if (enclosingClassName.equals(bean.getEnclosingClassName())) {
                        throw new ProcessingException("Error! More than one field annotated by " + serviceCanonicalName + " in " + enclosingClassName + "!");
                    }
                }
            }

            LocalServiceBean bean = new LocalServiceBean();
            bean.setServiceCanonicalName(serviceCanonicalName);
            bean.setServiceImplField(element.toString());
            bean.setEnclosingClassName(enclosingClassName);
            beanList.add(bean);
        }
    }

    /**
     * //TODO 考虑一个问题，就是如果有多个地方出现对于同一个接口的@LBind和@LRegister怎么办？
     * //TODO 考虑到这个问题的话，必须引入Scope的概念，即在同一个Scope中对于一个接口，只能有一个@LBind和@LRegister
     * //TODO 如果一个Scope中对于一个接口，出现多个@LRegister和@LBind,那就有问题，需要抛出异常提示!
     * //TODO 对于这个问题，lancet应该也遇到了，看下它是怎么解决的。
     * //TODO 第一期就强制只允许出现一次好了，Scope的问题比较复杂，到第二期再解决！
     *
     * @param elements
     * @throws ProcessingException
     */
    private void processLRegister(Set<? extends Element> elements) throws ProcessingException {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) {
                throw new ProcessingException(element, "Only methods can be annotated with @%s", LRegister.class.getSimpleName());
            }
            Set<String> serviceNameSet = new HashSet<>();
            try {
                Class<?>[] services = element.getAnnotation(LRegister.class).value();
                for (Class<?> clazz : services) {
                    serviceNameSet.add(clazz.getCanonicalName());
                }
            } catch (MirroredTypesException mte) {  //注意:由于LRegister的value对应的是Class<?>[],所以这里是MirroredTypesException而不是MirroredTypeException
                List<? extends TypeMirror> typeMirrors = mte.getTypeMirrors();
                for (TypeMirror typeMirror : typeMirrors) {
                    serviceNameSet.add(typeMirror.toString());
                }
            }

            ExecutableElement methodElement = (ExecutableElement) element;
            Element enclosingElement = element.getEnclosingElement();
            //TODO debug发现registerClassName是"wang.imallen.blog.servicemanager.MainActivity.Apple",
            //TODO 为什么不是"wang.imallen.blog.servicemanager.MainActivity$Apple"呢?

            //TODO 这里其实有个问题，就是可能某个类是内部类的内部类，所以实际上需要循环替换!
            Element outerElement = enclosingElement.getEnclosingElement();
            String registerClassName = enclosingElement.toString();
            while (outerElement != null && outerElement instanceof TypeElement) {
                registerClassName = replaceSingleDot(outerElement, registerClassName);
                outerElement = outerElement.getEnclosingElement();
            }

            String tmp = enclosingElement.asType().toString();  //Debug发现tmp也是"wang.imallen.blog.servicemanager.MainActivity.Apple"

            String methodName = methodElement.getSimpleName().toString();
            List<? extends VariableElement> variableElements = methodElement.getParameters();

            for (String serviceName : serviceNameSet) {
                LocalServiceBean bean = chooseRightBean(serviceName, methodElement);
                MethodBean methodBean = new MethodBean(methodName, variableElements);
                methodBean.setRegisterClassName(registerClassName);
                bean.addMethodBean(methodBean);
            }

        }
    }

    private String replaceSingleDot(Element outerElement, String registerClassName) {
        //if (outerElement != null && outerElement instanceof TypeElement) {
        //要把最后一个"."替换为"$"
        int index = registerClassName.lastIndexOf('.');
        if (index > 0) {
            registerClassName = registerClassName.substring(0, index) + "$" + registerClassName.substring(index + 1, registerClassName.length());
        }
        //}
        return registerClassName;
    }

    private LocalServiceBean chooseRightBean(String serviceName, Element methodElement) throws ProcessingException {
        List<LocalServiceBean> beanList = localServiceBeanMap.get(serviceName);
        if (null == beanList) {
            throw new ProcessingException("No field annotated by @LBind(" + serviceName + ")!");
        }
        LocalServiceBean bean = null;
        Element enclosingElement = methodElement.getEnclosingElement();
        while (enclosingElement != null) {
            bean = getBeanInSameClass(beanList, enclosingElement);
            if (bean != null) {
                break;
            }
            enclosingElement = enclosingElement.getEnclosingElement();
        }
        if (bean == null) {
            throw new ProcessingException("No field annotated by @LBind(" + serviceName + ") in the same or outer class!");
        }
        return bean;
    }

    private LocalServiceBean getBeanInSameClass(List<LocalServiceBean> beanList, Element enclosingElement) {
        for (LocalServiceBean bean : beanList) {
            if (bean.getEnclosingClassName().equals(enclosingElement.toString())) {
                return bean;
            }
        }
        return null;
    }

    private void processLUnRegister(Set<? extends Element> elements) {

    }

    private void processLInject(Set<? extends Element> elements) {


    }

}
