# 项目简介

ServiceManager旨在提供一套易用的组件间通信方案。

方案选择对AIDL进行封装，在于AIDL具有天然的互操作性，即SDK对外输出的时候无需携带任何aar或者jar。 为了解决易用的问题，ServiceManager对AIDL进行如下优化：   
1. 同步调用，原始的bindService()是异步调用，通过ServiceManager.get("xxx")可以同步获取Binder；   
2. 生命周期自动管理，借助带有LifeCycle的Archetecture components，可以实现unbind()的自动调用，详见[Binder生命周期自动管理](http://gitlab.qiyi.domain/wanglonghai/ServiceManager/wikis/%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F%E8%87%AA%E5%8A%A8%E7%AE%A1%E7%90%86).  
3. 接口的兼容性管理, 详见[滚木移石方案](http://gitlab.qiyi.domain/wanglonghai/ServiceManager/wikis/%E6%8E%A5%E5%8F%A3%E5%85%BC%E5%AE%B9%E6%80%A7%E7%AE%A1%E7%90%86---%E6%BB%9A%E6%9C%A8%E7%A7%BB%E7%9F%B3).  

Client端调用为接口型调用，就像这样:

```
IXxxService xxx = Services.get(context, IXxxService.class);
xxx.getSomething();
```

目前ServiceManager支持Local服务和IPC服务2种接口服务，即同进程和跨进程IPC。之所以分成Local服务和IPC服务这两种，是由于Local服务的接口可以传递各种类型的参数和返回值，而IPC接口则受AIDL的限制，参数和返回值只能是基本类型或者实现了Parcelable接口的自定义类型。

# 接入方式

##编译接入

添加maven路径:

```groovy
    allprojects {
        repositories {
          ...
    
            maven{
                url 'http://maven.mbd.qiyi.domain/nexus/content/repositories/mcg-arch'
            }
        }
    }
```

是添加gradle依赖:

```groovy
    compile 'org.qiyi.video.svg:svglib:0.3.0'
```

## 初始化

使用之前进行初始化(建议在Application中进行初始化)

```java
    ServiceManager.init(Context);
```

ServiceManager多次初始化将视为单次。


## Local服务的注册与使用
### Local接口定义与实现
Local接口定义与实现这方面，和普通的接口定义、实现没什么太大区别，不一样的地方就两个:

+ 接口文件需要暴露出去，使其对项目中的所有模块编译期可见。
+ 接口的实现推荐采用单例模式，防止多次注册造成的实现被覆盖的问题。

### 服务端接口注册
本地服务的注册有三种方法，如下:

```java
ServiceManager.getInstance().registerLocalService(ICheckApple.class.getCanonicalName(),CheckAppleImpl.getInstance());
//ServiceManager.getInstance().registerLocalService(ICheckApple.class,CheckAppleImpl.getInstance());
//ServiceManager.getInstance().registerLocalService("wang.imallen.blog.moduleexportlib.apple.ICheckApple",CheckAppleImpl.getInstance());
```

[注:使用第三种方式需要考虑混淆.]

### 客户端接口调用


```java
 ICheckApple checkApple = (ICheckApple) ServiceManager.getInstance().getLocalService(ICheckApple.class);
//ICheckApple checkApple = (ICheckApple) ServiceManager.getInstance().getLocalService(ICheckApple.class.getCanonicalName());
// ICheckApple checkApple = (ICheckApple) ServiceManager.getInstance().getLocalService("wang.imallen.blog.moduleexportlib.apple.ICheckApple");
```

### Local服务接口的Callback问题
如果是耗时操作，由Local服务接口自己定义Callback接口，调用方在调用接口时传入Callback对象即可。

## IPC服务的注册与使用
IPC服务的注册需要像实现AIDL Service那样定义aidl接口。
### IPC接口的定义与实现
定义aidl接口，并且要将编译生成的Stub和Proxy类在编译期暴露给调用方代码。

如下定义了一个购买苹果的服务接口:

```aidl
    package wang.imallen.blog.moduleexportlib.apple;
    import org.qiyi.video.svg.IPCCallback;
    
    interface IBuyApple {
        int buyAppleInShop(int userId);
        void buyAppleOnNet(int userId,IPCCallback callback);
    }
```

接口实现如下:

```
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

### 远程接口的注册
与本地接口的注册略有不同，远程接口注册的是继承了Stub类的IBinder部分，注册方式有传递接口Class和接口全路径名两种，如下:

```java
ServiceManager.getInstance().registerRemoteService(IBuyApple.class, BuyAppleImpl.getInstance().asBinder());
//ServiceManager.getInstance().registerRemoteService(IBuyApple.class, BuyAppleImpl.getInstance());
//ServiceManager.getInstance().registerRemoteService(IBuyApple.class.getCanonicalName(),BuyAppleImpl.getInstance().asBinder());
```

### 远程接口的使用
由于跨进程只能传递IBinder,所以只能获取到远程服务的IBinder之后，再调用XX.Stub.asInterface()获取到它的代理,如下:

```java
        IBinder buyAppleBinder = ServiceManager.getInstance().getRemoteService(IBuyApple.class);
		//IBinder buyAppleBinder = ServiceManager.getInstance().getRemoteService(IBuyApple.class.getCanonicalName);
         if (null == buyAppleBinder) {
             return;
         }
         IBuyApple buyApple = IBuyApple.Stub.asInterface(buyAppleBinder);
         if (null != buyApple) {
            try {
               int appleNum = buyApple.buyAppleInShop(10);
                Toast.makeText(BananaActivity.this, "got remote service in other process(:banana),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();
        
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
         }
```
[注：远程服务也可以在同进程中被调用，此时会自动降级为进程内普通的接口调用，这个是binder原生支持的]

### IPC接口的Callback问题
对于有耗时操作的IPC服务，定义接口时需要借助lib中的IPCCallback,如下:

```aidl
    package wang.imallen.blog.moduleexportlib.apple;
    import org.qiyi.video.svg.IPCCallback;
    
    interface IBuyApple {
        int buyAppleInShop(int userId);
        void buyAppleOnNet(int userId,IPCCallback callback);
    }
```

其中的buyAppleOnNet()方法就是耗时操作，需要在定义时加上IPCCallback。

IPCCallback本身也是一个aidl接口，如下:

```aidl
    interface IPCCallback {
       void onSuccess(in Bundle result);
       void onFail(String reason);
    }
```

对于IPCCallback的调用，有2种方式：

1.调用者直接继承IPCCallback.Stub类，实现相关接口，以下代码将在binder线程中回调，如:

```java
        IBinder buyAppleBinder = ServiceManager.getInstance().getRemoteService(IBuyApple.class);
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

2.调用者希望回调在主线程, 可以调用lib中封装的一个BaseCallback给调用方使用(推荐使用)，

```
...
buyApple.buyAppleOnNet(10, new BaseCallback() {
...
```

BaseCallback的实现如下：

```java
public abstract class BaseCallback extends IPCCallback.Stub {

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public final void onSuccess(final Bundle result) throws RemoteException {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onSucceed(result);
            }
        });
    }

    @Override
    public final void onFail(final String reason) throws RemoteException {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onFailed(reason);
            }
        });
    }

    public abstract void onSucceed(Bundle result);

    public abstract void onFailed(String reason);
}
```

## 事件订阅与发布
### 事件定义:Event

Event的定义如下:

```java
public class Event implements Parcelable {

    private String name;

    private Bundle data;
    
    ...
}
```

即 事件=名称+数据  
其中**名称要求在整个项目中唯一**。
由于要跨进程传输，所以所有数据只能放在Bundle中进行包装。

### 事件订阅
事件订阅很简单，首先需要有一个实现了EventListener接口的对象。
然后就可以订阅自己感兴趣的事件了，如下:

```java
    ServiceRouter.getInstance().subscribe(EventConstants.APPLE_EVENT,MainActivity.this);
```

其中MainActivity实现了EventListener接口，此处表示订阅了名称为EventConstnts.APPLE_EVENT的事件。

### 事件发布
事件发布很简单，调用publish方法即可，如下:

```java
       Bundle bundle = new Bundle();
       bundle.putString("Result", "gave u five apples!");
       ServiceRouter.getInstance().publish(new Event(EventConstants.APPLE_EVENT, bundle));
```

# TODO
1.做成透明依赖，最好是让使用者不需要写额外的代码  
2.事件总线需要支持粘性事件  
3.事件总线需要支持三种threadMode回调

# 技术支持

1. Sample代码
2. 阅读Wiki或者FAQ
3. 联系@王龙海