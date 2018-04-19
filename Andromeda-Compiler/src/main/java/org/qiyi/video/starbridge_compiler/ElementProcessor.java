package org.qiyi.video.starbridge_compiler;

import java.util.Set;

import javax.lang.model.element.Element;

/**
 * Created by wangallen on 2018/3/5.
 */

public interface ElementProcessor {
    void process(Set<? extends Element> elements) throws ProcessingException;
}
