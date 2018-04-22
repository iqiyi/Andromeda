/*
* Copyright (c) 2018-present, iQIYI, Inc. All rights reserved.
*
* Redistribution and use in source and binary forms, with or without modification,
* are permitted provided that the following conditions are met:
*
*        1. Redistributions of source code must retain the above copyright notice,
*        this list of conditions and the following disclaimer.
*
*        2. Redistributions in binary form must reproduce the above copyright notice,
*        this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
*
*        3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived
*        from this software without specific prior written permission.
*
*        THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
*        INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
*        IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
*        OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
*        OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
*        OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
*        EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
package org.qiyi.video.svg.plugin.injector

import com.android.build.api.transform.JarInput
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.io.FileUtils
import org.qiyi.video.svg.plugin.service.IServiceGenerator
import org.qiyi.video.svg.plugin.service.StubServiceGenerator
import org.qiyi.video.svg.plugin.utils.JarUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile

public class StubServiceMatchInjector {

    private static final String STUB_SERVICE_MATCHER = "org.qiyi.video.svg.utils.StubServiceMatcher"

    private static final String STUB_SERVICE_MATCHER_CLASS = "StubServiceMatcher.class"

    private static final String GET_TARGET_SERVICE = "getTargetService"

    private ClassPool classPool
    private String rootDirPath

    private IServiceGenerator serviceGenerator

    private Map<String,String>matchedServices
    private boolean found = false

    public StubServiceMatchInjector(ClassPool classPool, IServiceGenerator serviceGenerator, String rootDirPath) {
        this.classPool = classPool
        this.serviceGenerator=serviceGenerator
        this.rootDirPath=rootDirPath
    }

    private void readMatchedServices(String dirPath, String fileName) {
        println "readMatchedServices()"
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
                println "read key:"+matchKeyValues[0]+",value:"+matchKeyValues[1]
                matchedServices.put(matchKeyValues[0], matchKeyValues[1])
            }
        }
        reader.close()
        ism.close()
    }

    public void injectMatchCode(JarInput jarInput) {
        if (found) {
            return
        }

        String filePath = jarInput.file.getAbsolutePath()

        if (filePath.endsWith(".jar") && !filePath.contains("com.android.support")
                && !filePath.contains("/com/android/support")) {

            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()

                //println "jarEntryName:"+entryName

                if (entryName.endsWith(STUB_SERVICE_MATCHER_CLASS)) {
                    prepareInjectMatchCode(filePath)
                    found = true
                    break
                }
            }

        }
    }

    private void prepareInjectMatchCode(String filePath) {

        //filePath是类似../ServiceManager/StarBridge-Lib/build/intermediates/intermediate-jars/debug/classes.jar这样的路径
        //TODO 但是遇到/Users/wangallen/.gradle/caches/transforms-1/files-1.1/core-0.9.3.aar/c57f58254c75aebebc28c22661fd8042/jars/classes.jar这样的，就会出现如下异常:
        //TODO java.io.FileNotFoundException: /Users/wangallen/.gradle/caches/transforms-1/files-1.1/core-0.9.3.aar/c57f58254c75aebebc28c22661fd8042/jars/classes/org/qiyi (Not a directory)
        println "StubServiceMatchInjector-->prepareInjectMatchCode,filePath:" + filePath

        File jarFile = new File(filePath)
        String jarDir = jarFile.getParent() + File.separator + jarFile.getName().replace('.jar', '')

        println "jarDir:"+jarDir

        //解压jar包，解压之后就是.class文件
        List<String> classNameList = JarUtils.unzipJar(filePath, jarDir)

        //删除原来的jar包
        jarFile.delete()

        //注入代码
        //classPool.insertClassPath(jarDir)
        classPool.appendClassPath(jarDir)

        for (String className : classNameList) {
            if (className.endsWith(STUB_SERVICE_MATCHER_CLASS)) {
                doInjectMatchCode(jarDir)
                break
            }
        }

        //重新打包jar
        JarUtils.zipJar(jarDir, filePath)

        //删除目录
        FileUtils.deleteDirectory(new File(jarDir))

    }

    private void fetchServiceInfo(){
        matchedServices=serviceGenerator.getMatchServices()
        if (matchedServices == null) {
            this.matchedServices=new HashMap<>()
            readMatchedServices(rootDirPath + File.separator + StubServiceGenerator.MATCH_DIR, StubServiceGenerator.MATCH_FILE_NAME)
        }
    }

    //这个className含有.class,而实际上要获取CtClass的话只需要前面那部分，即"org.qiyi.video.svg.utils.StubServiceMatcher"而不是"org.qiyi.video.svg.utils.StubServiceMatcher.class"
    private void doInjectMatchCode(String path) {

        println "doInjectMatchCode"

        //首先获取服务信息
        fetchServiceInfo()

        CtClass ctClass = classPool.getCtClass(STUB_SERVICE_MATCHER)
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

        ctClass.writeFile(path)
    }
}