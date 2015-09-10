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
 * Blood Pressure Monitor behavior
 * Created by Chen on 2015/9/9.
 */
public class BPM extends BleDevice {

    private BluetoothGattCharacteristic mBPMCharacteristic;
    private BluetoothGattCharacteristic mICPCharacteristic;
    private BluetoothGattCharacteristic mDateTimeCharacteristic;

    private boolean readBattery;

    public BPM() {
        this(false);
    }

    public BPM(boolean readBattery) {
        this.readBattery = readBattery;
    }

    @Override
    public boolean connectable(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return "Yuwell BloodPressure".equals(device.getName());
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        BluetoothGattService mBPService = gatt.getService(Service.BLOOD_PRESSURE_MEASUREMMENT);

        mBPMCharacteristic = mBPService.getCharacteristic(Characteristic.BLOOD_PRESSURE);
        mICPCharacteristic = mBPService.getCharacteristic(Characteristic.ICP);
        mDateTimeCharacteristic = mBPService.getCharacteristic(Characteristic.DATE_TIME);

        BluetoothGattService mBatteryService = null;
        if (readBattery) {
            mBatteryService = gatt.getService(Service.BATTERY);
        }

        if (mBatteryService != null) {
            readBatteryLevel(gatt, mBatteryService);
        } else if (mDateTimeCharacteristic != null) {
            setDateTime(gatt);
        } else if (mICPCharacteristic != null) {
            setCharacteristic(gatt, mICPCharacteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            setCharacteristic(gatt, mBPMCharacteristic, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (Characteristic.BLOOD_PRESSURE.equals(characteristic.getUuid()) ||
                Characteristic.ICP.equals(characteristic.getUuid())) {
            onBpChange(characteristic);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (Characteristic.BATTERY_LEVEL.equals(characteristic.getUuid())) {
            final int batteryValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

            Message event = new Message();
            event.what = BleMessage.BATTERY;
            event.obj = batteryValue;
            EventBus.getDefault().post(event);

            if (mDateTimeCharacteristic != null) {
                setDateTime(gatt);
            } else if (mICPCharacteristic != null) {
                setCharacteristic(gatt, mICPCharacteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                setCharacteristic(gatt, mBPMCharacteristic, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            }
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (Characteristic.DATE_TIME.equals(characteristic.getUuid())) {
            if (mICPCharacteristic != null) {
                setCharacteristic(gatt, mICPCharacteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                setCharacteristic(gatt, mBPMCharacteristic, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            }
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        UUID characteristicUuid = descriptor.getCharacteristic().getUuid();

        if (Characteristic.ICP.equals(characteristicUuid)) {
            setCharacteristic(gatt, mBPMCharacteristic, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        }
    }

    /**
     * 读取电量
     * @param gatt
     * @param mService
     */
    private void readBatteryLevel(BluetoothGatt gatt, BluetoothGattService mService) {
        BluetoothGattCharacteristic mBatteryCharacteristic = mService.getCharacteristic(Characteristic.BATTERY_LEVEL);
        gatt.readCharacteristic(mBatteryCharacteristic);
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

    private void onBpChange(BluetoothGattCharacteristic characteristic) {
        // ICP or BPM characteristic returned value

        // first byte - flags
        int offset = 0;
        final int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset++);
        // See BPMManagerCallbacks.UNIT_* for unit options
        final int unit = flags & 0x01;
        final boolean timestampPresent = (flags & 0x02) > 0;
        final boolean pulseRatePresent = (flags & 0x04) > 0;

        float systolic = 0;
        float diastolic = 0;
        if (Characteristic.BLOOD_PRESSURE.equals(characteristic.getUuid())) {
            // following bytes - systolic, diastolic and mean arterial pressure
            systolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
            diastolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset + 2);
            final float meanArterialPressure = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset + 4);
            offset += 6;
        } else if (Characteristic.ICP.equals(characteristic.getUuid())) {
            // following bytes - cuff pressure. Diastolic and MAP are unused
            final float cuffPressure = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
            offset += 6;
            // TODO broadcast icp value
            return;
        }

        // parse timestamp if present
        if (timestampPresent) {
            final Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset));
            calendar.set(Calendar.MONTH, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2));
            calendar.set(Calendar.DAY_OF_MONTH, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 3));
            calendar.set(Calendar.HOUR_OF_DAY, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 4));
            calendar.set(Calendar.MINUTE, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 5));
            calendar.set(Calendar.SECOND, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 6));
            offset += 7;
        }

        // parse pulse rate if present
        float pulseRate = 0f;
        if (pulseRatePresent) {
            pulseRate = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
            offset += 2;
        }

        Message event = new Message();
        event.what = BleMessage.BP_DATA;
        event.obj = new int[]{(int) systolic, (int) diastolic, (int) pulseRate};
        EventBus.getDefault().post(event);
    }

}
