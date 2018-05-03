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

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    //private TextView mConnectionState;
   // private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    //private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
   // private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
   //         new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    //private BluetoothGattCharacteristic mNotifyCharacteristic;

    //private final String LIST_NAME = "NAME";
    //private final String LIST_UUID = "UUID";

    // LED Control handles
    private EditText mLedBlinkRate;
    private EditText mLedDuration;

    // Speaker Control handles
    private EditText mSpeakerPitch;
    private EditText mSpeakerVolume;

    private TextView mFlightStatTof;
    private boolean mEndOfFlight = false;

    // Graphing
    private GraphView mGraph;
    private LineGraphSeries<DataPoint> mAngVelRtSeries;
    private LineGraphSeries<DataPoint> mAngVelAvgSeries;
    private int mGraphDataPointsRt = 0;
    private int mGraphDataPointsAvg = 0;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            String extraData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            } else if (BluetoothLeService.ACTION_LED_BLINK_RATE.equals(action)) {
                mLedBlinkRate.setText(extraData);
            } else if (BluetoothLeService.ACTION_LED_DURATION.equals(action)) {
                mLedDuration.setText(extraData);
            } else if (BluetoothLeService.ACTION_SPEAKER_PITCH.equals(action)) {
                mSpeakerPitch.setText(extraData);
            } else if (BluetoothLeService.ACTION_SPEAKER_VOLUME.equals(action)) {
                mSpeakerVolume.setText(extraData);
            } else if (BluetoothLeService.ACTION_DISC_ANG_RT.equals(action)) {
                if (mEndOfFlight) {
                    mEndOfFlight = false;
                    //DataPoint[] resetData = new DataPoint[1];
                    //resetData[0] = new DataPoint(0, 0);
                    //mAngVelRtSeries.resetData(resetData);
                    //mAngVelAvgSeries.resetData(resetData);
                    //mGraphDataPointsAvg = 0;
                    //mGraphDataPointsRt = 0;
                }
                mAngVelRtSeries.appendData(new DataPoint(mGraphDataPointsRt, Integer.parseInt(extraData)), true, 40);
                mGraphDataPointsRt += 1;
            } else if (BluetoothLeService.ACTION_DISC_ANG_AVG.equals(action)) {
                mAngVelAvgSeries.appendData(new DataPoint(mGraphDataPointsAvg, Integer.parseInt(extraData)), true, 40);
                mGraphDataPointsAvg += 1;
            } else if (BluetoothLeService.ACTION_DISC_TOF.equals(action)) {
                mFlightStatTof.setText(getString(R.string.disc_stat_tof) + String.format(" %.1fs", ((float)Integer.parseInt(extraData)/2.0)));
                mEndOfFlight = true;
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    /*
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };
    */

    /*
    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }
    */

    /** OnClick Methods */


    public void ledEnableOnClick(View v) {
        mBluetoothLeService.ledEnable();
    }

    public void speakerEnableOnClick(View v) {
        mBluetoothLeService.speakerEnable();
    }


    private void writeCharacteristicFromEditText(EditText et, String s, String c) {
        try {
            byte data_byte = Byte.parseByte(et.getText().toString());
            byte[] data = new byte[1];
            data[0] = data_byte;
            mBluetoothLeService.writeCharacteristic(s, c, data);
        } catch (NumberFormatException e) {
            Log.d(TAG, e.getMessage());
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
        }
    }

    public void ledBlinkRateROnClick(View v) {
        mBluetoothLeService.readCharacteristic(SampleGattAttributes.LED_CONTROL,
                SampleGattAttributes.LED_BLINK_RATE);
    }


    public void ledBlinkRateWOnClick(View v) {
        writeCharacteristicFromEditText(mLedBlinkRate, SampleGattAttributes.LED_CONTROL,
                SampleGattAttributes.LED_BLINK_RATE);
    }


    public void ledDurationROnClick(View v) {
        mBluetoothLeService.readCharacteristic(SampleGattAttributes.LED_CONTROL,
                SampleGattAttributes.LED_DURATION);
    }

    public void ledDurationWOnClick(View v) {
        writeCharacteristicFromEditText(mLedDuration, SampleGattAttributes.LED_CONTROL,
                SampleGattAttributes.LED_DURATION);
    }

    public void speakerPitchROnClick(View v) {
        mBluetoothLeService.readCharacteristic(SampleGattAttributes.SPEAKER_CONTROL,
                SampleGattAttributes.SPEAKER_PITCH);
    }

    public void speakerPitchWOnClick(View v) {
        writeCharacteristicFromEditText(mSpeakerPitch, SampleGattAttributes.SPEAKER_CONTROL,
                SampleGattAttributes.SPEAKER_PITCH);
    }

    public void speakerVolumeROnClick(View v) {
        mBluetoothLeService.readCharacteristic(SampleGattAttributes.SPEAKER_CONTROL,
                SampleGattAttributes.SPEAKER_VOLUME);
    }

    public void speakerVolumeWOnClick(View v) {
        writeCharacteristicFromEditText(mSpeakerVolume, SampleGattAttributes.SPEAKER_CONTROL,
                SampleGattAttributes.SPEAKER_VOLUME);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.gatt_services_characteristics);
        setContentView(R.layout.disc_app);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        //mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        //mGattServicesList.setOnChildClickListener(servicesListClickListner);
        //mConnectionState = (TextView) findViewById(R.id.connection_state);
        //mDataField = (TextView) findViewById(R.id.data_value);
        mLedBlinkRate = findViewById(R.id.led_blink_rate);
        mLedDuration = findViewById(R.id.led_duration);
        mSpeakerPitch = findViewById(R.id.speaker_pitch);
        mSpeakerVolume = findViewById(R.id.speaker_volume);
        mFlightStatTof = findViewById(R.id.disc_stat_tof);

        mGraph = findViewById(R.id.graph);

        mAngVelRtSeries = new LineGraphSeries<>();
        mAngVelRtSeries.setColor(Color.RED);

        mAngVelAvgSeries = new LineGraphSeries<>();
        mAngVelAvgSeries.setColor(Color.GREEN);

        mGraph.addSeries(mAngVelRtSeries);
        mGraph.addSeries(mAngVelAvgSeries);
        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getViewport().setYAxisBoundsManual(true);
        mGraph.getViewport().setMinY(-2000);
        mGraph.getViewport().setMaxY(2000);
        mGraph.getViewport().setMinX(0);
        mGraph.getViewport().setMaxX(40);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }
    */
    /*
    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }
    */
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    /*
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }
    */

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        // EditText Update Actions
        intentFilter.addAction(BluetoothLeService.ACTION_LED_BLINK_RATE);
        intentFilter.addAction(BluetoothLeService.ACTION_LED_DURATION);
        intentFilter.addAction(BluetoothLeService.ACTION_SPEAKER_PITCH);
        intentFilter.addAction(BluetoothLeService.ACTION_SPEAKER_VOLUME);
        // Disc Status
        intentFilter.addAction(BluetoothLeService.ACTION_DISC_ANG_RT);
        intentFilter.addAction(BluetoothLeService.ACTION_DISC_ANG_AVG);
        intentFilter.addAction(BluetoothLeService.ACTION_DISC_TOF);
        return intentFilter;
    }
}
