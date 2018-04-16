package org.qiyi.video.svg.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.qiyi.video.svg.plugin.injector.BizServiceInjector
import org.qiyi.video.svg.plugin.injector.StubServiceMatchInjector
import org.qiyi.video.svg.plugin.service.IServiceGenerator

public class StarBridgeTransform extends Transform {

    private Project project

    private BizServiceInjector bizServiceInjector

    private StubServiceMatchInjector stubServiceMatchInjector

    private IServiceGenerator serviceGenerator

    public StarBridgeTransform(Project project, IServiceGenerator serviceGenerator) {
        this.project = project
        this.serviceGenerator=serviceGenerator
    }

    @Override
    String getName() {
        return "StarBridgeTransform"
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

        //step1:将所有类的路径加入到ClassPool中
        ClassPool classPool = new ClassPool()
        project.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }

        //TODO 这里有优化的空间,实际上只要将我们需要的类加进去即可
        ClassAppender.appendAllClasses(transformInvocation.getInputs(), classPool)

        this.bizServiceInjector = new BizServiceInjector(project, classPool)

        this.stubServiceMatchInjector = new StubServiceMatchInjector(classPool, serviceGenerator,project.rootDir.absolutePath)
        //遍历input
        transformInvocation.inputs.each { TransformInput input ->

            println("----------TransformInput-------------")

            //遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->

                //TODO 其实这样做不够，还需要对JarInput进行处理才完全，因为实际上jar包中也完全可能含有注解的!
                //bizServiceInjector.injectRegisterAndGetInfo(directoryInput.file.absolutePath)

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

}