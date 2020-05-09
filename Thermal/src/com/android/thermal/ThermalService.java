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

import android.app.ActivityManager;
import android.app.Service;
import android.app.TaskStackListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class ThermalService extends Service {

    private static final String TAG = "ThermalService";
    private static final boolean DEBUG = false;

    private String mPreviousApp;
    private ThermalUtils mThermalUtils;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPreviousApp = "";
            mThermalUtils.setDefaultThermalProfile();
        }
    };

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        mThermalUtils = new ThermalUtils(this);
        registerReceiver();
        registerTaskStackListener();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        this.registerReceiver(mIntentReceiver, filter);
    }

    private void registerTaskStackListener() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return;

        TaskStackListener taskListener = new TaskStackListener() {
            @Override
            public void onTaskStackChanged() {
                try {
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    if (am == null) return;
                    List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                    if (tasks != null && !tasks.isEmpty()) {
                        ComponentName topActivity = tasks.get(0).topActivity;
                        if (topActivity != null) {
                            String foregroundApp = topActivity.getPackageName();
                            if (!foregroundApp.equals(mPreviousApp)) {
                                mThermalUtils.setThermalProfile(foregroundApp);
                                mPreviousApp = foregroundApp;
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore task query failures
                }
            }
        };

        try {
            am.registerTaskStackListener(taskListener);
        } catch (Exception e) {
            Log.e(TAG, "Failed to register task stack listener", e);
        }
    }
}
