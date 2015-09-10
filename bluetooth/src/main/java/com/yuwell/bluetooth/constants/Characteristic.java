package com.yuwell.bluetooth.constants;

import java.util.UUID;

/**
 * Ble characteristic uuids
 * Created by Chen on 2015/9/9.
 */
public interface Characteristic {

    UUID BLOOD_GLUCOSE = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb");

    UUID BLOOD_PRESSURE = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb");

    UUID BATTERY_LEVEL = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

    UUID ICP = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb");

    UUID RACP = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb");

    UUID DATE_TIME = UUID.fromString("00002A08-0000-1000-8000-00805f9b34fb");
}
