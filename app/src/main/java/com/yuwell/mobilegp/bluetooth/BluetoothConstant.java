/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yuwell.mobilegp.bluetooth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class BluetoothConstant {
    public static final String DEVICE_TYPE = "device_type";
	public static Set<String> bgDeviceNameSet = new HashSet<String>();
	public static Set<String> bpDeviceNameSet = new HashSet<String>();
    static final Map<Integer, Set<String>> typeNameMap = new HashMap<Integer, Set<String>>();

    public static final int UNIT_kgpl = 0;
    public static final int UNIT_molpl = 1;
	
	static {
		bgDeviceNameSet.add("Yuwell Glucose");
		bgDeviceNameSet.add("Glucose Sensor");
		
		bpDeviceNameSet.add("BloodPressure Sen");
		bpDeviceNameSet.add("LSDSDD Me");
		bpDeviceNameSet.add("Yuwell BloodPressure");

        typeNameMap.put(0, bpDeviceNameSet);
        typeNameMap.put(1, bgDeviceNameSet);
	}
	
	public static final int DEVICE_TYPE_BLOOD_PRESSURE = 0;
	public static final int DEVICE_TYPE_BLOOD_GLUCOSE = 1;

    public static final class Service {

        public static final UUID BLOOD_GLUCOSE_MEASUREMMENT = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");

        public static final UUID BLOOD_PRESSURE_MEASUREMMENT = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");

        public static final UUID BATTERY = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    }

    public static final class Characteristic {

        public static final UUID BLOOD_GLUCOSE = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb");

        public static final UUID BLOOD_PRESSURE = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb");

        public static final UUID BATTERY_LEVEL = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

        public static final UUID ICP = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb");

        public static final UUID RACP = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb");

        public static final UUID DATE_TIME = UUID.fromString("00002A08-0000-1000-8000-00805f9b34fb");
    }

    public static final class Descriptor {
        public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public static final class OperateCode {

        public static final int REPORT_STORED_RECORDS = 1;

        public static final int DELETE_STORED_RECORDS = 2;

        public static final int ABORT_OPERATION = 3;

        public static final int REPORT_NUMBER_OF_RECORDS = 4;

        public static final int NUMBER_OF_STORED_RECORDS_RESPONSE = 5;

        public static final int RESPONSE_CODE = 6;
    }

    public static final class Operator {

        public static final int NULL = 0;

        public static final int ALL_RECORDS = 1;

        public static final int LESS_THEN_OR_EQUAL = 2;

        public static final int GREATER_THEN_OR_EQUAL = 3;

        public static final int WITHING_RANGE = 4;

        public static final int FIRST_RECORD = 5;

        public static final int LAST_RECORD = 6;
    }
}
