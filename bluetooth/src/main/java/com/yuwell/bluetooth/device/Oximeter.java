package com.yuwell.bluetooth.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Message;
import android.util.Log;

import com.yuwell.bluetooth.Utils;
import com.yuwell.bluetooth.constants.BleMessage;
import com.yuwell.bluetooth.constants.Characteristic;
import com.yuwell.bluetooth.constants.Service;

import de.greenrobot.event.EventBus;

/**
 * Created by Chen on 2015/9/14.
 */
public class Oximeter extends BleDevice {

    public static final String TAG = Oximeter.class.getSimpleName();

    @Override
    public boolean connectable(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return "Tv221u-4C4AB482".equals(device.getName());
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        BluetoothGattService mOximeterMeasurement = gatt.getService(Service.OXIMETER);
        if (mOximeterMeasurement != null) {
            BluetoothGattCharacteristic characteristic = mOximeterMeasurement.getCharacteristic(Characteristic.OXIMETER);
            if (characteristic != null) {
                setCharacteristic(gatt, characteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        decodeData(characteristic);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        decodeData(characteristic);
    }

    private void decodeData(BluetoothGattCharacteristic characteristic) {
        if (Characteristic.OXIMETER.equals(characteristic.getUuid())) {
            byte[] value = characteristic.getValue();
            String byteStr = Utils.bytesToHexString(value);
            int length = byteStr.length();
            if (length == 16 && byteStr.startsWith("fe0856")) {
                Log.d(TAG, "Wave:" + byteStr);
            }
            if (length > 16) {
                int index = byteStr.indexOf("fe0a55");
                if (length >= index + 20) {
                    String result = byteStr.substring(index, index + 20);
                    int pr = Integer.parseInt(result.substring(6, 10), 16);
                    int spo2 = Integer.parseInt(result.substring(10, 12), 16);
                    int pi = Integer.parseInt(result.substring(12, 16), 16) / 1000;
                    if (pr < 301 && pr > 0) {
                        int[] data = new int[]{pr, spo2, pi};
                        postData(data);
                    }
                }
            }
        }
    }

    private void postData(int[] data) {
        Message event = new Message();
        event.what = BleMessage.OXI_DATA;
        event.obj = data;
        EventBus.getDefault().post(event);
    }

}
