package org.qiyi.video.svg.plugin

import com.google.gson.Gson
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

public class ServiceInject {

    //private static final ClassPool pool = ClassPool.getDefault()
    //private ClassPool pool=new ClassPool(true)
    private ClassPool pool

    private Gson gson
    private List<LocalServiceBean> localServiceBeanList = new ArrayList<>()
    //key为registerClassName
    //private Map<String,LocalServiceBean>localServiceBeanMap=new HashMap<>()
    private Map<String, List<LocalServiceBean>> localServiceBeanMap = new HashMap<>()

    private File buildDir
    private Map<String, CtClass> paramClassCache = new HashMap<>()

    private boolean appendBootFlag = false

    public ServiceInject(Project project,ClassPool pool) {
        this.buildDir = project.buildDir
        this.pool=pool
        readLocalServiceInfo("local_service_register_info.json")
    }

    private void readLocalServiceInfo(String fileName) {
        if (gson == null) {
            gson = new Gson()
        }

        //代表当前目录
        //File directory = new File(DIR)
        println(buildDir.absolutePath)

        File file = new File(buildDir, fileName)
        BufferedReader reader = null
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String content
            while ((content = reader.readLine()) != null) {
                //localServiceBeanList.add(gson.fromJson(content, LocalServiceBean.class));
                LocalServiceBean bean = gson.fromJson(content, LocalServiceBean.class)

                println("----------LocalServiceBean,methodName:"+bean.getMethodBean().getMethodName()+"--------------")

                localServiceBeanList.add(bean)
                //TODO 这里有个问题，就是有可能一个registerClassName对应多个LocalServiceBean
                //localServiceBeanMap.put(bean.getRegisterClassName(),bean)
                if (localServiceBeanMap.get(bean.getRegisterClassName()) == null) {
                    localServiceBeanMap.put(bean.getRegisterClassName(), new ArrayList<LocalServiceBean>())
                }
                localServiceBeanMap.get(bean.getRegisterClassName()).add(bean)
            }

        } catch (IOException ex) {
            ex.printStackTrace()
        } finally {
            try {
                if (reader != null) {
                    reader.close()
                }
            } catch (IOException ex) {
                ex.printStackTrace()
            }
        }
    }

    //TODO 不用的类要即使detach,否则编译时容易OOM
    void appendClassPath(String path, Project project) {
        /*
        //加入anadroid.jar,不然找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString())
        //引入android.os.Bundle包
        pool.importPackage("android.os.Bundle")

        //将当前路径加入类池，不然找不到这个类
        pool.appendClassPath(path)
        */

        File dir = new File(path)
        if (dir.isDirectory()) {

            //遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                println("----------------filePath:" + filePath + "---------------")

                String classNameTemp = filePath.replace(path, "")
                        .replace("\\", ".")
                        .replace("/", ".")
                if (classNameTemp.endsWith(".class")) {
                    //TODO 这是全路径类名还是简单类名?
                    String className = classNameTemp.substring(1, classNameTemp.length() - 6)

                    println("---------------className:" + className + "-----------------")

                    if (localServiceBeanMap.get(className) != null) {
                        for (LocalServiceBean bean : localServiceBeanMap.get(className)) {
                            CtClass ctClass = pool.getCtClass(bean.getRegisterClassName())
                            if (ctClass == null) {
                                continue
                            }
                            if (ctClass.isFrozen()) {
                                ctClass.defrost()
                            }
                            CtClass[] paramClasses = null
                            if (bean.getMethodBean().getParameterTypeNames() != null &&
                                    bean.getMethodBean().getParameterTypeNames().size() > 0) {
                                paramClasses = new CtClass[bean.getMethodBean().getParameterTypeNames().size()]
                                int i = 0
                                for (String paramTypeName : bean.getMethodBean().getParameterTypeNames()) {
                                    CtClass paramType
                                    if(paramClassCache.get(paramTypeName)==null){
                                        paramType=pool.getCtClass(paramTypeName)
                                        paramClassCache.put(paramTypeName,paramType)
                                    }else{
                                        paramType=paramClassCache.get(paramTypeName)
                                    }
                                    paramClasses[i++] = paramType
                                }
                            }

                            CtMethod ctMethod
                            if(paramClasses==null){
                                ctMethod=ctClass.getDeclaredMethod(bean.getMethodBean().getMethodName())
                            }else{
                                ctMethod= ctClass.getDeclaredMethod(bean.getMethodBean().getMethodName(), paramClasses)
                            }

                            if (ctMethod == null) {
                                continue
                            }
                            println("methodName:" + ctMethod)

                            String registerLocalServiceCode = "org.qiyi.video.svg.ServiceRouter.getInstance().registerLocalService(" + bean.getServiceCanonicalName() +
                                    ".class," + bean.getServiceImplField() + ");"

                            ctMethod.insertAfter(registerLocalServiceCode)
                            ctClass.writeFile(path)
                            ctClass.detach()
                        }
                    }
                }

            }
        }

    }


    void injectService() {

        insertLocalService()

        insertRemoteService()
    }


    //TODO 这样分类不好，还是要按registerClassName来逐个进行分析
    private void insertLocalService() {
        for (LocalServiceBean bean : localServiceBeanList) {
            CtClass ctClass = pool.getCtClass(bean.getRegisterClassName())
            if (ctClass == null) {
                continue
            }
            if (ctClass.isFrozen()) {
                ctClass.defrost()
            }
            CtClass[] paramClasses = null
            if (bean.getMethodBean().getParameterTypeNames() != null &&
                    bean.getMethodBean().getParameterTypeNames().size() > 0) {
                paramClasses = new CtClass[bean.getMethodBean().getParameterTypeNames().size()]
                int i = 0
                for (String paramTypeName : bean.getMethodBean().getParameterTypeNames()) {
                    CtClass paramType
                    if(paramClassCache.get(paramTypeName)==null){
                        paramType=pool.getCtClass(paramTypeName)
                        paramClassCache.put(paramTypeName,paramType)
                    }else{
                        paramType=paramClassCache.get(paramTypeName)
                    }
                    paramClasses[i++] = paramType
                }
            }

            CtMethod ctMethod
            if(paramClasses==null){
                ctMethod=ctClass.getDeclaredMethod(bean.getMethodBean().getMethodName())
            }else{
                ctMethod= ctClass.getDeclaredMethod(bean.getMethodBean().getMethodName(), paramClasses)
            }

            if (ctMethod == null) {
                continue
            }
            println("methodName:" + ctMethod)

            String registerLocalServiceCode = "org.qiyi.video.svg.ServiceRouter.getInstance().registerLocalService(" + bean.getServiceCanonicalName() +
                    ".class," + bean.getServiceImplField() + ");"

            ctMethod.insertAfter(registerLocalServiceCode)
            ctClass.writeFile(path)
            ctClass.detach()

        }
    }

    private void insertRemoteService() {
        //TODO
    }

}