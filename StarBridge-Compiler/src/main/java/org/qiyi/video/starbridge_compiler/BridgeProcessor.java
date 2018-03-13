package org.qiyi.video.starbridge_compiler;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;

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
import org.qiyi.video.starbridge_compiler.bean.LocalServiceBean;
import org.qiyi.video.starbridge_compiler.bean.RegisterClassBean;
import org.qiyi.video.starbridge_compiler.impl.local.LBindProcessor;
import org.qiyi.video.starbridge_compiler.impl.local.LGetProcessor;
import org.qiyi.video.starbridge_compiler.impl.local.LInjectProcessor;
import org.qiyi.video.starbridge_compiler.impl.local.LRegisterProcessor;
import org.qiyi.video.starbridge_compiler.impl.local.LUnRegisterProcessor;
import org.qiyi.video.starbridge_compiler.impl.remote.RBindProcessor;
import org.qiyi.video.starbridge_compiler.impl.remote.RGetProcessor;
import org.qiyi.video.starbridge_compiler.impl.remote.RInjectProcessor;
import org.qiyi.video.starbridge_compiler.impl.remote.RRegisterProcessor;
import org.qiyi.video.starbridge_compiler.impl.remote.RUnRegisterProcessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
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
import javax.lang.model.element.TypeElement;
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

    /**
     * key为classCanonicalName, value为RegisterClassBean
     */
    private Map<String, RegisterClassBean> registerClassBeanMap = new HashMap<>();

    private ElementProcessor lBindProcessor;
    private ElementProcessor lRegisterProcessor;
    private ElementProcessor lUnRegisterProcessor;

    private ElementProcessor lInjectProcessor;
    private ElementProcessor lGetProcessor;

    private ElementProcessor rBindProcessor;
    private ElementProcessor rRegisterProcessor;
    private ElementProcessor rUnRegisterProcessor;

    private ElementProcessor rInjectProcessor;
    private ElementProcessor rGetProcessor;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();

        lBindProcessor = new LBindProcessor(registerClassBeanMap);
        lRegisterProcessor = new LRegisterProcessor(registerClassBeanMap);
        lUnRegisterProcessor = new LUnRegisterProcessor(registerClassBeanMap);

        lInjectProcessor = new LInjectProcessor(registerClassBeanMap);
        lGetProcessor = new LGetProcessor(registerClassBeanMap);

        rBindProcessor = new RBindProcessor(registerClassBeanMap);
        rRegisterProcessor = new RRegisterProcessor(registerClassBeanMap);
        rUnRegisterProcessor = new RUnRegisterProcessor(registerClassBeanMap);

        rInjectProcessor = new RInjectProcessor(registerClassBeanMap);
        rGetProcessor = new RGetProcessor(registerClassBeanMap);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {

        Set<String> annotations = new LinkedHashSet<>();
        //local
        annotations.add(LBind.class.getCanonicalName());
        annotations.add(LRegister.class.getCanonicalName());
        annotations.add(LUnRegister.class.getCanonicalName());
        annotations.add(LInject.class.getCanonicalName());
        annotations.add(LGet.class.getCanonicalName());

        //remote
        annotations.add(RBind.class.getCanonicalName());
        annotations.add(RRegister.class.getCanonicalName());
        annotations.add(RUnRegister.class.getCanonicalName());
        annotations.add(RInject.class.getCanonicalName());
        annotations.add(RGet.class.getCanonicalName());

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
            //processLBind(lBindElements);
            lBindProcessor.process(lBindElements);
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unpected error in BridgeProcessor:" + ex);
            return false;
        }

        Set<? extends Element> lRegisterElements = roundEnv.getElementsAnnotatedWith(LRegister.class);
        try {
            //processLRegister(lRegisterElements);
            lRegisterProcessor.process(lRegisterElements);
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unpected error in BridgeProcessor:" + ex);
            return false;
        }

        try {
            lUnRegisterProcessor.process(roundEnv.getElementsAnnotatedWith(LUnRegister.class));
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in LUnRegisterProcessor:" + ex);
            return false;
        }

        Set<? extends Element> lInjectElements = roundEnv.getElementsAnnotatedWith(LInject.class);
        try {
            lInjectProcessor.process(lInjectElements);
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unpected error in BridgeProcessor:" + ex);
            return false;
        }


        Set<? extends Element> lGetElements = roundEnv.getElementsAnnotatedWith(LGet.class);
        try {
            lGetProcessor.process(lGetElements);
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unpected error in when processing @LGet:" + ex);
            return false;
        }

        /////////////////////下面开始是remote服务的添加了
        try {
            rBindProcessor.process(roundEnv.getElementsAnnotatedWith(RBind.class));
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in RBindProcessor:" + ex);
            return false;
        }

        try {
            rRegisterProcessor.process(roundEnv.getElementsAnnotatedWith(RRegister.class));
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in RRegisterProcessor:" + ex);
            return false;
        }

        try {
            rUnRegisterProcessor.process(roundEnv.getElementsAnnotatedWith(RUnRegister.class));
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in RUnRegisterProcessor:" + ex);
            return false;
        }

        try {
            rInjectProcessor.process(roundEnv.getElementsAnnotatedWith(RInject.class));
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in RInjectProcessor:" + ex);
            return false;
        }

        try {
            rGetProcessor.process(roundEnv.getElementsAnnotatedWith(RGet.class));
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in RGetProcessor:" + ex);
            return false;
        }

        String fileName = "local_service_register_info.json";
        //TODO 然后是不是要生成json文件保存起来？到gradle插件时再使用
        saveLocalServiceInfo(fileName);

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

            //TODO 但是一次性转换为字符串然后再写入，容易出现OOM吧？所以第二期需要优化，使用okio或者逐条记录写入。
            //TODO 不能这样写入，需要以list的方式写入，然后再以list的方式读出
            for (Map.Entry<String, RegisterClassBean> entry : registerClassBeanMap.entrySet()) {
                writer.write(gson.toJson(entry.getValue()));
                writer.write("\n");
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


    private LocalServiceBean chooseRightBean(String serviceName, String registerClassName) throws ProcessingException {
        List<LocalServiceBean> beanList = localServiceBeanMap.get(serviceName);
        if (null == beanList) {
            throw new ProcessingException("No field annotated by @LBind(" + serviceName + ")!");
        }

        LocalServiceBean bean = null;
        for (LocalServiceBean localServiceBean : beanList) {
            if (localServiceBean.getEnclosingClassName().equals(registerClassName)) {
                bean = localServiceBean;
                break;
            }
        }

        if (bean == null) {
            throw new ProcessingException("No field annotated by @LBind(" + serviceName + ") in the same class! Attention:Inner class is not supported now!");
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

}
