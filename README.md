# ServiceManager

ServiceManager设计的目的是让组件间通信如同调用Java Interface一样简单。
目前ServiceManager支持本地服务的注册与使用，远程服务的注册与使用。
**之所以分成本地服务和远程服务这两种，是由于本地服务的接口可以传递各种类型的参数和返回值，而远程接口则受AIDL的限制，参数和返回值只能是基本类型或者实现了Parcelable接口的自定义类型。**
即服务方只要注册了接口和实现，调用方就可通过ServiceManager调用到。

**注意这里的服务不是Android中四大组件的Service,而是指提供的接口与实现。为了表示区分，后面的服务均是这个含义，而Service则是指Android中的组件。**

# 接入方式
首先添加maven路径:
```groovy
allprojects {
    repositories {
        google()
        jcenter()

        maven{
            url 'http://maven.mbd.qiyi.domain/nexus/content/repositories/mcg-arch'
        }
    }
}
```
其实是添加gradle依赖:
```groovy
compile 'org.qiyi.video.svg:svglib:0.3.0'
```
**注:这里具体是采用implementation,api,compileOnly中的哪一种，需要结合具体的使用场景，比如如果是在一个独立的App中使用，则使用api; 
反之，如果是在一个组件中使用(比如爱奇艺的Download组件),那么使用compileOnly即可。**

目前对外的接口是ServiceManager，业务方接入只需要跟这个类打交道。

##初始化
最好是在自己进程的Application中进行初始化(每个进程都有自己的ServiceManager对象)，代码如下:
```java
ServiceManager.init(Context);
```
如果不能在Application中进行初始化(比如没有自己的Application),那么至少要保证在使用之前进行初始化。
**另外，不用担心ServiceManager多次初始化的问题，在任一进程中，只有首次初始化有效，后面的初始化会自动忽略**。

##本地服务的注册与使用
###本地接口定义与实现
本地接口定义与实现这方面，和普通的接口定义、实现没什么太大区别，不一样的地方就两个:
+ 接口需要就是要暴露出去，使其对项目中的所有模块都可见。
**至于具体的打包方式，目前还在实现中，在Demo阶段先特殊处理。**
+ 接口的实现最好采用单例模式，否则后面的实现类会将全面的替换，从而可能出现

###本地接口注册
本地服务的注册有两种方法，一种是直接调用接口的全路径名和接口的实现，如下:
```java
ServiceManager.getInstance().registerLocalService(ICheckApple.class.getCanonicalName(),CheckAppleImpl.getInstance());
```
还有一种是调用接口class和接口的实现，其实在内部也是获取了它的全路径名,如下:
```java
ServiceManager.getInstance().registerLocalService(ICheckApple.class,CheckAppleImpl.getInstance());
```
其中ICheckApple.class为接口，虽然也可以采用下面这种方式注册:
```java
ServiceManager.getInstance().registerLocalService("wang.imallen.blog.moduleexportlib.apple.ICheckApple",CheckAppleImpl.getInstance());
```
**但是考虑到混淆问题，非常不推荐使用这种方式进行注册**，除非双方能够协商一致使用这个key(因为实际上ServiceManager只需要保证有一个唯一的key与该服务对应即可).

###本地接口使用
注册完之后，与服务提供方同进程的任何模块都可以调用该服务,获取服务的方式与注册对应，也有两种方式，一种是通过接口的class获取,如下:
```java
ICheckApple checkApple = (ICheckApple) ServiceManager.getInstance().getLocalService(ICheckApple.class);
if (checkApple != null) {
     int calories = checkApple.getAppleCalories(3);
     String desc = checkApple.getAppleDescription(2);
     Toast.makeText(LocalServiceDemo.this,
      "got ICheckApple service,calories:" + calories + ",description:" + desc, Toast.LENGTH_SHORT).show();
     }
```
还有一种方法是通过接口的全路径名获取，如下:
```java
    ICheckApple checkApple = (ICheckApple) ServiceManager.getInstance().getLocalService(ICheckApple.class.getCanonicalName());
    if (checkApple != null) {
         int calories = checkApple.getAppleCalories(3);
         String desc = checkApple.getAppleDescription(2);
         Toast.makeText(LocalServiceDemo.this,
          "got ICheckApple service,calories:" + calories + ",description:" + desc, Toast.LENGTH_SHORT).show();
         }
```
与注册类似，仍然不推荐使用如下方式来获取，除非双方始终协商好使用一个唯一的key（但是这样对于新的调用方或者新加入的开发者不友好，容易入坑):
```java
    ICheckApple checkApple = (ICheckApple) ServiceManager.getInstance().getLocalService("wang.imallen.blog.moduleexportlib.apple.ICheckApple");
...
```
具体使用，可以察看applemodule中LocalServiceDemo这个Activity。如果还有不懂的，可以在热聊中联系**王龙海**进行询问。

###本地接口的Callback问题
如果是耗时操作，由本地接口自己定义Callback接口，调用方在调用接口时传入Callback对象即可。

##远程服务的注册与使用
远程服务的注册与使用略微麻烦一点，因为需要像实现AIDL Service那样定义aidl接口。
###远程接口的定义与实现
定义aidl接口，并且要将编译生成的Stub和Proxy类暴露给所有模块。比如定义一个购买苹果的服务接口:
```aidl
    package wang.imallen.blog.moduleexportlib.apple;
    import org.qiyi.video.svg.IPCCallback;
    
    interface IBuyApple {
        int buyAppleInShop(int userId);
        void buyAppleOnNet(int userId,IPCCallback callback);
    }
```

**至于具体的打包和暴露方式，目前还在实现中，Demo阶段先特殊处理。**

而接口的实现如下:
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
        Logger.d("BuyAppleImpl-->buyAppleInShop,userId:" + userId);
        if (userId == 10) {
            return 20;
        } else if (userId == 20) {
            return 30;
        } else {
            return -1;
        }
    }

    @Override
    public void buyAppleOnNet(int userId, IPCCallback callback) throws RemoteException {
        Logger.d("BuyAppleImpl-->buyAppleOnNet,userId:" + userId);


        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }


        Bundle result = new Bundle();
        if (userId == 10) {
            result.putInt("Result", 20);
            callback.onSuccess(result);
        } else if (userId == 20) {
            result.putInt("Result", 30);
            callback.onSuccess(result);
        } else {
            callback.onFail("Sorry, u are not authorized!");
        }
    }

}

```

###远程接口的注册
与本地接口的注册略有不同，远程接口注册的是继承了Stub类的IBinder部分，注册方式有传递接口Class和接口全路径名两种，如下:
```java
    ServiceManager.getInstance().registerRemoteService(IBuyApple.class, BuyAppleImpl.getInstance().asBinder());
```
其中BuyAppleImpl是继承了IBuyApple.Stub类的具体实现类,另外，其实这里写成下面这样其实也可以:
```java
    ServiceManager.getInstance().registerRemoteService(IBuyApple.class, BuyAppleImpl.getInstance());
```
因为BuyAppleImpl继承的IBuyApple.Stub类继承自android.os.Binder,实现了asBinder()接口;

传递全路径名的注册方式如下:
```java
    ServiceManager.getInstance().registerRemoteService(IBuyApple.class.getCanonicalName(),BuyAppleImpl.getInstance().asBinder());
```

###远程接口的使用
由于跨进程只能传递IBinder,所以只能获取到远程服务的IBinder之后，再调用XX.Stub.asInterface()获取到它的代理,如下:
```java
        //此处也可以是IBinder buyAppleBinder = ServiceManager.getInstance().getRemoteService(IBuyApple.class.getCanonicalName);
        IBinder buyAppleBinder = ServiceManager.getInstance().getRemoteService(IBuyApple.class);
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
值得注意的是，**远程服务其实既可在其他进程中调用，也可以在同进程中被调用，当在同一进程时，虽然调用方式一样，但其实会自动降级为进程内普通的接口调用，这个不需要开发者做额外的处理.**

###远程接口的Callback问题
考虑到远程服务也可能有耗时操作，所以需要支持远程调用的Callback功能。
对于有耗时操作的远程服务，定义接口时需要借助lib中的IPCCallback,如下:
```aidl
    package wang.imallen.blog.moduleexportlib.apple;
    import org.qiyi.video.svg.IPCCallback;
    
    interface IBuyApple {
        int buyAppleInShop(int userId);
        void buyAppleOnNet(int userId,IPCCallback callback);
    }
```
其中的buyAppleOnNet()方法就是耗时操作，所以需要在定义时加上IPCCallback,注意这里不能是自己随便定义的Callback接口，否则aidl编译通不过。
其中的IPCCallback本身也是一个aidl接口，如下:
```aidl
interface IPCCallback {
   void onSuccess(in Bundle result);
   void onFail(String reason);
}
```
对于IPCCallback的调用，固然也可以用由调用者直接继承IPCCallback.Stub类，实现相关接口，如:
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
                        int appleNum = result.getInt("Result", 0);
                        Logger.d("got remote service with callback in other process(:banana),appleNum:" + appleNum);
                        Toast.makeText(BananaActivity.this,
                                "got remote service with callback in other process(:banana),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(String reason) throws RemoteException {
                        Logger.e("buyAppleOnNet failed,reason:" + reason);
                        Toast.makeText(BananaActivity.this, "got remote service failed with callback!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    
```
但是考虑到回调是在Binder线程中，而绝大部分情况下调用者希望回调在主线程，所以lib封装了一个BaseCallback给接入方使用，如下:
```java
   IBinder buyAppleBinder = ServiceManager.getInstance().getRemoteService(IBuyApple.class);
        if (null == buyAppleBinder) {
            return;
        }
        IBuyApple buyApple = IBuyApple.Stub.asInterface(buyAppleBinder);
        if (null != buyApple) {
            try {
                buyApple.buyAppleOnNet(10, new BaseCallback() {
                    @Override
                    public void onSucceed(Bundle result) {
                        int appleNum = result.getInt("Result", 0);
                        Logger.d("got remote service with callback in other process(:banana),appleNum:" + appleNum);
                        Toast.makeText(BananaActivity.this,
                                "got remote service with callback in other process(:banana),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String reason) {
                        Logger.e("buyAppleOnNet failed,reason:" + reason);
                        Toast.makeText(BananaActivity.this, "got remote service failed with callback!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
```
**第二种方式也是我们推荐的使用方式!**

详情可察看applemodule中的BananaActivity,如果还有疑问，欢迎联系**王龙海**进行讨论。

# 已知问题


# 技术支持

1. Sample代码
2. 阅读Wiki或者FAQ
3. 联系@王龙海