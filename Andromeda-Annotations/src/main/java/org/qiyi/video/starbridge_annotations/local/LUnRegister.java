package org.qiyi.video.starbridge_annotations.local;

/**
 * Created by wangallen on 2018/2/11.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface LUnRegister {
    Class<?>[] value() default {Object.class};
}
