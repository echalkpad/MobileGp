package com.yuwell.bluetooth.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.yuwell.bluetooth.Utils;
import com.yuwell.bluetooth.constants.Characteristic;
import com.yuwell.bluetooth.constants.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Chen on 2015/9/15.
 */
public class UrineAnalyzer extends BleDevice {

    private static final byte[] DEVICE_CONFIRM = {(byte) 0x93, (byte) 0x8e, 0x08, 0x00, 0x08, 0x01, 0x43, 0x4f, 0x4e, 0x54, 0x45};
    private static final byte[] READ_SINGLE_DATA = {(byte) 0x93, (byte) 0x8e, 0x04, 0x00, 0x08, 0x04, 0x10};

    private boolean confirm = false;

    private List<String> data = new ArrayList<>();

    private BluetoothGattCharacteristic characteristicToWrite = null;

    @Override
    public boolean connectable(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return "BLE-EMP-Ui".equals(device.getName());
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        BluetoothGattService service = gatt.getService(Service.URINE_MEASUREMENT);
        if (service != null) {
            characteristicToWrite = service.getCharacteristic(Characteristic.URINE_WRITE);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(Characteristic.URINE_INDICATE);
            if (characteristicToWrite != null && characteristic != null) {
                setCharacteristic(gatt, characteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        decodeData(characteristic);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (descriptor.getCharacteristic().getUuid().equals(Characteristic.URINE_INDICATE)) {
            sendCommand(DEVICE_CONFIRM);
        }
    }

    public void readLastData() {
        data.clear();
        sendCommand(READ_SINGLE_DATA);
    }

    private void sendCommand(byte[] value) {
        if (mBluetoothGatt != null && characteristicToWrite != null) {
            characteristicToWrite.setValue(value);
            mBluetoothGatt.writeCharacteristic(characteristicToWrite);
        }
    }

    private void setTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 2000;
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int checkSum = 19 + year + month + day + hour + min;

        byte[] array = new byte[12];
        array[0] = (byte) 0x93;
        array[1] = (byte) 0x8e;
        array[2] = 0x09;
        array[3] = 0x00;
        array[4] = 0x08;
        array[5] = 0x02;
        array[6] = (byte) Utils.decimalToHex(year);
        array[7] = (byte) Utils.decimalToHex(month);
        array[8] = (byte) Utils.decimalToHex(day);
        array[9] = (byte) Utils.decimalToHex(hour);
        array[10] = (byte) Utils.decimalToHex(min);
        array[11] = (byte) Utils.decimalToHex(checkSum);

        sendCommand(array);
    }

    private void decodeData(BluetoothGattCharacteristic characteristic) {
        if (Characteristic.URINE_INDICATE.equals(characteristic.getUuid())) {
            byte[] value = characteristic.getValue();
            String[] array = Utils.bytesToHexStringArray(value);
            Log.d("URINE", "BYTE:" + Utils.bytesToHexString(value));

            if ("93".equals(array[0]) && "8e".equals(array[1])) {
                switch (Byte.parseByte(array[5])) {
                    case 0x01:
                        confirm = true;
                        setTime();
                        break;
                    case 0x04:
                        data.addAll(Arrays.asList(array));
                        break;
                }
            } else if (data.size() == 14) {
                data.addAll(Arrays.asList(array));

            }
        }
    }
}
