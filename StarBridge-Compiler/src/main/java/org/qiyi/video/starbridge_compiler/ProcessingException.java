package org.qiyi.video.starbridge_compiler;

import javax.lang.model.element.Element;

/**
 * Created by wangallen on 2018/2/11.
 */

public class ProcessingException extends Exception {

    Element element;

    public ProcessingException(String msg) {
        super(msg);
    }

    public ProcessingException(String msg, Object... args) {
        super(String.format(msg, args));
    }

    public ProcessingException(Element element, String msg, Object... args) {
        super(String.format(msg, args));
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

}
