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

package com.example.android.bluetoothlegatt;

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
import android.os.IBinder;
import android.util.Log;

import java.net.Inet4Address;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SINT8;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private int descriptorCounter = 0;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_LED_BLINK_RATE =
            "com.example.bluetooth.le.ACTION_LED_BLINK_RATE";
    public final static String ACTION_LED_DURATION =
            "com.example.bluetooth.le.ACTION_LED_DURATION";
    public final static String ACTION_SPEAKER_PITCH =
            "com.example.bluetooth.le.ACTION_SPEAKER_PITCH";
    public final static String ACTION_SPEAKER_VOLUME =
            "com.example.bluetooth.le.ACTION_SPEAKER_VOLUME";
    public final static String ACTION_DISC_ANG_RT =
            "com.example.bluetooth.le.ACTION_DISC_ANG_RT";
    public final static String ACTION_DISC_ANG_AVG =
            "com.example.bluetooth.le.ACTION_DISC_ANG_AVG";
    public final static String ACTION_DISC_TOF =
            "com.example.bluetooth.le.ACTION_DISC_TOF";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    private BluetoothGattCharacteristic strToChar(String s, String c) {
        return mBluetoothGatt.getService(UUID.fromString(s)).getCharacteristic(UUID.fromString(c));
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        private void enableNotifications(BluetoothGatt gatt, BluetoothGattCharacteristic c) {
            BluetoothGattDescriptor d;
            // Enable Local Notifications
            gatt.setCharacteristicNotification(c, true);
            // Enable Remote Notifications
            d = c.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(d);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
            // Enable Notifications for Disc Stats
            BluetoothGattCharacteristic c;

            descriptorCounter += 1;
            c = gatt.getService(UUID.fromString(SampleGattAttributes.DISC_STATS)).getCharacteristic(
                    UUID.fromString(SampleGattAttributes.DISC_TOF));
            enableNotifications(gatt, c);

            // test code:  read RSSI
            gatt.readRemoteRssi();
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.w(TAG, "RSSI: " + rssi);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic == strToChar(SampleGattAttributes.LED_CONTROL, SampleGattAttributes.LED_BLINK_RATE)) {
                    broadcastUpdate(ACTION_LED_BLINK_RATE, characteristic);
                } else if (characteristic == strToChar(SampleGattAttributes.LED_CONTROL, SampleGattAttributes.LED_DURATION)) {
                    broadcastUpdate(ACTION_LED_DURATION, characteristic);
                } else if (characteristic == strToChar(SampleGattAttributes.SPEAKER_CONTROL, SampleGattAttributes.SPEAKER_PITCH)) {
                    broadcastUpdate(ACTION_SPEAKER_PITCH, characteristic);
                } else if (characteristic == strToChar(SampleGattAttributes.SPEAKER_CONTROL, SampleGattAttributes.SPEAKER_VOLUME)) {
                    broadcastUpdate(ACTION_SPEAKER_VOLUME, characteristic);
                } else {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if (characteristic == strToChar(SampleGattAttributes.DISC_STATS, SampleGattAttributes.DISC_ANG_RT)) {
                broadcastUpdate(ACTION_DISC_ANG_RT, characteristic);
            } else if (characteristic == strToChar(SampleGattAttributes.DISC_STATS, SampleGattAttributes.DISC_ANG_AVG)){
                broadcastUpdate(ACTION_DISC_ANG_AVG, characteristic);
            } else if (characteristic == strToChar(SampleGattAttributes.DISC_STATS, SampleGattAttributes.DISC_TOF)) {
                broadcastUpdate(ACTION_DISC_TOF, characteristic);
            }

            else {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            BluetoothGattCharacteristic c;

            if (descriptorCounter == 1) {
                c = gatt.getService(UUID.fromString(SampleGattAttributes.DISC_STATS)).getCharacteristic(
                        UUID.fromString(SampleGattAttributes.DISC_ANG_AVG));
                enableNotifications(gatt, c);
            } else if (descriptorCounter == 2) {
                c = gatt.getService(UUID.fromString(SampleGattAttributes.DISC_STATS)).getCharacteristic(
                        UUID.fromString(SampleGattAttributes.DISC_ANG_RT));
                enableNotifications(gatt, c);
            }
            descriptorCounter += 1;

        }
    };

    public void ledEnable() {
        BluetoothGattCharacteristic c;
        c = mBluetoothGatt.getService(UUID.fromString(SampleGattAttributes.LED_CONTROL)).getCharacteristic(
                UUID.fromString(SampleGattAttributes.LED_ON_OFF));
        byte[] on_off = new byte[1];
        on_off[0] = 1;
        c.setValue(on_off);
        mBluetoothGatt.writeCharacteristic(c);
    }

    public void speakerEnable() {
        BluetoothGattCharacteristic c;
        c = mBluetoothGatt.getService(UUID.fromString(SampleGattAttributes.SPEAKER_CONTROL)).getCharacteristic(
                UUID.fromString(SampleGattAttributes.SPEAKER_ON_OFF));
        byte[] on_off = new byte[1];
        on_off[0] = 1;
        c.setValue(on_off);
        mBluetoothGatt.writeCharacteristic(c);
    }

    /** Source:  http://processors.wiki.ti.com/ as referenced in IoT Projects with BLE book */
    private Integer shortSignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(FORMAT_SINT8, offset + 1);
        return (upperByte << 8) + lowerByte;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        if ((characteristic == strToChar(SampleGattAttributes.DISC_STATS, SampleGattAttributes.DISC_ANG_RT)) ||
                (characteristic == strToChar(SampleGattAttributes.DISC_STATS, SampleGattAttributes.DISC_ANG_AVG))) {
            Integer data = shortSignedAtOffset(characteristic, 4);
            intent.putExtra(EXTRA_DATA, String.format("%d", data));
        }
        else {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%d", byteChar));
                intent.putExtra(EXTRA_DATA, stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
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
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
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
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(String service, String characteristic) {
        BluetoothGattCharacteristic c;
        c = mBluetoothGatt.getService(UUID.fromString(service)).getCharacteristic(
                UUID.fromString(characteristic));
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(c);
    }

    public void writeCharacteristic(String service, String characteristic, byte[] data) {
        BluetoothGattCharacteristic c;
        c = mBluetoothGatt.getService(UUID.fromString(service)).getCharacteristic(
                UUID.fromString(characteristic));

        c.setValue(data);
        mBluetoothGatt.writeCharacteristic(c);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
