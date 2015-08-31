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

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.yuwell.mobilegp.common.event.EventMessage;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

import de.greenrobot.event.EventBus;


/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

    public static final String INTENT_TYPE = "type";
	
	private static final int FILTER_TYPE_SEQUENCE_NUMBER = 1;
	
    // Loop scan pause interval
    private static final int SCAN_INTERVAL = 10 * 1000;
	
	private final IBinder mBinder = new LocalBinder();

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
	private int mDeviceType;
	// Whether it is scanning ble
    private boolean isScanning;
	// Whether it has started scanning
	private boolean isScanStarted = false;
    private boolean isNetworkActive = false;
    private boolean isUserDisconnected = false;

    private int firstDeviceType;
    private int secondDeviceType;

	private BluetoothGattCharacteristic mBPMCharacteristic;
	private BluetoothGattCharacteristic mICPCharacteristic;
	private BluetoothGattCharacteristic mGMCharacteristic;
	private BluetoothGattCharacteristic mDateTimeCharacteristic;
	private BluetoothGattCharacteristic mRACPCharacteristic;

    private EventBus mEventBus;

	// Implements callback methods for GATT events that the app cares about. For
	// example,
	// connection change and services discovered.
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            mConnectionState = newState;
            postConnectionState();

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
			}
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.i(TAG, "Disconnected from GATT server.");

                if (isUserDisconnected) {
                    closeGatt();
                    isUserDisconnected = false;
                }

                scanDevice();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.i(TAG, "onServicesDiscovered from GATT server." + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (BluetoothConstant.DEVICE_TYPE_BLOOD_GLUCOSE == mDeviceType) {
					BluetoothGattService mBGService = gatt.getService(BluetoothConstant.Service.BLOOD_GLUCOSE_MEASUREMMENT);

					mDateTimeCharacteristic = mBGService.getCharacteristic(
                            BluetoothConstant.Characteristic.DATE_TIME);
					mGMCharacteristic = mBGService.getCharacteristic(
							BluetoothConstant.Characteristic.BLOOD_GLUCOSE);
					mRACPCharacteristic = mBGService.getCharacteristic(
							BluetoothConstant.Characteristic.RACP);

//					BluetoothGattService mBatteryService = gatt.getService(BluetoothConstant.BATTERY_SERVICE);
					BluetoothGattService mBatteryService = null;
					if (mBatteryService != null) {
						readBatteryLevel(gatt, mBatteryService);
					} else if (mDateTimeCharacteristic != null) {
						setDateTime(gatt);
					} else {
						setCharacteristic(mGMCharacteristic, true, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
					}
				}

				if (BluetoothConstant.DEVICE_TYPE_BLOOD_PRESSURE == mDeviceType) {
					BluetoothGattService mBPService = gatt.getService(BluetoothConstant.Service.BLOOD_PRESSURE_MEASUREMMENT);

					mBPMCharacteristic = mBPService.getCharacteristic(
							BluetoothConstant.Characteristic.BLOOD_PRESSURE);
//					mICPCharacteristic = mBPService.getCharacteristic(
//							BluetoothConstant.Characteristic.ICP);
					mDateTimeCharacteristic = mBPService.getCharacteristic(
							BluetoothConstant.Characteristic.DATE_TIME);

//					BluetoothGattService mBatteryService = gatt.getService(BluetoothConstant.Service.BATTERY);
					BluetoothGattService mBatteryService = null;
					if (mBatteryService != null) {
						readBatteryLevel(gatt, mBatteryService);
					} else if (mDateTimeCharacteristic != null) {
						setDateTime(gatt);
					} else if (mICPCharacteristic != null) {
						setCharacteristic(mICPCharacteristic, true, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
					} else {
						setCharacteristic(mBPMCharacteristic, true, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
					}
				}
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			final UUID characteristicUuid = characteristic.getUuid();

            //血糖
            if (BluetoothConstant.Characteristic.BLOOD_GLUCOSE.equals(characteristicUuid)) {
                onBgChange(characteristic);
            }

            // 血压
            if (BluetoothConstant.Characteristic.BLOOD_PRESSURE.equals(characteristicUuid) ||
                    BluetoothConstant.Characteristic.ICP.equals(characteristicUuid)) {
                onBpChange(characteristic);
            }
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (BluetoothConstant.Characteristic.BATTERY_LEVEL.equals(characteristic.getUuid())) {
					final int batteryValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

					if (BluetoothConstant.DEVICE_TYPE_BLOOD_PRESSURE == mDeviceType) {
						if (mDateTimeCharacteristic != null) {
							setDateTime(gatt);
						} else if (mICPCharacteristic != null) {
							setCharacteristic(mICPCharacteristic, true, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						} else {
							setCharacteristic(mBPMCharacteristic, true, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
						}
					}
				}
			}
		}

        @Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (BluetoothConstant.Characteristic.DATE_TIME.equals(characteristic.getUuid())) {
					if (BluetoothConstant.DEVICE_TYPE_BLOOD_GLUCOSE == mDeviceType) {
						setCharacteristic(mGMCharacteristic, true, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
					}

					if (BluetoothConstant.DEVICE_TYPE_BLOOD_PRESSURE == mDeviceType) {
						if (mICPCharacteristic != null) {
							setCharacteristic(mICPCharacteristic, true, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						} else {
							setCharacteristic(mBPMCharacteristic, true, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
						}
					}
				}
			}
		}

        @Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				UUID characteristicUuid = descriptor.getCharacteristic().getUuid();

				if (BluetoothConstant.Characteristic.ICP.equals(characteristicUuid)) {
					setCharacteristic(mBPMCharacteristic, true, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
				}
			}
		}
	};

    /**
     * 读取电量
     * @param gatt
     * @param mService
     */
	private void readBatteryLevel(BluetoothGatt gatt, BluetoothGattService mService) {
		BluetoothGattCharacteristic mBatteryCharacteristic = mService.getCharacteristic(
				BluetoothConstant.Characteristic.BATTERY_LEVEL);
		
		if (BluetoothConstant.DEVICE_TYPE_BLOOD_PRESSURE == mDeviceType) {
			gatt.readCharacteristic(mBatteryCharacteristic);
		} else {
			setCharacteristic(mBatteryCharacteristic, true, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		}
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
		array[2] = (byte) decimalToHex(month);
		array[3] = (byte) decimalToHex(day);
		array[4] = (byte) decimalToHex(hour);
		array[5] = (byte) decimalToHex(min);
		array[6] = (byte) decimalToHex(sec);
		
		mDateTimeCharacteristic.setValue(array);
		gatt.writeCharacteristic(mDateTimeCharacteristic);
	}

    private void onBgChange(BluetoothGattCharacteristic characteristic) {
        int offset = 0;
        final int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        offset += 1;

        final boolean timeOffsetPresent = (flags & 0x01) > 0;
        final boolean typeAndLocationPresent = (flags & 0x02) > 0;
        final int concentrationUnit = (flags & 0x04) > 0 ? BluetoothConstant.UNIT_molpl : BluetoothConstant.UNIT_kgpl;
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
			event.what = EventMessage.BLE_DATA;
			event.arg1 = mDeviceType;
            event.obj = multiply(glucoseConcentration, 1000);
            mEventBus.post(event);
        }

        if (sensorStatusAnnunciationPresent) {
            int status = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
            offset += 2;
        }
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
        if (BluetoothConstant.Characteristic.BLOOD_PRESSURE.equals(characteristic.getUuid())) {
            // following bytes - systolic, diastolic and mean arterial pressure
            systolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
            diastolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset + 2);
            final float meanArterialPressure = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset + 4);
            offset += 6;
        } else if (BluetoothConstant.Characteristic.ICP.equals(characteristic.getUuid())) {
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
		event.what = EventMessage.BLE_DATA;
		event.arg1 = mDeviceType;
        event.obj = new int[]{(int) systolic, (int) diastolic, (int) pulseRate};
        mEventBus.post(event);
    }
	
	/**
	 * Writes given operation parameters to the characteristic
	 * 
	 * @param characteristic
	 *            the characteristic to write. This must be the Record Access Control Point characteristic
	 * @param opCode
	 *            the operation code
	 * @param operator
	 *            the operator
	 * @param params
	 *            optional parameters (one for >=, <=, two for the range, none for other operators)
	 */
	private void setOpCode(final BluetoothGattCharacteristic characteristic, final int opCode, final int operator, final Integer... params) {
		// 1 byte for opCode, 1 for operator, 1 for filter type (if parameters exists) and 2 for each parameter
		final int size = 2 + ((params.length > 0) ? 1 : 0) + params.length * 2; 
		characteristic.setValue(new byte[size]);

		// write the operation code
		int offset = 0;
		characteristic.setValue(opCode, BluetoothGattCharacteristic.FORMAT_UINT8, offset);
		offset += 1;

		// write the operator. This is always present but may be equal to OPERATOR_NULL
		characteristic.setValue(operator, BluetoothGattCharacteristic.FORMAT_UINT8, offset);
		offset += 1;

		// if parameters exists, append them. Parameters should be sorted from minimum to maximum. 
		// Currently only one or two params are allowed
		if (params.length > 0) {
			// our implementation use only sequence number as a filer type
			characteristic.setValue(FILTER_TYPE_SEQUENCE_NUMBER, BluetoothGattCharacteristic.FORMAT_UINT8, offset);
			offset += 1;

			for (final Integer i : params) {
				characteristic.setValue(i, BluetoothGattCharacteristic.FORMAT_UINT16, offset);
				offset += 2;
			}
		}
	}

	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

    @Override
    public void onCreate() {
        super.onCreate();
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);

        boolean bpFirst = true;
        firstDeviceType = bpFirst ? BluetoothConstant.DEVICE_TYPE_BLOOD_PRESSURE :
                BluetoothConstant.DEVICE_TYPE_BLOOD_GLUCOSE;
        secondDeviceType = bpFirst ? BluetoothConstant.DEVICE_TYPE_BLOOD_GLUCOSE :
                BluetoothConstant.DEVICE_TYPE_BLOOD_PRESSURE;

        initialize();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        enableBluetoothAdapter();
        return START_STICKY;
    }

    @Override
	public IBinder onBind(Intent intent) {
        mDeviceType = intent.getIntExtra(INTENT_TYPE, firstDeviceType);
        return mBinder;
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
        stopLoopScan();
        close();
    }

    /**
     * Try to build connection with a ble device
     */
    public void connect() {
        enableBluetoothAdapter();

        if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
//            if (TextUtils.isEmpty(mBluetoothDeviceAddress)) {
//                // 从未建立过连接,搜索设备
//                scanDevice();
//            } else {
//                // 尝试重新连接之前设备
//                boolean isConnectionInitiated = connect(mBluetoothDeviceAddress, BluetoothConstant.DEVICE_TYPE_BLOOD_GLUCOSE);
//                if (isConnectionInitiated) {
//                    postConnectionState();
//                } else {
//                    // if reconnect failed, scan devices
//                    scanDevice();
//                }
//            }
            scanDevice();
        }
    }

    /**
     * When activity is pausing, stop scan device,
     * but still keep the connection with current device
     */
    public void pause() {
        stopLoopScan();
    }

    /**
     * Start to scan ble devices after 500ms
     */
    public void scanDevice() {
        if (!isScanStarted && mConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanLeDevice(true, true);
                }
            }, 500);
			isScanStarted = true;
        }
    }

    /**
     * Change current connect device type
     * @param mDeviceType
     */
    public void resetDeviceType(int mDeviceType) {
        if (this.mDeviceType == mDeviceType && mConnectionState == BluetoothProfile.STATE_CONNECTED) {
            return;
        } else {
            this.mDeviceType = mDeviceType;
            if (mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                isUserDisconnected = true;
                closeGatt();
            }
            scanDevice();
        }
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    /**
     * Disconnect current connection if type is secondary device
     */
    public void disconnectSecondaryDevice() {
        if (mDeviceType == secondDeviceType &&
                mConnectionState == BluetoothProfile.STATE_CONNECTED) {
            closeGatt();
        }
        mDeviceType = firstDeviceType;
    }

    /**
     * Disconnect & close bluetooth gatt
     */
    private void closeGatt() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    }

    private void stopLoopScan() {
        if (mConnectionState != BluetoothProfile.STATE_CONNECTED) {
            if (isScanning) {
                scanLeDevice(false, false);
            } else {
                mHandler.removeCallbacksAndMessages(null);
				isScanStarted = false;
            }
        }
    }

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	private boolean connect(final String address, final int type) {
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		if (mDeviceType == type && !TextUtils.isEmpty(mBluetoothDeviceAddress) &&
                address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
			Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				mConnectionState = BluetoothProfile.STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		Log.d(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		mDeviceType = type;
		mConnectionState = BluetoothProfile.STATE_CONNECTING;
		return true;
	}

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    private boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	private void close() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
        mBluetoothAdapter = null;
	}

    private void enableBluetoothAdapter() {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    private void postConnectionState() {
        Message event = new Message();
		event.what = EventMessage.STATE_CHANGE;
		event.arg1 = mConnectionState;
        mEventBus.post(event);
    }

    public synchronized int getConnectionState() {
        return mConnectionState;
    }

	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
    private void setCharacteristic(BluetoothGattCharacteristic characteristic, boolean enabled, byte[] value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BluetoothConstant.Descriptor.CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(value);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Set<String> deviceNameSet = BluetoothConstant.typeNameMap.get(mDeviceType);
            if (deviceNameSet.contains(device.getName())) {
				boolean isConnectionInitiated = connect(device.getAddress(), mDeviceType);
				if (isConnectionInitiated) {
					scanLeDevice(false, false);
					postConnectionState();
				}
            }
        }
    };

    /**
     * 搜索BLE设备
     * @param enable Turn on / off
     * @param loopScan Scan periodically
     */
    private void scanLeDevice(final boolean enable, boolean loopScan) {
        if (mBluetoothAdapter != null) {
            if (enable) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                isScanning = true;

                if (loopScan) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scanLeDevice(false, true);
                        }
                    }, SCAN_INTERVAL);
                }
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                isScanning = false;

                if (loopScan && !isNetworkActive) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scanLeDevice(true, true);
                        }
                    }, SCAN_INTERVAL);
                } else {
                    mHandler.removeCallbacksAndMessages(null);
					isScanStarted = false;
                }
            }
        }
    }

    private final Handler mHandler = new Handler();

	public void getAllRecords() {
		if (mBluetoothGatt == null || mRACPCharacteristic == null)
			return;

		setOpCode(mRACPCharacteristic, BluetoothConstant.OperateCode.REPORT_NUMBER_OF_RECORDS,
                BluetoothConstant.Operator.ALL_RECORDS);
		mBluetoothGatt.writeCharacteristic(mRACPCharacteristic);
	}
	
	private static int decimalToHex(int n) {
		return Integer.parseInt(Integer.toHexString(n), 16);
	}

	private static String multiply(float v1, float v2) {
		BigDecimal b1 = new BigDecimal(Float.toString(v1));
		BigDecimal b2 = new BigDecimal(Float.toString(v2));
		return retainOneDecimal(b1.multiply(b2).toString());
	}

	private static String retainOneDecimal(String val) {
		if (val.endsWith(".")) {
			val = val.substring(0, val.length() - 1);
		}
		String[] strArr = val.split("\\.");
		if (strArr.length == 2 && strArr[1].length() > 1) {
			val = strArr[0] + "." + strArr[1].substring(0, 1);
		}
		return val;
	}

}
