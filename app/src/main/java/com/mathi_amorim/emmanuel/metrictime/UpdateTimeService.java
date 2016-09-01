/*
        MIT License

        Copyright (c) 2016 Emmanuel Mathi-Amorim

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
*/

package com.mathi_amorim.emmanuel.metrictime;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.widget.RemoteViews;

import java.util.Calendar;

public final class UpdateTimeService extends Service {
    private static Calendar mCalendar;
    private final static IntentFilter mTimeIntentFilter = new IntentFilter();
    private final static IntentFilter mScreenIntentFilter = new IntentFilter();

    static {
        mTimeIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        mTimeIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        mTimeIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        mScreenIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        mScreenIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mCalendar = Calendar.getInstance();

        if(isScreenOn()) {
            registerReceiver(mTimeChangedReceiver, mTimeIntentFilter);
        }
        registerReceiver(mScreenChangedReceiver, mScreenIntentFilter);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Config.showNotification = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SHOW_NOTIFICATION, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mTimeChangedReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        updateWidget();
        updateNotification();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean isScreenOn() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }

    private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Config.LOG_TAG, "Time Broadcast Received");

            if(isScreenOn()) {
                updateWidget();
                updateNotification();
            }
        }
    };

    private final BroadcastReceiver mScreenChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                Log.d(Config.LOG_TAG, "Screen Broadcast On");

                updateWidget();
                updateNotification();

                registerReceiver(mTimeChangedReceiver, mTimeIntentFilter);
            }
            else if(intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                Log.d(Config.LOG_TAG, "Screen Broadcast Off");

                unregisterReceiver(mTimeChangedReceiver);
            }
        }
    };

    private void updateWidget() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        MetricTime time = MetricTimeConverter.currentMetricTime();
        String currentTime = String.format("%1$01d:%2$02d", time.hours, time.minutes);

        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.metric_time_widget);
        mRemoteViews.setTextViewText(R.id.widget1label, currentTime);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.widget1label, pendingIntent);

        ComponentName mComponentName = new ComponentName(this, MetricTimeWidgetProvider.class);
        AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetManager.updateAppWidget(mComponentName, mRemoteViews);

        Log.d(Config.LOG_TAG, "Widget Updated");
    }

    private void updateNotification() {
        if(Config.showNotification) {
            MetricTime time = MetricTimeConverter.currentMetricTime();
            String currentTime = String.format("%1$01d:%2$02d", time.hours, time.minutes);

            Intent intent = new Intent(this, SettingsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle("Metric Time");
            mBuilder.setContentText(currentTime);
            mBuilder.setOngoing(true);
            mBuilder.setContentIntent(pendingIntent);

            int mNotificationId = 1;
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(mNotificationId, mBuilder.build());

            Log.d(Config.LOG_TAG, "Notification Updated");
        }
        else {
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.cancelAll();
        }
    }
}