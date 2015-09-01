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

    // 蓝牙连接状态改变
    int BLE_STATE_CHANGE = 0x10;

    // 电池电量
    int BATTERY_LEVEL = 0x11;

    // 蓝牙数据
    int BLE_DATA = 0x12;

    // 扫描蓝牙设备
    int BLE_ACTIVITY_START = 0x16;

    // 停止扫描蓝牙设备
    int STOP_LE_SCAN = 0x17;

    // 点击打印
    int ON_PRINT = 0x18;
}
