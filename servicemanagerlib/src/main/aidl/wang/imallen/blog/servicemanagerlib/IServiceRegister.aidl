// IServiceRegister.aidl
package wang.imallen.blog.servicemanagerlib;

// Declare any non-default types here with import statements

interface IServiceRegister {
    void registerRemoteService(String module,IBinder binder);
    void unregisterRemoteService(String module);
}
