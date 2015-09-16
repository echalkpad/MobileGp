package com.yuwell.bluetooth.constants;

import java.util.UUID;

/**
 * Ble service uuids
 * Created by chenshuai on 2015/9/9.
 */
public interface Service {

    UUID BLOOD_GLUCOSE_MEASUREMMENT = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");

    UUID BLOOD_PRESSURE_MEASUREMMENT = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");

    UUID BATTERY = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");

    UUID CHOLESTEROL = UUID.fromString("C14D2C0A-401F-B7A9-841F-E2E93B80F631");

    UUID OXIMETER = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    UUID URINE_MEASUREMENT = UUID.fromString("000018f0-0000-1000-8000-00805f9b34fb");
}
