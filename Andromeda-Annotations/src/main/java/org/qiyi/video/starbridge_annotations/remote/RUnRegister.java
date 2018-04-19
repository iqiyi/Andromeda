package org.qiyi.video.starbridge_annotations.remote;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 由于远程服务还未提供注销服务的功能，所以这个注解暂时还用不到
 * Created by wangallen on 2018/2/11.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface RUnRegister {
    Class<?>[] value() default {Object.class};
}
