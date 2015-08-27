package com.yuwell.mobilegp.bluetooth;

/**
 * Created by chenshuai on 2015/8/26.
 */
public interface BluetoothConnectionListener {
    void onDeviceConnected();
    void onDeviceDisconnected();
    void onDeviceConnectionFailed();
}
