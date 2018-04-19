package org.qiyi.video.starbridge_annotations.local;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangallen on 2018/2/11.
 */
//用来修饰方法，会在一个方法的最后进行注册，使用者需要保证在这个方法的最后一句之前(包含最后一句)完成对象的初始化
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface LRegister {
    Class<?>[] value() default {Object.class};
}
