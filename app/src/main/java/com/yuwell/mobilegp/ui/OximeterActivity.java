package com.yuwell.mobilegp.ui;

import android.app.Activity;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.widget.TextView;

import com.yuwell.bluetooth.constants.BleMessage;
import com.yuwell.bluetooth.core.BleService;
import com.yuwell.bluetooth.device.Oximeter;
import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.common.event.EventListener;

import de.greenrobot.event.EventBus;

/**
 * Created by Chen on 2015/9/8.
 */
public class OximeterActivity extends Activity implements EventListener {

    private TextView mPulseRate;
    private TextView mSpo2;
    private TextView mPi;
    private TextView mState;

    private BleService.LocalBinder mBluetoothLeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        setContentView(R.layout.oximeter_activity);
        mPulseRate = (TextView) findViewById(R.id.tv_pulse_rate);
        mSpo2 = (TextView) findViewById(R.id.tv_spo2);
        mState = (TextView) findViewById(R.id.tv_state);
        mPi = (TextView) findViewById(R.id.tv_pi);

        startBleService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从后台重新切换回当前Activity
        if (mBluetoothLeService != null) {
            if (BluetoothProfile.STATE_DISCONNECTED == mBluetoothLeService.getConnectionState()) {
                mBluetoothLeService.scanBleDevice();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBluetoothLeService != null) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }

        EventBus.getDefault().unregister(this);
    }

    private void startBleService() {
        Intent startService = new Intent(this, BleService.class);
        bindService(startService, mServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * Service连接回调函数
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = (BleService.LocalBinder) service;
            mBluetoothLeService.setDevice(new Oximeter());
            // Activity创建时判断当前蓝牙服务连接状态
            // 若断开则重新扫描，否则直接更新界面
            int state = mBluetoothLeService.getConnectionState();
            if (state == BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothLeService.scanBleDevice();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onEventMainThread(Message event) {
        switch (event.what) {
            case BleMessage.STATE_CHANGE:
                switch (event.arg1) {
                    case BluetoothProfile.STATE_CONNECTED:
                        mState.setText("已连接");
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        mState.setText("已断开");
                        break;
                }
                break;
            case BleMessage.OXI_DATA:
                int[] data = (int[]) event.obj;
                mPulseRate.setText(String.valueOf(data[0]));
                mSpo2.setText(String.valueOf(data[1]));
                mPi.setText(String.valueOf(data[2]));
        }
    }
}
