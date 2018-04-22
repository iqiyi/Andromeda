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
package org.qiyi.video.svg.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.qiyi.video.svg.plugin.service.ServiceMatchInjector
import org.qiyi.video.svg.plugin.injector.StubServiceMatchInjector
import org.qiyi.video.svg.plugin.service.IServiceGenerator
import org.qiyi.video.svg.plugin.utils.CtUtils

public class AndromedaTransform extends Transform {

    private Project project

    private StubServiceMatchInjector stubServiceMatchInjector

    private ServiceMatchInjector serviceMatchInjector

    private IServiceGenerator serviceGenerator

    public AndromedaTransform(Project project, IServiceGenerator serviceGenerator) {
        this.project = project
        this.serviceGenerator = serviceGenerator
        //this.serviceMatchInjector=new ServiceMatchInjector(serviceGenerator)
    }

    @Override
    String getName() {
        return "AndromedaTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        println "AndromedaTransform-->transform"

        //newTransform(transformInvocation)


        //step1:将所有类的路径加入到ClassPool中
        ClassPool classPool = new ClassPool()
        project.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }

        //TODO 这里有优化的空间,实际上只要将我们需要的类加进去即可
        ClassAppender.appendAllClasses(transformInvocation.getInputs(), classPool)

        this.stubServiceMatchInjector = new StubServiceMatchInjector(classPool, serviceGenerator, project.rootDir.absolutePath)
        //遍历input
        transformInvocation.inputs.each { TransformInput input ->

            println("----------TransformInput-------------")

            //遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->

                //获取output目录
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                //将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            //遍历jar文件，对jar不操作，但是要输出到out路径
            input.jarInputs.each { JarInput jarInput ->
                //重命名输出文件(同目录copyFile会冲突)
                def jarName = jarInput.name
                println("jar=" + jarInput.file.getAbsolutePath())

                stubServiceMatchInjector.injectMatchCode(jarInput)

                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }



    }

    private void newTransform(TransformInvocation transformInvocation){
        TransformOutputProvider outputProvider=transformInvocation.outputProvider
        outputProvider.deleteAll()
        File jarFile=outputProvider.getContentLocation("main",getOutputTypes(),getScopes(),Format.JAR)
        if(!jarFile.getParentFile().exists()){
            jarFile.getParentFile().mkdirs()
        }
        if(jarFile.exists()){
            jarFile.delete()
        }

        ClassPool classPool=new ClassPool()
        project.android.bootClasspath.each{
            classPool.appendClassPath((String)it.absolutePath)
        }

        List<CtClass> allClasses=CtUtils.getCtClasses(transformInvocation.inputs,classPool)
        serviceMatchInjector.injectMatchCode(allClasses,jarFile)
    }


}