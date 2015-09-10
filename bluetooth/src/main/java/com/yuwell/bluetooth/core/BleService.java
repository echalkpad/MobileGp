package com.yuwell.bluetooth.core;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.yuwell.bluetooth.constants.BleMessage;
import com.yuwell.bluetooth.device.BleDevice;

import de.greenrobot.event.EventBus;

/**
 * A service which controls ble devices
 * Created by Chen on 2015/9/9.
 */
public class BleService extends Service {

    private static final String TAG = BleService.class.getSimpleName();

    // Loop scan pause interval
    private static final int SCAN_INTERVAL = 10 * 1000;

    private final IBinder mBinder = new LocalBinder();
    private final Handler mHandler = new Handler();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private BleDevice mDevice;
    private String mBluetoothDeviceAddress;

    // Whether it is scanning ble
    private boolean isScanning;
    // Whether it has started scanning
    private boolean isScanStarted = false;
    private boolean isNetworkActive = false;
    private boolean isUserDisconnected = false;

    private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }

        public boolean setDevice(BleDevice device) {
            if (mDevice != null && mDevice.equals(device) && mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                return false;
            } else {
                mDevice = device;
                if (mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                    isUserDisconnected = true;
                    closeGatt();
                }
                scanDevice();
                return true;
            }
        }

        public int getConnectionState() {
            return mConnectionState;
        }

        /**
         * Start to scan ble devices after 500ms
         */
        public void scanBleDevice() {
            scanDevice();
        }

        /**
         * Disconnect current connection
         */
        public void disconnect() {
            if (mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                closeGatt();
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through BluetoothManager.
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            Log.e(TAG, "Unable to initialize BluetoothManager.");
        } else {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        enableBluetoothAdapter();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLoopScan();
        close();
    }

    private void scanDevice() {
        if (!isScanStarted && mConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanLeDevice(true, true);
                }
            }, 500);
            isScanStarted = true;
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    private void close() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mBluetoothAdapter = null;
    }

    /**
     * Disconnect & close bluetooth gatt
     */
    private void closeGatt() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    }

    private void enableBluetoothAdapter() {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    private void stopLoopScan() {
        if (mConnectionState != BluetoothProfile.STATE_CONNECTED) {
            if (isScanning) {
                scanLeDevice(false, false);
            } else {
                mHandler.removeCallbacksAndMessages(null);
                isScanStarted = false;
            }
        }
    }

    /**
     * 搜索BLE设备
     * @param enable Turn on / off
     * @param loopScan Scan periodically
     */
    private void scanLeDevice(final boolean enable, boolean loopScan) {
        if (mBluetoothAdapter != null) {
            if (enable) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                isScanning = true;

                if (loopScan) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scanLeDevice(false, true);
                        }
                    }, SCAN_INTERVAL);
                }
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                isScanning = false;

                if (loopScan && !isNetworkActive) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scanLeDevice(true, true);
                        }
                    }, SCAN_INTERVAL);
                } else {
                    mHandler.removeCallbacksAndMessages(null);
                    isScanStarted = false;
                }
            }
        }
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address
     *            The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The
     *         connection result is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    private boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        if (!TextUtils.isEmpty(mBluetoothDeviceAddress) && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = BluetoothProfile.STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = BluetoothProfile.STATE_CONNECTING;
        return true;
    }

    private void postConnectionState() {
        Message event = new Message();
        event.what = BleMessage.STATE_CHANGE;
        event.arg1 = mConnectionState;
        EventBus.getDefault().post(event);
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (mDevice != null && mDevice.connectable(device, rssi, scanRecord)) {
                mDevice.setBluetoothDevice(device);
                boolean isConnectionInitiated = connect(device.getAddress());
                if (isConnectionInitiated) {
                    scanLeDevice(false, false);
                    postConnectionState();
                }
            }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            mConnectionState = newState;
            postConnectionState();

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");

                if (isUserDisconnected) {
                    closeGatt();
                    isUserDisconnected = false;
                }

                scanDevice();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "onServicesDiscovered from GATT server." + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mDevice != null) {
                    mDevice.onServicesDiscovered(gatt, status);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (mDevice != null) {
                mDevice.onCharacteristicChanged(gatt, characteristic);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mDevice != null) {
                    mDevice.onCharacteristicRead(gatt, characteristic, status);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mDevice != null) {
                    mDevice.onCharacteristicWrite(gatt, characteristic, status);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mDevice != null) {
                    mDevice.onDescriptorWrite(gatt, descriptor, status);
                }
            }
        }
    };

}
