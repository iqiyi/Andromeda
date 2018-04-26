# Andromeda
![Andromeda_license](https://img.shields.io/badge/license-BSD--3--Clause-brightgreen.svg)
![Andromeda_core_tag](https://img.shields.io/badge/Andromeda%20core-1.1.0-brightgreen.svg)
![Andromeda_plugin_tag](https://img.shields.io/badge/Andromeda%20plugin-1.1.0-brightgreen.svg)

Andromeda提供了接口式的组件间通信管理，包括同进程的本地接口调用和跨进程接口调用。

**注:之所以分成本地服务和远程服务这两种，是由于本地服务的接口可以传递各种类型的参数和返回值，而远程接口则受AIDL的限制，参数和返回值只能是基本类型或者实现了Parcelable接口的自定义类型。**

# 特色

+ 无需开发者进行bindService()操作,也不用定义Service,只需要定义aidl接口和实现

+ 同步获取服务。抛弃了bindService()这种异步获取的方式，改造成了同步获取

+ 生命周期自动管理。可根据Fragment或Activity的生命周期进行提高或降低服务提供方进程的操作

+ 支持IPC的Callback，并且支持跨进程的事件总线

+ 采用"接口+数据结构"的方式来实现组件间通信，这种方式相比协议的方式在于实现简单，维护方便


**注意这里的服务不是Android中四大组件的Service,而是指提供的接口与实现。为了表示区分，后面的服务均是这个含义，而Service则是指Android中的组件。**

Andromeda和其他组件间通信方案的对比如下:

|       |    使用方便性     | 代码侵入性  |   互操作性    |  是否支持IPC   |  是否支持跨进程事件总线  |  是否支持页面跳转  |
| :---: | :-------: | :----------: |:----------: |:----------: |:----------: |:----------: |
| Andromeda |  好     |   较小     |    好    |    Yes    |   Yes    |   No     |
| DDComponentForAndroid |  较差      |   较大     |    差    |   No     |   No    |   Yes     |
| ARouter |  较好      |   较大     |    差    |   No     |   No    |    Yes    |


# 接入方式
首先在buildscript中添加classpath(请使用最新的版本名称来替换$version):
```groovy
    classpath "org.qiyi.video.svg:plugin:$version"
```
这两个分别是核心代码库和gradle插件库的路径。
在Application或library Module中使用核心库:
```groovy
    implementation "org.qiyi.video.svg:core:$version"
```
在application Module中使用gradle插件:
```groovy
    apply plugin: 'org.qiyi.svg.plugin'
```

# 使用方式
## 为Dispatcher配置进程
由于Dispatcher负责管理所有进程信息，所以它应该运行在存活时间最长的进程中。
如果不进行配置，Dispatcher默认运行在主进程中。
但是考虑到在有些App中，主进程不一定是存活时间最长的(比如音乐播放App中往往是播放进程的存活时间最长),
所以出现这种情况时开发者应该在application module的build.gradle中为Dispatcher配置进程名，如下:
```groovy
    dispatcher{
        process ":downloader"
    }
```
在这里，":downloader"进程是存活时间最长的.

## 初始化
最好是在自己进程的Application中进行初始化(每个进程都有自己的Andromeda对象)，代码如下:
```java
    Andromeda.init(Context);
```

## 本地服务的注册与使用
### 本地接口定义与实现
本地接口定义与实现这方面，和普通的接口定义、实现没什么太大区别，不一样的地方就两个:
+ 对外接口需要要暴露出去，使其对项目中的所有模块都可见，比如可以放在一个公共的module中
+ 如果对于某个接口有多个实现，那么需要根据业务需求在不同的时候注册不同的实现到Andromeda,不过需要注意的是，同一时间Andromeda中只会有一个实现

### 本地服务注册
本地服务的注册有两种方法，一种是直接调用接口的全路径名和接口的实现，如下:
```java
    Andromeda.registerLocalService(ICheckApple.class.getCanonicalName(),new CheckApple());
```
还有一种是调用接口class和接口的实现，其实在内部也是获取了它的全路径名,如下:
```java
    Andromeda.registerLocalService(ICheckApple.class,new CheckApple());
```
其中ICheckApple.class为接口，虽然也可以采用下面这种方式注册:
```java
    Andromeda.registerLocalService("wang.imallen.blog.moduleexportlib.apple.ICheckApple",CheckAppleImpl.getInstance());
```
**但是考虑到混淆问题，非常不推荐使用这种方式进行注册**，除非双方能够协商一致使用这个key(因为实际上Andromeda只需要保证有一个唯一的key与该服务对应即可).

### 本地服务使用
注册完之后，与服务提供方同进程的任何模块都可以调用该服务,获取服务的方式与注册对应，也有两种方式，一种是通过接口的class获取,如下:
```java
    ICheckApple checkApple = (ICheckApple) Andromeda.getLocalService(ICheckApple.class);
```
还有一种方法是通过接口的全路径名获取，如下:
```java
    ICheckApple checkApple = (ICheckApple) Andromeda.getLocalService(ICheckApple.class.getCanonicalName());
```
与注册类似，仍然不推荐使用如下方式来获取，除非双方始终协商好使用一个唯一的key（但是这样对于新的调用方或者新加入的开发者不友好，容易入坑):
```java
    ICheckApple checkApple = (ICheckApple) Andromeda.getLocalService("wang.imallen.blog.moduleexportlib.apple.ICheckApple");
```
具体使用，可以察看applemodule中LocalServiceDemo这个Activity。

### 本地接口的Callback问题
如果是耗时操作，由本地接口自己定义Callback接口，调用方在调用接口时传入Callback对象即可。

## 远程服务的注册与使用
远程服务的注册与使用略微麻烦一点，因为需要像实现AIDL Service那样定义aidl接口。
### 远程接口的定义与实现
定义aidl接口，并且要将编译生成的Stub和Proxy类暴露给所有模块, 类似的，也是放在公共module中，以暴露给其他模块使用。比如定义一个购买苹果的服务接口:
```aidl
    package wang.imallen.blog.moduleexportlib.apple;
    import org.qiyi.video.svg.IPCCallback;
    
    interface IBuyApple {
        int buyAppleInShop(int userId);
        void buyAppleOnNet(int userId,IPCCallback callback);
    }
```

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
       ...
    }

    @Override
    public void buyAppleOnNet(int userId, IPCCallback callback) throws RemoteException {
       ...
    }
}

```

### 远程服务的注册
与本地接口的注册略有不同，远程接口注册的是继承了Stub类的IBinder部分，注册方式有传递接口Class和接口全路径名两种，如下:
```java
    Andromeda.registerRemoteService(IBuyApple.class, BuyAppleImpl.getInstance().asBinder());
```
其中BuyAppleImpl是继承了IBuyApple.Stub类的具体实现类,另外，其实这里写成下面这样其实也可以:
```java
    Andromeda.registerRemoteService(IBuyApple.class, BuyAppleImpl.getInstance());
```
因为BuyAppleImpl继承的IBuyApple.Stub类继承自android.os.Binder,实现了asBinder()接口;

传递全路径名的注册方式如下:
```java
    Andromeda.registerRemoteService(IBuyApple.class.getCanonicalName(),BuyAppleImpl.getInstance().asBinder());
```

### 远程服务的使用
+ 由于Andromeda利用bindService()来提升通信过程中的优先级，对于在Fragment或者Activity中使用的情形，可在onDestroy()时自动释放连接，所以需要调用先调用with();
+ 由于跨进程只能传递IBinder,所以只能获取到远程服务的IBinder之后，再调用XX.Stub.asInterface()获取到它的代理.

以FragmentActivity中使用为例,如下的this是指FragmentActivity:
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
其他的在android.app.Fragment,android.support.v4.app.Fragment，以及普通的Activity中远程服务的使用类似，可查看app module中的CustomFragment,CustomSupportFragment,FragActivity等，不再赘述。

值得注意的是，**远程服务其实既可在其他进程中调用，也可以在同进程中被调用，当在同一进程时，虽然调用方式一样，但其实会自动降级为进程内普通的接口调用，这个binder会自动处理.**

### 远程服务的Callback问题
考虑到远程服务也可能有耗时操作，所以需要支持远程调用的Callback功能。
对于有耗时操作的远程服务，定义接口时需要借助lib中的IPCCallback,如下:
```aidl
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
但是考虑到回调是在Binder线程中，而绝大部分情况下调用者希望回调在主线程，所以lib封装了一个BaseCallback给接入方使用，如下:
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
**第二种方式也是我们推荐的使用方式!**

详情可察看applemodule中的BananaActivity.

### 生命周期自动管理的问题
对于IPC,为了提高对方进程的优先极，在使用Andromeda.with().getRemoteService()时会进行bindService()操作。
既然进行了bind操作，那自然要进行unbind操作以释放连接了，目前有如下两种情形。
+ 对于在Fragment或者Activity中，并且是在主线程中调用的情形，只要在获取远程服务时利用with()传递Fragment或者Activity对象进去，Andromeda就会在onDestroy()时自动释放连接，不需要开发者做任何unbind()操作。

+ 对于在子线程或者不是Fragment/Activity中的情形，只能传递Application Context去获取远程服务。在使用完毕后，需要手动调用Andromeda的unbind()释放连接:

```java
    public static void unbind(Class<?> serviceClass);
    public static void unbind(Set<Class<?>> serviceClasses);
```
  如果只获取了一个远程服务，那么就使用前一个unbind()方法;否则使用后一个。

## 事件订阅与发布
### 事件
Andromeda中Event的定义如下:
```java
    public class Event implements Parcelable {
    
        private String name;
    
        private Bundle data;
        
        ...
    }
```
即 事件=名称+数据，通信时将需要传递的数据存放在Bundle中。  
其中**名称要求在整个项目中唯一**，否则可能出错。
由于要跨进程传输，所以所有数据只能放在Bundle中进行包装。

### 事件订阅
事件订阅很简单，首先需要有一个实现了EventListener接口的对象。
然后就可以订阅自己感兴趣的事件了，如下:
```java
    Andromeda.subscribe(EventConstants.APPLE_EVENT,MainActivity.this);
```
其中MainActivity实现了EventListener接口，此处表示订阅了名称为EventConstnts.APPLE_EVENT的事件。

### 事件发布
事件发布很简单，调用publish方法即可，如下:
```java
    Bundle bundle = new Bundle();
    bundle.putString("Result", "gave u five apples!");
    Andromeda.publish(new Event(EventConstants.APPLE_EVENT, bundle));
```
# License
BSD-3-Clause. See the [BSD-3-Clause](https://opensource.org/licenses/BSD-3-Clause) file for details.

# 支持
1. Sample代码
2. 阅读Wiki或者FAQ
3. 联系bettarwang@gmail.com