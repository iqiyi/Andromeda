// IServiceRegister.aidl
package org.qiyi.video.svg;

// Declare any non-default types here with import statements

interface IServiceRegister {
    void registerRemoteService(String serivceName,IBinder binder);
    void unregisterRemoteService(String serviceName);
}
