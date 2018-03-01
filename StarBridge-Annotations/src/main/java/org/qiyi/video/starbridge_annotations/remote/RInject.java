package org.qiyi.video.starbridge_annotations.remote;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangallen on 2018/2/11.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface RInject {
    Class<?> value() default Object.class;
}
