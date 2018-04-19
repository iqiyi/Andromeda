package org.qiyi.video.svg.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.qiyi.video.svg.plugin.service.IServiceGenerator
import org.qiyi.video.svg.plugin.service.StubServiceGenerator
import org.qiyi.video.svg.plugin.extension.DispatcherExtension

public class AndromedaPlugin implements Plugin<Project> {

    private IServiceGenerator stubServiceGenerator = new StubServiceGenerator()

    @Override
    void apply(Project project) {

        project.extensions.create("dispatcher",DispatcherExtension)

        def android = project.extensions.getByType(AppExtension)

        stubServiceGenerator.injectStubServiceToManifest(project)

        //注册一个Transform
        def classTransform = new AndromedaTransform(project,stubServiceGenerator)

        android.registerTransform(classTransform)

        System.out.println("-------------end of buildSrc AndromedaTransform-----------------")
    }

}

