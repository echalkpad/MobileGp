package com.yuwell.mobilegp.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.bluetooth.OnDataRead;

/**
 * Created by Chen on 2015/9/8.
 */
public class OximeterActivity extends BTActivity implements OnDataRead {

    private TextView mPulseRate;
    private TextView mSpo2;
    private TextView mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oximeter_activity);
        mPulseRate = (TextView) findViewById(R.id.tv_pulse_rate);
        mSpo2 = (TextView) findViewById(R.id.tv_spo2);
        mState = (TextView) findViewById(R.id.tv_state);
    }

    @Override
    public String getDeviceName() {
        return "Tv221u-4C4AB482";
    }

    @Override
    public boolean doDiscoveryOnCreate() {
        return true;
    }

    @Override
    public void onNothingDiscovered() {
        mState.setText("未发现设备");
    }

    @Override
    public void onDeviceConnected() {
        mState.setText("已连接");
    }

    @Override
    public void onDeviceDisconnected() {
        mState.setText("连接断开");
    }

    @Override
    public void onDeviceConnectionFailed() {
        mState.setText("连接失败");
    }

    @Override
    public void onRead(int dataSize, byte[] data) {
        Log.e("Oximeter", "Data:" + dataSize);
    }
}
