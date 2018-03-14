package org.qiyi.video.starbridge_annotations.local;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangallen on 2018/2/11.
 */
//Local Bind的简称,由LBind这个注解记录下要注册的本地服务的实例名称
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface LBind {
    Class<?> value() default Object.class;
}
