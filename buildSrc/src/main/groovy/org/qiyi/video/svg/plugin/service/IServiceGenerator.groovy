package org.qiyi.video.svg.plugin.service

import org.gradle.api.Project

public interface IServiceGenerator {

    void injectStubServiceToManifest(Project project)

    /**
     * 获取匹配，key为进程名，value为对应的CommuStubService名称
     * @return
     */
    Map<String,String>getMatchServices()
}