# Andromeda
![Andromeda_license](https://img.shields.io/badge/license-BSD--3--Clause-brightgreen.svg)
![Andromeda_core_tag](https://img.shields.io/badge/Andromeda%20core-1.1.0-brightgreen.svg)
![Andromeda_plugin_tag](https://img.shields.io/badge/Andromeda%20plugin-1.1.0-brightgreen.svg)

Andromeda provides communication among modules for both local and remote service.

**Anno:The reason that differentiate local service from remote service is that parameter types in remote service can only be primitive type or custom type that implements Parcelable, while parameter types in local service can be any type such as View and Context.**

[文档，还是中文好](https://github.com/iqiyi/Andromeda/blob/master/CHINESE_README.md)

# Features

+ Aidl interface and implemention are the only thing developers need do. bindService() and Service definition are not necessary.

+ Remote service could be fetched synchronously instead of asynchronously

+ Process-priority-hencing work is managed along with Fragment/Activity's lifecycle

+ IPC Callback is supported
 
+ Event bus for all processes is supported

**Anno: service here means interface and it's implementation instead of component Service.**

Comparsion between other communication solutions and Andromeda:

|       |    convenience     | code invasion  |   interoperability    |  IPC   |  event bus  |  page router  |
| :---: | :-------: | :----------: |:----------: |:----------: |:----------: |:----------: |
| Andromeda |  good     |   none     |    good    |    Yes    |   Yes    |   No     |
| DDComponentForAndroid |  bad      |   some     |    bad    |   No     |   No    |   Yes     |
| ARouter |  good      |   some     |    bad    |   No     |   No    |    Yes    |


# Download
add classpath in buildscript(replace $version with latest version name):
```groovy
    classpath "org.qiyi.video.svg:plugin:$version"
```
add core lib dependency in Application or library Module:
```groovy
    implementation "org.qiyi.video.svg:core:$version"
```
apply gradle plugin in application Module:
```groovy
    apply plugin: 'org.qiyi.svg.plugin'
```

# How to use
## Dispatcher config
**Dispatcher should always in the process that live longest cause it manager all process infos!.**
Default process of Dispatcher is main process if not configed. 
Considering some process may live longer than main process in some apps(such as music app), developers should
 config process name for Dispatcher in this case. Just as follows in build.gradle of application module:
```groovy
    dispatcher{
        process ":downloader"
    }
``` 
In this case, ":downloader" process is the one that live longest.

## init
add init code in Application.onCreate():
```java
    Andromeda.init(Context);
```

## Register and use local service
### Definition and implementation of local service
There are only two differences between local service and normal interfaces:
+ interfaces should be put in a common module to make it accessible to all modules
+ Andromeda will only hold one implementation at a time

### Register local service
There are two methods to register local service, the first one is as follows:
```java
    Andromeda.registerLocalService(ICheckApple.class.getCanonicalName(),new CheckApple());
```
Another is as follows:
```java
    Andromeda.registerLocalService(ICheckApple.class,new CheckApple());
```
ICheckApple is interface definition. Considering proguard, registering local service with fixed String is not recommanded, just as follows:
```java
    Andromeda.registerLocalService("wang.imallen.blog.moduleexportlib.apple.ICheckApple",CheckAppleImpl.getInstance());
```

### How to use local service
Any module that's in the same process with server module could obtain local service after registration. The first method is as follows:
```java
    ICheckApple checkApple = (ICheckApple) Andromeda.getLocalService(ICheckApple.class);
```
Another is as follows:
```java
    ICheckApple checkApple = (ICheckApple) Andromeda.getLocalService(ICheckApple.class.getCanonicalName());
```
Similarly, considering proguard, obtaining local service with fixed String is not recommanded neighter:
```java
    ICheckApple checkApple = (ICheckApple) Andromeda.getLocalService("wang.imallen.blog.moduleexportlib.apple.ICheckApple");
```
LocalServiceDemo shows the details of registration and use of local service。

### Callback of local service
Callback of local service is just as normal interface, which is all up to developers.
As a result, Andromeda will not provide any callbacks.

## Registeration and use of remote service
### Definition and use of remote service
First, define a aidl interface, and expose it to common module along with its Stub and Proxy.
```aidl
    package wang.imallen.blog.moduleexportlib.apple;
    import org.qiyi.video.svg.IPCCallback;
    
    interface IBuyApple {
        int buyAppleInShop(int userId);
        void buyAppleOnNet(int userId,IPCCallback callback);
    }
```
Then provide implementation:
```java
public class BuyAppleImpl extends IBuyApple.Stub {

    private static BuyAppleImpl instance;

    public static BuyAppleImpl getInstance() {
        if (null == instance) {
            synchronized (BuyAppleImpl.class) {
                if (null == instance) {
                    instance = new BuyAppleImpl();
                }
            }
        }
        return instance;
    }

    private BuyAppleImpl() {
    }

    @Override
    public int buyAppleInShop(int userId) throws RemoteException {
       ...
    }

    @Override
    public void buyAppleOnNet(int userId, IPCCallback callback) throws RemoteException {
       ...
    }
}

```
### Registration of remote service
Differen from registration of local service, IBinder of remote service is need for registration :
```java
    Andromeda.registerRemoteService(IBuyApple.class, BuyAppleImpl.getInstance().asBinder());
```
The way as follows is also workable cause BuyAppleImpl extends IBuyApple.Stub, which extends android.os.Binder:
```java
    Andromeda.registerRemoteService(IBuyApple.class, BuyAppleImpl.getInstance());
```
Another way to register is as follows:
```java
    Andromeda.registerRemoteService(IBuyApple.class.getCanonicalName(),BuyAppleImpl.getInstance().asBinder());
```

### Use of remote service
+ with() is need before obtain remote service cause Andromeda need to hence server process priority in accordance with Fragment/Activity's lifecycle;
+ getRemoteService() will return IBinder. Then you can obtain proxy by XXStub.asInterface(binder);

Set use in a FragmentActivity as example:
```java
        IBinder binder = Andromeda.with(this).getRemoteService(IBuyApple.class);
        if (binder == null) {
            return;
        }
        IBuyApple buyApple = IBuyApple.Stub.asInterface(binder);
        if (buyApple == null) {
            return;
        }
        try {
            buyApple.buyAppleInShop(29);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
```
Use of remote service in android.app.Fragment,android.support.v4.app.Fragment and normal Activity is similar, demos are CustomFragment,CustomSupportFragment and FragActivity,etc.

Attention:**Remote service could be used both in same process and other processes. When use in the same process, it will turned to local interface invoking.**

### Callback of remote service
Considering time-consuming work may be done in server process, Callback of remote service is necessary.。
For ones that need callback should add IPCCallback parameter in their aidl definitions:
```aidl
    interface IBuyApple {
        int buyAppleInShop(int userId);
        void buyAppleOnNet(int userId,IPCCallback callback);
    }
```
The canonical name of IPCCallback is org.qiyi.video.svg.IPCCallback. Its definition is as follows:
```aidl
    interface IPCCallback {
       void onSuccess(in Bundle result);
       void onFail(String reason);
    }
```
Client can use IPCCallback as follows:
```java
        IBinder buyAppleBinder = Andromeda.getRemoteService(IBuyApple.class);
        if (null == buyAppleBinder) {
            return;
        }
        IBuyApple buyApple = IBuyApple.Stub.asInterface(buyAppleBinder);
        if (null != buyApple) {
            try {
                buyApple.buyAppleOnNet(10, new IPCCallback.Stub() {
                    @Override
                    public void onSuccess(Bundle result) throws RemoteException {
                       ...
                    }

                    @Override
                    public void onFail(String reason) throws RemoteException {
                       ...
                    }
                });

            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    
```
Considering the callback is in binder thread, while most developers want the callback in UI thread, Andromeda provide a BaseCallback for deverlopers.
```java
   IBinder buyAppleBinder = Andromeda.getRemoteService(IBuyApple.class);
        if (null == buyAppleBinder) {
            return;
        }
        IBuyApple buyApple = IBuyApple.Stub.asInterface(buyAppleBinder);
        if (null != buyApple) {
            try {
                buyApple.buyAppleOnNet(10, new BaseCallback() {
                    @Override
                    public void onSucceed(Bundle result) {
                       ...
                    }

                    @Override
                    public void onFailed(String reason) {
                        ...
                    }
                });

            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
```
**Use BaseCallback instead of IPCCallback is recommanded!**

BananaActivity shows details of how to use it.

### Lifecycle control
To enhence server process's priority, Andromeda will do bindService() when Andromeda.with().getRemoteService() in accordance with Fragment/Activity's lifecycle.
As a result, unbind action is need when Fragment/Activity destroyed.
There are 2 cases now:
+ For those who obtain remote service with Fragment/Activity and in main thread, Andromeda will do unbind() action automatically

+ For those who obtain not with Fragment/Activity or in work thread, unbind() action should be invoked by developers:

```java
    public static void unbind(Class<?> serviceClass);
    public static void unbind(Set<Class<?>> serviceClasses);
```
 
## Subscribe and pushlish event
### Event
Definition of Event in Andromeda is as follows:
```java
    public class Event implements Parcelable {
    
        private String name;
    
        private Bundle data;
        
        ...
    }
```
Obviously, Event consist of name and data, which is Bundle type that can load primitive type parameters or Parcelable type parameters.

### Subscribe event
Subscribing event is very simple with one who implements EventListenr such as MainActivity:
```java
    Andromeda.subscribe(EventConstants.APPLE_EVENT,MainActivity.this);
```
This means it subscribes Event whose name is EventConstans.APPLE_EVENT.

### Publish event
Publishing event is as simple as follows:
```java
    Bundle bundle = new Bundle();
    bundle.putString("Result", "gave u five apples!");
    Andromeda.publish(new Event(EventConstants.APPLE_EVENT, bundle));
```
After published, all listeners in any processes could receive the event.

MainActivity shows details of how to subscribe and publish event.

# License
BSD-3-Clause. See the [BSD-3-Clause](https://opensource.org/licenses/BSD-3-Clause) file for details.

# Support
1. Sample codes
2. Wiki and FAQs
3. contact bettarwang@gmail.com