package org.qiyi.video.svg.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

public class StarBridgeTransform extends Transform {

    private Project project
    private ServiceInject serviceInject

    public StarBridgeTransform(Project project) {
        this.project = project
        //this.serviceInject = new ServiceInject(project)
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
        //TODO 是不是只需要将bootClasspath[0]加入就可以呢
        project.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }

        ClassAppender.appendAllClasses(transformInvocation.getInputs(), classPool)

        this.serviceInject=new ServiceInject(project,classPool)

        //serviceInject.injectService()

        //遍历input
        transformInvocation.inputs.each { TransformInput input ->

            println("----------TransformInput-------------")

            //遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //注入代码
                serviceInject.appendClassPath(directoryInput.file.absolutePath, project)

                //获取output目录
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                //将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            //serviceInject.injectService()

            //遍历jar文件，对jar不操作，但是要输出到out路径
            input.jarInputs.each { JarInput jarInput ->
                //重命名输出文件(同目录copyFile会冲突)
                def jarName = jarInput.name
                println("jar=" + jarInput.file.getAbsolutePath())

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