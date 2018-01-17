// IRemoteService.aidl
package org.qiyi.video.svg;

// Declare any non-default types here with import statements

interface IServiceDispatcher {

   IBinder getTargetBinder(String serviceName);
   //这个uri其实就是Target Service的action
   IBinder fetchTargetBinder(String uri);

   //TODO 这个是不是也可以用oneway来修饰呢?
   void registerRemoteService(String serviceName,IBinder binder);

   void unregisterRemoteService(String module);

}
