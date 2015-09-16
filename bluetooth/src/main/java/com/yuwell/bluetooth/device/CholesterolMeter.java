package com.yuwell.bluetooth.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.yuwell.bluetooth.constants.Characteristic;
import com.yuwell.bluetooth.constants.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Cholesterol meter behavior
 * Created by Chen on 2015/9/16.
 */
public class CholesterolMeter extends BleDevice {

    private static final String TAG = CholesterolMeter.class.getSimpleName();

    private BluetoothGattCharacteristic writeCharacteristic = null;

    @Override
    public boolean connectable(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return "iGate".equals(device.getName());
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        BluetoothGattService service = gatt.getService(Service.CHOLESTEROL);
        if (service != null) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                if (characteristic.getUuid().equals(Characteristic.CHOLESTEROL_READ)) {
                    if (characteristic.getProperties() == BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) {
                        writeCharacteristic = characteristic;
                    } else {
                        setCharacteristic(gatt, characteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    }
                }
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        decodeData(characteristic);
    }

    public void readData() {
        sendCommand("GET");
    }

    public void sendCommand(String command) {
        if (mBluetoothGatt != null && writeCharacteristic != null) {
            writeCharacteristic.setValue(command.getBytes());
            mBluetoothGatt.writeCharacteristic(writeCharacteristic);
        }
    }

    private void decodeData(BluetoothGattCharacteristic characteristic) {
        if (Characteristic.CHOLESTEROL_READ.equals(characteristic.getUuid())) {
            byte[] data = characteristic.getValue();
            String strData;
            try {
                strData = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "error", e);
                strData = "";
            }
            Log.d(TAG, "received:" + strData);
        }
    }
}
