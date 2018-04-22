package org.qiyi.video.svg.plugin.service

import javassist.CtClass
import javassist.CtMethod

import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ServiceMatchInjector {

    private static final String PKG_NAME = "org.qiyi.video.svg.utils"
    private static final String STUB_SERVICE_MATCHER = "StubServiceMatcher"

    private static final String GET_TARGET_SERVICE = "getTargetService"

    private IServiceGenerator serviceGenerator
    private Map<String, String> matchedServices

    ServiceMatchInjector(IServiceGenerator serviceGenerator) {
        this.serviceGenerator = serviceGenerator
    }

    void injectMatchCode(List<CtClass> allClasses, File jarFile) {

        println "ServiceMatchInjector-->injectMatchCode(),jarFile:" + jarFile.absolutePath.toString()

        ZipOutputStream zipOutputStream = new JarOutputStream(new FileOutputStream(jarFile))

        for (CtClass ctClass : allClasses) {
            if (PKG_NAME.equals(ctClass.getPackageName()) && STUB_SERVICE_MATCHER.equals(ctClass.getSimpleName())) {
                doInjectMatchCode(ctClass)
            }

            zipFile(ctClass.toBytecode(), zipOutputStream, ctClass.getName().replace("\\.", "/") + ".class");
        }
        zipOutputStream.close()
    }

    //TODO 这里做得不对
    private void zipFile(byte[] classBytesArray, ZipOutputStream zipOutputStream, String entryName) {
        try {
            ZipEntry entry = new ZipEntry(entryName)
            zipOutputStream.putNextEntry(entry)
            zipOutputStream.write(classBytesArray, 0, classBytesArray.length)
            zipOutputStream.closeEntry()
            zipOutputStream.flush()
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    private void doInjectMatchCode(CtClass ctClass) {

        println "doInjectMatchCode"

        //首先获取服务信息
        fetchServiceInfo()

        //CtClass ctClass = classPool.getCtClass(STUB_SERVICE_MATCHER)
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }
        CtMethod[] ctMethods = ctClass.getDeclaredMethods()
        CtMethod getTargetServiceMethod = null
        ctMethods.each {
            if (GET_TARGET_SERVICE.equals(it.getName())) {
                getTargetServiceMethod = it
            }
        }
        StringBuilder code = new StringBuilder()
        //注意:javassist的编译器不支持泛型
        code.append("{\njava.util.Map matchedServices=new java.util.HashMap();\n")
        matchedServices.each {
            code.append("matchedServices.put(\"" + it.getKey() + "\"," + it.getValue() + ".class);\n")
        }
        code.append('if(matchedServices.get($1)!=null)return matchedServices.get($1);\n}')
        getTargetServiceMethod.insertBefore(code.toString())

    }

    private void fetchServiceInfo() {
        matchedServices = serviceGenerator.getMatchServices()
        if (matchedServices == null) {
            this.matchedServices = new HashMap<>()
            readMatchedServices(rootDirPath + File.separator + StubServiceGenerator.MATCH_DIR, StubServiceGenerator.MATCH_FILE_NAME)
        }
    }

    private void readMatchedServices(String dirPath, String fileName) {
        println "ServiceMatchInjector-->readMatchedServices()"

        File dir = new File(dirPath)
        if (!dir.exists()) {
            return
        }
        File matchFile = new File(dir, fileName)
        if (!matchFile.exists()) {
            return
        }
        BufferedInputStream ism = matchFile.newInputStream()
        BufferedReader reader = new BufferedReader(new InputStreamReader(ism))
        String content
        while ((content = reader.readLine()) != null) {
            String[] matchKeyValues = content.split(",")
            if (matchKeyValues != null) {
                println "read key:" + matchKeyValues[0] + ",value:" + matchKeyValues[1]
                matchedServices.put(matchKeyValues[0], matchKeyValues[1])
            }
        }
        reader.close()
        ism.close()
    }

}