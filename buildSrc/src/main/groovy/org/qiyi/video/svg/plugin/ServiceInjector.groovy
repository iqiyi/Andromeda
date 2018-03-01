package org.qiyi.video.svg.plugin

import com.google.gson.Gson
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project
import org.qiyi.video.svg.plugin.bean.LocalServiceBean
import org.qiyi.video.svg.plugin.bean.MethodBean
import org.qiyi.video.svg.plugin.bean.MethodWrapper

public class ServiceInject {

    //private static final ClassPool pool = ClassPool.getDefault()
    //private ClassPool pool=new ClassPool(true)
    private ClassPool pool

    private Gson gson
    private List<LocalServiceBean> localServiceBeanList = new ArrayList<>()
    //key为registerClassName
    //private Map<String,LocalServiceBean>methodBeanMap=new HashMap<>()
    //private Map<String, List<LocalServiceBean>> methodBeanMap = new HashMap<>()
    //private Map<String, Set<MethodBean>> methodBeanMap = new HashMap<>();
    //key是enclosingClassName,value是所有注册在这个类中的
    private Map<String, Set<LocalServiceBean>> localServiceBeanMap = new HashMap<>()

    private File buildDir
    private Map<String, CtClass> paramClassCache = new HashMap<>()

    private boolean appendBootFlag = false

    public ServiceInject(Project project, ClassPool pool) {
        this.buildDir = project.buildDir
        this.pool = pool
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
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))
            String content
            while ((content = reader.readLine()) != null) {
                LocalServiceBean bean = gson.fromJson(content, LocalServiceBean.class)

                localServiceBeanList.add(bean)

                if (localServiceBeanMap.get(bean.getEnclosingClassName()) == null) {
                    Set<LocalServiceBean> beanSet = new HashSet<>()
                    localServiceBeanMap.put(bean.getEnclosingClassName(), beanSet)
                }
                localServiceBeanMap.get(bean.getEnclosingClassName()).add(bean)
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

    /**
     * 在一个类中注入所有它的注册代码
     * @param localServiceBeans
     * @param path
     */
    private void injectRegisterInfoForOneClass(String className, Set<LocalServiceBean> localServiceBeans, String path) {
        CtClass ctClass = pool.getCtClass(className)
        if (ctClass == null) {
            throw new Exception(className + " cannot be loaded!")
        }
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }
        //考虑到重载，所以value是List<MethodWrapper>
        Map<String, List<MethodWrapper>> methodWrappers = new HashMap<>()
        //TODO 两种方案，一种是逐个MethodBean遍历，还有一种是把在同一个方法中进行Register的MethodBean都集合到一起，然后逐个插入，这种效率应该会更高一点!
        for (LocalServiceBean localServiceBean : localServiceBeans) {
            String serviceImplField = localServiceBean.getServiceImplField()
            String serviceCanonicalName = localServiceBean.getServiceCanonicalName()

            for (MethodBean methodBean : localServiceBean.getMethodBeanList()) {

                println("----------------methodName:"+methodBean.getMethodName()+"------------------")

                MethodWrapper wrapper = chooseMethodWrapper(methodBean, methodWrappers)
                CtMethod ctMethod

                if (wrapper == null) {
                    wrapper = new MethodWrapper()
                    wrapper.setMethodName(methodBean.getMethodName())
                    wrapper.setParameterTypeNames(methodBean.getParameterTypeNames())

                    ctMethod = createCtMethod(ctClass, methodBean)
                    wrapper.setMethod(ctMethod)
                }else{
                    ctMethod=wrapper.getMethod()
                    println("-----------found one cached MethodWrapper!---------")
                }

                if (methodWrappers.get(methodBean.getMethodName()) == null) {
                    methodWrappers.put(methodBean.getMethodName(), new ArrayList<MethodWrapper>())
                }

                methodWrappers.get(methodBean.getMethodName()).add(wrapper)

                String registerLocalServiceCode = "org.qiyi.video.svg.ServiceRouter.getInstance().registerLocalService(" + serviceCanonicalName +
                        ".class," + serviceImplField + ");"
                ctMethod.insertAfter(registerLocalServiceCode)
            }
        }

        ctClass.writeFile(path)

    }

    private MethodWrapper chooseMethodWrapper(MethodBean bean, Map<String, List<MethodWrapper>> methodWrappers) {
        List<MethodWrapper> wrappers = methodWrappers.get(bean.getMethodName())
        for (MethodWrapper wrapper : wrappers) {
            if (isSameParameters(bean, wrapper)) {
                return wrapper
            }
        }
        return null
    }

    private boolean isSameParameters(MethodBean methodBean, MethodWrapper methodWrapper) {
        if(methodBean.getParameterTypeNames()==null&&methodWrapper.getParameterTypeNames()==null){
            return true;
        }
        for (int i = 0; i < methodBean.getParameterTypeNames().size(); ++i) {
            if (!methodBean.getParameterTypeNames().get(i).equals(methodWrapper.getParameterTypeNames().get(i))) {
                return false
            }
        }
        return true
    }

    private CtMethod createCtMethod(CtClass ctClass, MethodBean methodBean) {
        CtClass[] paramClasses = null
        if (methodBean.getParameterTypeNames() != null &&
                methodBean.getParameterTypeNames().size() > 0) {
            paramClasses = new CtClass[methodBean.getParameterTypeNames().size()]
            int i = 0
            for (String paramTypeName : methodBean.getParameterTypeNames()) {
                CtClass paramType
                if (paramClassCache.get(paramTypeName) == null) {
                    paramType = pool.getCtClass(paramTypeName)
                    paramClassCache.put(paramTypeName, paramType)
                } else {
                    paramType = paramClassCache.get(paramTypeName)
                }
                paramClasses[i++] = paramType
            }
        }


        CtMethod ctMethod
        if (paramClasses == null) {
            ctMethod = ctClass.getDeclaredMethod(methodBean.getMethodName())
        } else {
            ctMethod = ctClass.getDeclaredMethod(methodBean.getMethodName(), paramClasses)
        }
        return ctMethod
    }

    /*
    private void injectSingleRegisterInfo(MethodBean methodBean, String path) {
        CtClass ctClass = pool.getCtClass(methodBean.getRegisterClassName())
        if (ctClass == null) {
            return
        }
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }
        CtClass[] paramClasses = null

        if (methodBean.getParameterTypeNames() != null &&
                methodBean.getParameterTypeNames().size() > 0) {
            paramClasses = new CtClass[methodBean.getParameterTypeNames().size()]
            int i = 0
            for (String paramTypeName : methodBean.getParameterTypeNames()) {
                CtClass paramType
                if (paramClassCache.get(paramTypeName) == null) {
                    paramType = pool.getCtClass(paramTypeName)
                    paramClassCache.put(paramTypeName, paramType)
                } else {
                    paramType = paramClassCache.get(paramTypeName)
                }
                paramClasses[i++] = paramType
            }
        }


        CtMethod ctMethod
        if (paramClasses == null) {
            ctMethod = ctClass.getDeclaredMethod(methodBean.getMethodName())
        } else {
            ctMethod = ctClass.getDeclaredMethod(methodBean.getMethodName(), paramClasses)
        }

        if (ctMethod == null) {
            return
        }
        println("-----------------methodName:" + methodBean.getMethodName() + ",field:" + methodBean.getServiceImplField() + "------------")


        String registerLocalServiceCode = "org.qiyi.video.svg.ServiceRouter.getInstance().registerLocalService(" + methodBean.getServiceCanonicalName() +
                ".class," + methodBean.getServiceImplField() + ");"


        ctMethod.insertAfter(registerLocalServiceCode)
        ctClass.writeFile(path)
        //TODO 是不是还不能detach()呢？因为可能它需要作为后面某个方法的参数!
        //ctClass.detach()
    }
    */

    //TODO 不用的类要即使detach,否则编译时容易OOM
    void injectRegisterInfo(String path, Project project) {
        /*
        //加入anadroid.jar,不然找不到android相关的所有类
        pool.injectRegisterInfo(project.android.bootClasspath[0].toString())
        //引入android.os.Bundle包
        pool.importPackage("android.os.Bundle")

        //将当前路径加入类池，不然找不到这个类
        pool.injectRegisterInfo(path)
        */

        File dir = new File(path)
        if (dir.isDirectory()) {

            //遍历文件夹 //debug发现file是类似"/Users/wangallen/AndroidStudioProjects/ServiceManager/app/build/intermediates/classes/debug/wang/imallen/blog/applemodule/R.class
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                //println("----------------filePath:" + filePath + "---------------")

                String classNameTemp = filePath.replace(path, "")
                        .replace("\\", ".")
                        .replace("/", ".")
                //TODO 这里有个问题，如果是内部类，比如wang.imallen.blog.servicemanager.MainActivity$Apple，不应该是wang.imallen.blog.serviceManager.MainActivity.Apple
                if (classNameTemp.endsWith(".class")) {
                    //TODO 这是全路径类名还是简单类名?
                    String className = classNameTemp.substring(1, classNameTemp.length() - 6)

                    if (localServiceBeanMap.get(className) != null) {

                        println("-----------className:"+className+"---------------")

                        Set<LocalServiceBean> localServiceBeans = localServiceBeanMap.get(className)
                        injectRegisterInfoForOneClass(className,localServiceBeans,path)
                    }
                }
            }

        }
    }


    private void insertRemoteService() {
        //TODO 远程服务的注册还没完成
    }

}