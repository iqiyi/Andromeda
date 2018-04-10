package org.qiyi.video.svg.plugin.injector

import com.google.gson.Gson
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project
import org.qiyi.video.svg.plugin.bean.RegisterClassBean
import org.qiyi.video.svg.plugin.bean.RegisterMethodBean
import org.qiyi.video.svg.plugin.bean.ServiceInfo

public class BizServiceInjector {

    private static final String STAR_BRIDGE_INSTANCE = "org.qiyi.video.svg.StarBridge.getInstance()"

    private ClassPool pool

    private Gson gson

    private Map<String, RegisterClassBean> registerClassBeanMap = new HashMap<>()

    private File buildDir
    private Map<String, CtClass> paramClassCache = new HashMap<>()

    public BizServiceInjector(Project project, ClassPool pool) {
        this.buildDir = project.buildDir
        this.pool = pool
        readRegisterClassBeanInfo("local_service_register_info.json")
    }

    private void readRegisterClassBeanInfo(String fileName) {
        if (gson == null) {
            gson = new Gson()
        }

        println(buildDir.absolutePath)

        File file = new File(buildDir, fileName)
        BufferedReader reader = null

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))
            String content
            while ((content = reader.readLine()) != null) {
                RegisterClassBean bean = gson.fromJson(content, RegisterClassBean.class)

                registerClassBeanMap.put(bean.getRegisterClassName(), bean)

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

    /*
    @Override
    void injectServiceCode(String className, String path) {
        if (registerClassBeanMap.get(className) != null) {
            injectRegisterInfoForOneClass(className, registerClassBeanMap.get(className), path)
        }
    }
    */

    void injectRegisterAndGetInfo(String path) {
        File dir = new File(path)
        if (dir.isDirectory()) {

            //遍历文件夹 //debug发现file是类似"/Users/wangallen/AndroidStudioProjects/ServiceManager/app/build/intermediates/classes/debug/wang/imallen/blog/applemodule/R.class
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                //println("----------------filePath:" + filePath + "---------------")

                String classNameTemp = filePath.replace(path, "")
                        .replace("\\", ".")
                        .replace("/", ".")
                if (classNameTemp.endsWith(".class")) {
                    String className = classNameTemp.substring(1, classNameTemp.length() - 6)
                    if (registerClassBeanMap.get(className) != null) {
                        injectRegisterInfoForOneClass(className, registerClassBeanMap.get(className), path)
                    }
                }
            }

        }
    }


    private void injectRegisterInfoForOneClass(String className,
                                               RegisterClassBean registerClassBean, String path) {
        CtClass ctClass = pool.getCtClass(className)
        if (ctClass == null) {
            throw new Exception(className + " cannot be loaded!")
        }
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }
        for (RegisterMethodBean methodBean : registerClassBean.getMethodBeans()) {

            CtMethod ctMethod = createCtMethod(ctClass, methodBean)
            //插入注册本地服务的代码
            injectLocalRegisterInfo(ctMethod, methodBean)
            //插入注册远程服务的代码
            injectRemoteRegisterInfo(ctMethod, methodBean)
            //插入获取本地服务的代码
            injectLocalGetInfo(ctMethod, methodBean)
            //插入获取远程服务的代码
            injectRemoteGetInfo(ctMethod, methodBean)

            injectLocalUnRegisterInfo(ctMethod, methodBean)

            injectRemoteUnRegisterInfo(ctMethod, methodBean)
        }

        ctClass.writeFile(path)
    }

    private void injectLocalGetInfo(CtMethod ctMethod, RegisterMethodBean methodBean) {
        for (ServiceInfo serviceInfo : methodBean.getLocalGetInfos()) {
            String getLocalServiceCode = serviceInfo.getServiceFieldName() + "=" + STAR_BRIDGE_INSTANCE + ".getLocalService(" +
                    serviceInfo.getServiceCanonicalName() + ".class);"
            ctMethod.insertAfter(getLocalServiceCode)
        }
    }

    private void injectRemoteGetInfo(CtMethod ctMethod, RegisterMethodBean methodBean) {
        for (ServiceInfo serviceInfo : methodBean.getRemoteGetInfos()) {
            String getRemoteServiceCode = serviceInfo.getServiceFieldName() + "=" + serviceInfo.getServiceCanonicalName() +
                    ".Stub.asInterface(" + STAR_BRIDGE_INSTANCE + ".getRemoteService(" + serviceInfo.getServiceCanonicalName() + ".class));"
            ctMethod.insertAfter(getRemoteServiceCode)
        }
    }

    private void injectLocalUnRegisterInfo(CtMethod ctMethod, RegisterMethodBean methodBean) {
        for (String serviceName : methodBean.getLocalUnRegisterInfos()) {
            String unregisterLocalServiceCode = STAR_BRIDGE_INSTANCE + ".unregisterLocalService(\"" + serviceName + "\");"
            ctMethod.insertAfter(unregisterLocalServiceCode)
        }
    }

    private void injectLocalRegisterInfo(CtMethod ctMethod, RegisterMethodBean methodBean) {
        for (ServiceInfo serviceInfo : methodBean.getLocalRegisterInfos()) {
            String registerLocalServiceCode = STAR_BRIDGE_INSTANCE + ".registerLocalService(" + serviceInfo.getServiceCanonicalName() +
                    ".class," + serviceInfo.getServiceFieldName() + ");"
            ctMethod.insertAfter(registerLocalServiceCode)
        }
    }

    private void injectRemoteRegisterInfo(CtMethod ctMethod, RegisterMethodBean methodBean) {
        for (ServiceInfo serviceInfo : methodBean.getRemoteRegisterInfos()) {
            //TODO 这样的话，对象必须是继承自IBinder的

            String registerRemoteServiceCode = STAR_BRIDGE_INSTANCE + ".registerRemoteService(" + serviceInfo.getServiceCanonicalName() +
                    ".class,org.qiyi.video.svg.utils.ServiceUtils.getIBinder(" + serviceInfo.getServiceFieldName() + "));"

            ctMethod.insertAfter(registerRemoteServiceCode)
        }
    }

    private void injectRemoteUnRegisterInfo(CtMethod ctMethod, RegisterMethodBean methodBean) {
        for (String serviceName : methodBean.getRemoteUnRegisterInfos()) {
            String unregisterRemoteServiceCode = STAR_BRIDGE_INSTANCE + ".unregisterRemoteService(\"" + serviceName + "\");"
            ctMethod.insertAfter(unregisterRemoteServiceCode)
        }
    }

    private CtMethod createCtMethod(CtClass ctClass, RegisterMethodBean methodBean) {
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


}