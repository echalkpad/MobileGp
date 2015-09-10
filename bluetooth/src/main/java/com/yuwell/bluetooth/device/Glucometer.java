package com.yuwell.bluetooth.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Message;

import com.yuwell.bluetooth.Utils;
import com.yuwell.bluetooth.constants.BleMessage;
import com.yuwell.bluetooth.constants.Characteristic;
import com.yuwell.bluetooth.constants.Service;

import java.util.Calendar;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Glucometer behavior
 * Created by Chen on 2015/9/9.
 */
public class Glucometer extends BleDevice {

    private static final String TAG = Glucometer.class.getSimpleName();

    private static final int UNIT_kgpl = 0;
    private static final int UNIT_molpl = 1;

    private BluetoothGattCharacteristic mGMCharacteristic;
    private BluetoothGattCharacteristic mDateTimeCharacteristic;

    private boolean readBattery;

    public Glucometer() {
        this(false);
    }

    public Glucometer(boolean readBattery) {
        this.readBattery = readBattery;
    }

    @Override
    public boolean connectable(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return "Yuwell Glucose".equals(device.getName());
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        BluetoothGattService mBGService = gatt.getService(Service.BLOOD_GLUCOSE_MEASUREMMENT);

        mDateTimeCharacteristic = mBGService.getCharacteristic(Characteristic.DATE_TIME);
        mGMCharacteristic = mBGService.getCharacteristic(Characteristic.BLOOD_GLUCOSE);

        BluetoothGattService mBatteryService = null;
        if (readBattery) {
            mBatteryService = gatt.getService(Service.BATTERY);
        }

        if (mBatteryService != null) {
            readBatteryLevel(gatt, mBatteryService);
        } else if (mDateTimeCharacteristic != null) {
            setDateTime(gatt);
        } else {
            setCharacteristic(gatt, mGMCharacteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        final UUID characteristicUuid = characteristic.getUuid();

        //血糖
        if (Characteristic.BLOOD_GLUCOSE.equals(characteristicUuid)) {
            onBgChange(characteristic);
        }

        if (Characteristic.BATTERY_LEVEL.equals(characteristicUuid)) {
            int batteryValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            Message event = new Message();
            event.what = BleMessage.BATTERY;
            event.obj = batteryValue;
            EventBus.getDefault().post(event);

            if (mDateTimeCharacteristic != null) {
                setDateTime(gatt);
            } else {
                setCharacteristic(gatt, mGMCharacteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (Characteristic.DATE_TIME.equals(characteristic.getUuid())) {
            setCharacteristic(gatt, mGMCharacteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }
    }

    /**
     * 读取电量
     * @param gatt
     * @param mService
     */
    private void readBatteryLevel(BluetoothGatt gatt, BluetoothGattService mService) {
        BluetoothGattCharacteristic mBatteryCharacteristic = mService.getCharacteristic(Characteristic.BATTERY_LEVEL);
        setCharacteristic(gatt, mBatteryCharacteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    }

    /**
     * 设置时间
     * @param gatt
     */
    private void setDateTime(BluetoothGatt gatt) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);

        String strHexYear = "0" + Integer.toHexString(year);

        byte[] array = new byte[7];
        array[0] = (byte) Integer.parseInt(strHexYear.substring(2), 16);
        array[1] = (byte) Integer.parseInt(strHexYear.substring(0, 2), 16);
        array[2] = (byte) Utils.decimalToHex(month);
        array[3] = (byte) Utils.decimalToHex(day);
        array[4] = (byte) Utils.decimalToHex(hour);
        array[5] = (byte) Utils.decimalToHex(min);
        array[6] = (byte) Utils.decimalToHex(sec);

        mDateTimeCharacteristic.setValue(array);
        gatt.writeCharacteristic(mDateTimeCharacteristic);
    }

    private void onBgChange(BluetoothGattCharacteristic characteristic) {
        int offset = 0;
        final int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        offset += 1;

        final boolean timeOffsetPresent = (flags & 0x01) > 0;
        final boolean typeAndLocationPresent = (flags & 0x02) > 0;
        final int concentrationUnit = (flags & 0x04) > 0 ? UNIT_molpl : UNIT_kgpl;
        final boolean sensorStatusAnnunciationPresent = (flags & 0x08) > 0;
        final boolean contextInfoFollows = (flags & 0x10) > 0;

        // create and fill the new record
        int sequenceNumber = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
        offset += 2;

        final int year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset + 0);
        final int month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2);
        final int day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 3);
        final int hours = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 4);
        final int minutes = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 5);
        final int seconds = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 6);
        offset += 7;

        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hours, minutes, seconds);

        if (timeOffsetPresent) {
            // time offset is ignored in the current release
            int timeOffset = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            offset += 2;
        }

        if (typeAndLocationPresent) {
            float glucoseConcentration = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
            final int typeAndLocation = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2);
            int type = (typeAndLocation & 0xF0) >> 4; // TODO this way or around?
            int sampleLocation = (typeAndLocation & 0x0F);
            offset += 3;

            Message event = new Message();
            event.what = BleMessage.BG_DATA;
            event.obj = Utils.multiply(glucoseConcentration, 1000);
            EventBus.getDefault().post(event);
        }

        if (sensorStatusAnnunciationPresent) {
            int status = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
            offset += 2;
        }
    }
}
