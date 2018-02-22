package org.qiyi.video.starbridge_annotations.remote;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//TODO 对于@Remote和对于@Local需要的信息可以不一样，因为对于远程服务来说，不像本地服务那么复杂，基本来说在Application中初始化就可以
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Remote {
    //实现的服务接口是哪一个,如果不写就表示它只实现了一个接口
    Class<?>serivce()default Object.class;

    //getInstance()是否需要参数
    boolean needContext() default false;

    /*
    //简单模式，可通过默认构造方法来创建对象，或者通过getInstance()来创建对象,并且是在Application中创建
    //boolean simple() default true;

    boolean singleton() default false;

    boolean singleWithContext() default false;
    */
}
