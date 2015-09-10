package com.yuwell.bluetooth.constants;

/**
 * Notify user about ble messages
 * Created by Chen on 2015/9/9.
 */
public interface BleMessage {

    int STATE_CHANGE = 0x1000;

    int BP_DATA = 0x1001;

    int BG_DATA = 0x1002;

    int ICP_DATA = 0x1003;

    int BATTERY = 0x1004;
}
