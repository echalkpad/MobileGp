package com.yuwell.bluetooth.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

/**
 * Abstract bluetooth le device
 * Created by Chen on 2015/9/9.
 */
public abstract class BleDevice extends BluetoothGattCallback {

    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice bluetoothDevice;

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public abstract boolean connectable(final BluetoothDevice device, int rssi, byte[] scanRecord);

    public abstract void onServicesDiscovered(BluetoothGatt gatt, int status);

    public abstract void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    public abstract void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    /**
     * Enables notification on a give characteristic.
     *  @param characteristic Characteristic to act on.
     */
    protected void setCharacteristic(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic characteristic, byte[] value) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(value);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    @Override
    public boolean equals(Object o) {
        return getClass().getSimpleName().equals(o.getClass().getSimpleName());
    }
}
