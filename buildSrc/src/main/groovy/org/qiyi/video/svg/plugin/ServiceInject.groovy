package org.qiyi.video.svg.plugin

import com.google.gson.Gson
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.expr.ExprEditor
import org.gradle.api.Project
import org.qiyi.video.svg.plugin.bean.LocalServiceBean
import org.qiyi.video.svg.plugin.bean.MethodBean

public class ServiceInject {

    //private static final ClassPool pool = ClassPool.getDefault()
    //private ClassPool pool=new ClassPool(true)
    private ClassPool pool

    private Gson gson
    private List<LocalServiceBean> localServiceBeanList = new ArrayList<>()
    //key为registerClassName
    //private Map<String,LocalServiceBean>methodBeanMap=new HashMap<>()
    //private Map<String, List<LocalServiceBean>> methodBeanMap = new HashMap<>()
    private Map<String, Set<MethodBean>> methodBeanMap = new HashMap<>();

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
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String content
            while ((content = reader.readLine()) != null) {
                //localServiceBeanList.add(gson.fromJson(content, LocalServiceBean.class));
                LocalServiceBean bean = gson.fromJson(content, LocalServiceBean.class)

                localServiceBeanList.add(bean)

                for (MethodBean methodBean : bean.getMethodBeanList()) {
                    methodBean.setServiceImplField(bean.getServiceImplField())
                    methodBean.setServiceCanonicalName(bean.getServiceCanonicalName())

                    if (methodBeanMap.get(methodBean.getRegisterClassName()) == null) {
                        Set<MethodBean> set = new HashSet<>()
                        methodBeanMap.put(methodBean.getRegisterClassName(), set)
                    }
                    methodBeanMap.get(methodBean.getRegisterClassName()).add(methodBean)
                }

                /*
                if (methodBeanMap.get(bean.getServiceCanonicalName()) == null) {
                    methodBeanMap.put(bean.getServiceCanonicalName(), new ArrayList<LocalServiceBean>())
                }
                methodBeanMap.get(bean.getServiceCanonicalName()).add(bean)
                */
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

    private void injectSingleRegisterInfo(MethodBean methodBean,String path) {
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
        println("-----------------methodName:" + methodBean.getMethodName()+",field:"+methodBean.getServiceImplField()+"------------")


        String registerLocalServiceCode = "org.qiyi.video.svg.ServiceRouter.getInstance().registerLocalService(" + methodBean.getServiceCanonicalName() +
                ".class," + methodBean.getServiceImplField() + ");"


        ctMethod.insertAfter(registerLocalServiceCode)
        ctClass.writeFile(path)
        //TODO 是不是还不能detach()呢？因为可能它需要作为后面某个方法的参数!
        //ctClass.detach()
    }

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


                    //TODO 这里的逻辑需要变一下，其实要等到出现registerClassName的时候才进行代码插入
                    if (methodBeanMap.get(className) != null) {
                        println("---------------className:" + className + "-----------------")
                        Set<MethodBean> methodBeanSet = methodBeanMap.get(className)
                        for (MethodBean methodBean : methodBeanSet) {
                            injectSingleRegisterInfo(methodBean,path)
                        }

                    }
                    /*
                   for (LocalServiceBean localServiceBean : methodBeanMap.get(className)) {

                       for (MethodBean methodBean : localServiceBean.getMethodBeanList()) {
                           injectSingleRegisterInfo(methodBean)
                       }
                   */
                    /*
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
                            if (paramClassCache.get(paramTypeName) == null) {
                                paramType = pool.getCtClass(paramTypeName)
                                paramClassCache.put(paramTypeName, paramType)
                            } else {
                                paramType = paramClassCache.get(paramTypeName)
                            }

                            //paramType.getAnnotations()
                            paramClasses[i++] = paramType
                        }
                    }

                    CtMethod ctMethod
                    if (paramClasses == null) {
                        ctMethod = ctClass.getDeclaredMethod(bean.getMethodBean().getMethodName())
                    } else {
                        ctMethod = ctClass.getDeclaredMethod(bean.getMethodBean().getMethodName(), paramClasses)
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
                 */

                }
            }

        }
    }


    void injectService(String path) {

        //insertLocalService()
        injectLocalService(path)

        insertRemoteService()
    }

/**
 * 注入本地服务的注册信息
 */
    private void injectLocalService(String path) {
        for (LocalServiceBean localServiceBean : localServiceBeanList) {
            for (MethodBean methodBean : localServiceBean.getMethodBeanList()) {
                CtClass ctClass = pool.getCtClass(methodBean.getRegisterClassName())
                if (ctClass == null) {
                    continue
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
                    continue
                }
                println("-----------------methodName:" + methodBean.getMethodName()+"---------------")

                String registerLocalServiceCode = "org.qiyi.video.svg.ServiceRouter.getInstance().registerLocalService(" + localServiceBean.getServiceCanonicalName() +
                        ".class," + localServiceBean.getServiceImplField() + ");"

                ctMethod.insertAfter(registerLocalServiceCode)
                ctClass.writeFile(path)
                //TODO 是不是还不能detach()呢？因为可能它需要作为后面某个方法的参数!
                ctClass.detach()
            }
        }
    }

//TODO 这样分类不好，还是要按registerClassName来逐个进行分析
/*
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
            ctMethod = ctClass.getDeclaredMethod(bean.getMethodBean().getMethodName())
        } else {
            ctMethod = ctClass.getDeclaredMethod(bean.getMethodBean().getMethodName(), paramClasses)
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
*/

    private void insertRemoteService() {
        //TODO 远程服务的注册还没完成
    }

}