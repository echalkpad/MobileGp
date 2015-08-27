package com.yuwell.mobilegp.common.event;

/**
 * 总线消息事件
 * Created by Chen on 2015/4/24.
 */
public interface EventMessage {

     int DATA_RECEIVED = 1;

    int STATE_CHANGE = 2;

    int CONNECTION_FAILED = 3;

    int CONNECTION_LOST = 4;
}
