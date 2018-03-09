package org.qiyi.video.svg.plugin.bean;

import java.io.Serializable;

/**
 * Created by wangallen on 2018/3/6.
 */

public class ServiceInfo implements Serializable {

    private String serviceCanonicalName;
    private String serviceFieldName;

    public ServiceInfo(String serviceCanonicalName, String serviceFieldName) {
        this.serviceCanonicalName = serviceCanonicalName;
        this.serviceFieldName = serviceFieldName;
    }

    public String getServiceCanonicalName() {
        return serviceCanonicalName;
    }

    public void setServiceCanonicalName(String serviceCanonicalName) {
        this.serviceCanonicalName = serviceCanonicalName;
    }

    public String getServiceFieldName() {
        return serviceFieldName;
    }

    public void setServiceFieldName(String serviceFieldName) {
        this.serviceFieldName = serviceFieldName;
    }
}