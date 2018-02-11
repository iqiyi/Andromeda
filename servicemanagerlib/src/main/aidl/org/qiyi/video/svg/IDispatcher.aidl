// IRemoteService.aidl
package org.qiyi.video.svg;
import org.qiyi.video.svg.event.Event;
// Declare any non-default types here with import statements
import org.qiyi.video.svg.bean.BinderBean;

interface IDispatcher {

   BinderBean getTargetBinder(String serviceCanonicalName);
   //IBinder getTargetBinder(String serviceCanonicalName);
   //这个uri其实就是Target Service的action
   IBinder fetchTargetBinder(String uri);

   //TODO 这个是不是也可以用oneway来修饰呢?
   //void registerRemoteService(String serviceCanonicalName,IBinder binder);
   void registerRemoteService(String serviceCanonicalName,String processName,IBinder binder);

   void unregisterRemoteService(String serviceCanonicalName);

   void publish(in Event event);

}
