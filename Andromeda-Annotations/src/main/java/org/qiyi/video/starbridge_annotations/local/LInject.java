package org.qiyi.video.starbridge_annotations.local;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangallen on 2018/2/11.
 */
//注入本地服务对象,如果使用默认值，则使用其定义类型
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface LInject {
    Class<?> value() default Object.class;
}
