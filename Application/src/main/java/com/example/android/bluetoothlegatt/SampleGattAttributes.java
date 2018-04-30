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

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    // Services
    public static String LED_CONTROL =     "7ac8e949-3d1c-4e1f-8e33-29100625eb06";
    public static String SPEAKER_CONTROL = "b8059629-f548-4f5d-a923-4ee141649921";
    public static String DISC_STATS =      "59ff525b-9e02-495d-83c0-0d1bed2a1c5f";
    // Characteristics
    // LED Control
    public static String LED_ON_OFF =      "30432380-fd30-4543-b0dc-c891606e7551";
    public static String LED_BLINK_RATE =  "2cba242d-643c-489b-aad9-beea762764aa";
    public static String LED_DURATION   =  "07f15833-d39b-4eb3-a5f8-863fd642a27f";
    // Speaker Control
    public static String SPEAKER_ON_OFF =  "2e729421-a4f2-45c6-9cd1-fbb534a77844";
    public static String SPEAKER_PITCH  =  "e5f08409-545b-46e4-84c5-4861f08c0bab";
    public static String SPEAKER_VOLUME =  "32e81b8c-93e5-44ca-884f-bcfc6b1b2d79";
    // Disc Statistics
    public static String DISC_ANG_RT =     "53ef3ae4-50ce-4d56-ba7c-1bf8461fa745";
    public static String DISC_ANG_AVG =    "2d9b3c66-a3b3-4754-a0fe-a189841ef8c9";
    public static String DISC_TOF     =    "c344b260-92d9-45fa-992f-f57360dc3d70";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(LED_CONTROL, "LED Control");
        attributes.put(SPEAKER_CONTROL, "Speaker Control");
        attributes.put(DISC_STATS, "Disc Statistics");
        attributes.put(LED_ON_OFF, "LED On/Off");
        attributes.put(LED_BLINK_RATE, "LED Blink Rate");
        attributes.put(LED_DURATION, "LED Duration");
        attributes.put(SPEAKER_ON_OFF, "Speaker On/Off");
        attributes.put(SPEAKER_PITCH, "Speaker Pitch");
        attributes.put(SPEAKER_VOLUME, "Speaker Volume");
        attributes.put(DISC_ANG_RT, "Disc Angular vel. real-time");
        attributes.put(DISC_ANG_AVG, "Disc Angular vel. average");
        attributes.put(DISC_TOF, "Disc Time of Flight");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
