package wang.imallen.blog.applemodule;

import wang.imallen.blog.moduleexportlib.apple.DeliverAppleStub;
import wang.imallen.blog.moduleexportlib.apple.IDeliverApple;

/**
 * Created by wangallen on 2018/1/8.
 */

public class DeliverAppleNative extends DeliverAppleStub {

    //TODO 实在不行就连实现类的名称也收集，然后在调用时使用反射
    private IDeliverApple deliverApple = new DeliveryAppleImpl();

    @Override
    public int getApple(int userId) {
        return deliverApple.getApple(userId);
    }

    @Override
    public void sendApple(int saleId, int appleNum) {
        deliverApple.sendApple(saleId, appleNum);
    }
}
