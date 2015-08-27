package com.yuwell.mobilegp.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.yuwell.mobilegp.R;
import com.yuwell.mobilegp.bluetooth.BluetoothConnectionListener;
import com.yuwell.mobilegp.bluetooth.BluetoothService;
import com.yuwell.mobilegp.bluetooth.Constants;
import com.yuwell.mobilegp.common.event.EventListener;
import com.yuwell.mobilegp.common.event.EventMessage;

import java.util.Iterator;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * Created by Chen on 2015/8/11.
 */
public abstract class BTActivity extends Activity
        implements EventListener, BluetoothConnectionListener {

    private static final String TAG = BTActivity.class.getSimpleName();

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothService mService = null;

    private BluetoothDevice mDeviceToConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_available, Toast.LENGTH_LONG).show();
            finish();
        }

        if (mService == null) {
            mService = new BluetoothService(false);
        }

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        doDiscovery();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.stop();
        }

        // Make sure we're not doing discovery anymore
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onEventMainThread(Message event) {
        switch (event.what) {
            case EventMessage.STATE_CHANGE:
                switch (event.arg1) {
                    case Constants.STATE_CONNECTED:
                        onDeviceConnected();
                        break;
                    case Constants.STATE_DISCONNECTED:
                        onDeviceDisconnected();
                        break;
                }
                break;
            case EventMessage.CONNECTION_FAILED:
                onDeviceConnectionFailed();
                break;
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        Set<BluetoothDevice> bondedBluetoothDevices = mBluetoothAdapter.getBondedDevices();
        if (bondedBluetoothDevices.size() > 0) {
            Iterator<BluetoothDevice> iterator = bondedBluetoothDevices.iterator();
            while (iterator.hasNext()) {
                BluetoothDevice bluetoothDevice = iterator.next();
                if (getDeviceName().equalsIgnoreCase(bluetoothDevice.getName())) {
                    mDeviceToConnect = bluetoothDevice;
                    mService.connect(bluetoothDevice);
                    return;
                }
            }
        }

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }

    public BluetoothService getService() {
        return mService;
    }

    protected synchronized void write(byte[] out) {
        mService.write(out);
    }

    public abstract String getDeviceName();

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Device Name:" + device.getName());
                // If it's already paired, skip it, because it's been listed already
                if (device != null && getDeviceName().equalsIgnoreCase(device.getName())) {
                    mDeviceToConnect = device;
                    mService.connect(device);
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery finished");
                showMessage("未发现设备");
            }
        }
    };

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
