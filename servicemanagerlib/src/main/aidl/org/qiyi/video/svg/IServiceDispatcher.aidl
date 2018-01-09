// IRemoteService.aidl
package org.qiyi.video.svg;

// Declare any non-default types here with import statements

interface IServiceDispatcher {

   IBinder getTargetBinder(String module);
   //这个uri其实就是Target Service的action
   IBinder fetchTargetBinder(String uri);

   void registerRemoteService(String module,IBinder binder);

   void unregisterRemoteService(String module);

}
