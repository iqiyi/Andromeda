package org.qiyi.video.svg.plugin.manifest

public interface IComponentReader {

    void readActivities(Set<String> processNames)

    void readServices(Set<String> processNames)

    void readBroadcastReceivers(Set<String> processNames)

    void readProviders(Set<String> processNames)

}