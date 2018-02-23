package org.qiyi.video.svg.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class StarBridgePlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        def android=project.extensions.getByType(AppExtension)

        //注册一个Transform
        def classTransform=new StarBridgeTransform(project)
        android.registerTransform(classTransform)

        System.out.println("-------------end of StarBridgeTransform-----------------")
    }
}

