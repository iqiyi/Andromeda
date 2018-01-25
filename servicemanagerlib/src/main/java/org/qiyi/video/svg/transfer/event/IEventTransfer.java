package org.qiyi.video.svg.transfer.event;

import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;

/**
 * Created by wangallen on 2018/1/25.
 */

public interface IEventTransfer {

    void subscribeEvent(String name, EventListener listener);

    void unsubscribeEvent(EventListener listener);

    void publish(Event event);

}
