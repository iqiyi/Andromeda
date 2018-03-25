package org.qiyi.video.svg.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.qiyi.video.svg.plugin.service.IServiceGenerator
import org.qiyi.video.svg.plugin.service.StubServiceGenerator

public class StarBridgePlugin implements Plugin<Project> {

    private IServiceGenerator stubServiceGenerator = new StubServiceGenerator()

    @Override
    void apply(Project project) {

        def android = project.extensions.getByType(AppExtension)

        //TODO 这里这样getName()是否会有问题?
        stubServiceGenerator.injectStubServiceToManifest(project)

        //注册一个Transform
        def classTransform = new StarBridgeTransform(project,stubServiceGenerator.getMatchServices())

        android.registerTransform(classTransform)

        System.out.println("-------------end of StarBridgeTransform-----------------")
    }


}

