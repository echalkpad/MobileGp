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

    UUID OXIMETER = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");

    UUID URINE_INDICATE = UUID.fromString("00002af0-0000-1000-8000-00805f9b34fb");

    UUID URINE_WRITE = UUID.fromString("00002af1-0000-1000-8000-00805f9b34fb");

    UUID CHOLESTEROL_READ = UUID.fromString("81eb77bd-89b8-4494-8a09-7f83d986ddc7");
}
