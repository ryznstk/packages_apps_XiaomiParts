/*
 * Copyright (C) 2020 The LineageOS Project
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

package com.android.thermal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;

import androidx.preference.PreferenceManager;

import com.android.thermal.utils.FileUtils;

public final class ThermalUtils {

    // Profile indices (used in spinner position / SharedPreferences storage)
    protected static final int STATE_DEFAULT = 0;
    protected static final int STATE_GAMING_HEAVY = 1;
    protected static final int STATE_GAMING_MEDIUM = 2;
    protected static final int STATE_GAMING_LIGHT = 3;
    protected static final int STATE_BENCHMARK = 4;
    protected static final int STATE_CAMERA = 5;
    protected static final int STATE_CAMERA_4K = 6;
    protected static final int STATE_STREAMING = 7;
    protected static final int STATE_VIDEO_CHAT = 8;
    protected static final int STATE_NAVIGATION = 9;
    protected static final int STATE_PHONE = 10;
    protected static final int STATE_DIALER = 11;

    protected static final int STATE_COUNT = 12;

    private static final String SCONFIG_DEFAULT          = "0";   // thermal-normal
    private static final String SCONFIG_GAMING_HEAVY     = "18";  // thermal-mgame
    private static final String SCONFIG_GAMING_MEDIUM    = "19";  // thermal-tgame
    private static final String SCONFIG_GAMING_LIGHT     = "1";   // thermal-huanji
    private static final String SCONFIG_BENCHMARK        = "10";  // thermal-nolimits
    private static final String SCONFIG_CAMERA           = "14";  // thermal-camera
    private static final String SCONFIG_CAMERA_4K        = "15";  // thermal-4k
    private static final String SCONFIG_STREAMING        = "11";  // thermal-video
    private static final String SCONFIG_VIDEO_CHAT       = "16";  // thermal-videochat
    private static final String SCONFIG_NAVIGATION       = "9";   // thermal-navigation
    private static final String SCONFIG_PHONE            = "5";   // thermal-phone
    private static final String SCONFIG_DIALER           = "8";   // thermal-youtube

    private static final String[] SCONFIG_VALUES = {
        SCONFIG_DEFAULT, SCONFIG_GAMING_HEAVY, SCONFIG_GAMING_MEDIUM, SCONFIG_GAMING_LIGHT,
        SCONFIG_BENCHMARK, SCONFIG_CAMERA, SCONFIG_CAMERA_4K, SCONFIG_STREAMING,
        SCONFIG_VIDEO_CHAT, SCONFIG_NAVIGATION, SCONFIG_PHONE, SCONFIG_DIALER
    };

    private static final String THERMAL_CONTROL = "thermal_control";

    // Profile prefixes for SharedPreferences storage
    private static final String[] THERMAL_PREFIXES = {
        /* not used for default */ "",
        "thermal.gaming_heavy=",
        "thermal.gaming_medium=",
        "thermal.gaming_light=",
        "thermal.benchmark=",
        "thermal.camera=",
        "thermal.camera_4k=",
        "thermal.streaming=",
        "thermal.video_chat=",
        "thermal.navigation=",
        "thermal.phone=",
        "thermal.dialer="
    };

    private static final String THERMAL_SCONFIG = "/sys/devices/virtual/thermal/thermal_message/sconfig";

    private SharedPreferences mSharedPrefs;

    protected ThermalUtils(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void startService(Context context) {
        if (FileUtils.fileExists(THERMAL_SCONFIG)) {
            context.startServiceAsUser(new Intent(context, ThermalService.class),
                    UserHandle.CURRENT);
        }
    }

    private void writeValue(String profiles) {
        mSharedPrefs.edit().putString(THERMAL_CONTROL, profiles).apply();
    }

    private String getValue() {
        String value = mSharedPrefs.getString(THERMAL_CONTROL, null);

        if (value != null) {
            String[] modes = value.split(":");
            if (modes.length < STATE_COUNT - 1) value = null;
        }

        if (value == null || value.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < THERMAL_PREFIXES.length; i++) {
                if (i > 1) sb.append(":");
                sb.append(THERMAL_PREFIXES[i]);
            }
            value = sb.toString();
            writeValue(value);
        }
        return value;
    }

    protected void writePackage(String packageName, int mode) {
        String value = getValue();
        value = value.replace(packageName + ",", "");
        String[] modes = value.split(":");

        if (mode >= 1 && mode < STATE_COUNT) {
            modes[mode - 1] = modes[mode - 1] + packageName + ",";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modes.length; i++) {
            if (i > 0) sb.append(":");
            sb.append(modes[i]);
        }

        writeValue(sb.toString());
    }

    protected int getStateForPackage(String packageName) {
        String value = getValue();
        String[] modes = value.split(":");

        for (int i = 0; i < modes.length; i++) {
            if (modes[i].contains(packageName + ",")) {
                return i + 1;
            }
        }
        return STATE_DEFAULT;
    }

    protected void setDefaultThermalProfile() {
        FileUtils.writeLine(THERMAL_SCONFIG, SCONFIG_DEFAULT);
    }

    protected void setThermalProfile(String packageName) {
        int state = getStateForPackage(packageName);
        String sconfig = SCONFIG_VALUES[state];
        FileUtils.writeLine(THERMAL_SCONFIG, sconfig);
    }
}
