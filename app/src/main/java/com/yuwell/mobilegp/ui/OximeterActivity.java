package com.yuwell.mobilegp.ui;

/**
 * Created by Chen on 2015/9/8.
 */
public class OximeterActivity extends BTActivity {

    @Override
    public String getDeviceName() {
        return null;
    }

    @Override
    public boolean doDiscoveryOnCreate() {
        return true;
    }

    @Override
    public void onNothingDiscovered() {

    }

    @Override
    public void onDeviceConnected() {

    }

    @Override
    public void onDeviceDisconnected() {

    }

    @Override
    public void onDeviceConnectionFailed() {

    }
}
