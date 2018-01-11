package org.qiyi.video.svg.helper;

import android.os.IBinder;

import org.qiyi.video.svg.config.Constants;

import wang.imallen.blog.moduleexportlib.apple.DeliverAppleStub;
import wang.imallen.blog.moduleexportlib.show.IDisplayTalkShow;

/**
 * Created by wangallen on 2018/1/10.
 */
//TODO 这个要采用注解或者是gradle插件来生成,不过一开始甚至可以考虑人为写死
//TODO 还是要采用动态代理+参数处理才行,即相当于有一个通用的proxy
public class MatchPolicy {
    public static Object asInterface(String serviceName, IBinder binder) {

        switch (serviceName) {
            case "wang.imallen.blog.moduleexportlib.apple.IDeliverApple": {
                return DeliverAppleStub.asInterface(binder);
            }
            case "wang.imallen.blog.moduleexportlib.show.IDisplayTalkShow": {
                return IDisplayTalkShow.Stub.asInterface(binder);
            }
            case Constants.APPLE_MODULE: {
                //TODO 这里有可能采用自己生成一个Proxy来实现，这样就不需要暴露出Stub类了
                return DeliverAppleStub.asInterface(binder);
            }
            case Constants.CHERRY_MODULE: {
                //TODO 暂时先不实现cherry的，后面再补上
            }
            default:
                break;
        }
        return null;


    }

    //一开始根据packageName进行Service的匹配
    public static String getServiceAction(String serviceName) {
        if (serviceName.startsWith("wang.imallen.blog.moduleexportlib.apple")) {
            return Constants.APPLE_PROCESS_SEVICE_ACTION;
        } else if (serviceName.startsWith("wang.imallen.blog.moduleexportlib.show")) {
            return Constants.PLUGIN1_PROCESS_SERVICE_ACTION;
        }
        return "";
    }


}
