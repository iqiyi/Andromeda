package org.qiyi.video.svg.plugin

import com.android.build.api.transform.TransformInput
import javassist.ClassPool

class ClassAppender {

    //TODO ?这样会不会容易导致编译时OOM?
    //TODO 另外，由于插入的只是ServiceRouter相关的代码，所以是不是只需要将它添加到classPool中即可?
    static void appendAllClasses(Collection<TransformInput> inputs, ClassPool classPool) {
        inputs.each {
            it.directoryInputs.each {
                def dirPath = it.file.absolutePath
                classPool.insertClassPath(dirPath)
            }

            it.jarInputs.each {
                classPool.insertClassPath(it.file.absolutePath)
            }
        }
    }

}