package org.qiyi.video.svg.plugin

import com.android.build.api.transform.TransformInput
import javassist.ClassPool

class ClassAppender {

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