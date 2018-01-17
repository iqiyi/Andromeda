// IServiceRegister.aidl
package org.qiyi.video.svg;

// Declare any non-default types here with import statements

interface IDispatcherRegister {
    //这个要加上oneway关键字比较好,反正我们又不需要返回值
    oneway void registerDispatcher(IBinder dispatcherBinder);
}
