package com.yuwell.mobilegp.common.event;

import android.os.Message;

/**
 * 总线事件监听
 * Created by Chen on 2015/3/11.
 */
public interface EventListener {

    void onEventMainThread(Message event);
}
