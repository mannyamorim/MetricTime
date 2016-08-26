package com.mathi_amorim.emmanuel.metrictime;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MetricTimeWidgetProvider extends AppWidgetProvider {
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        context.startService(new Intent(context, UpdateTimeService.class));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        context.stopService(new Intent(context, UpdateTimeService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        context.startService(new Intent(context, UpdateTimeService.class));
    }

    public static final class UpdateTimeService extends Service {
        static final String UPDATE_TIME = "com.mathi_amorim.emmanuel.metrictime.UPDATE_TIME";

        private Calendar mCalendar;
        private final static IntentFilter mIntentFilter = new IntentFilter();

        static {
            mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
            mIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
            mIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        }

        @Override
        public void onCreate() {
            super.onCreate();

            mCalendar = Calendar.getInstance();
            registerReceiver(mTimeChangedReceiver, mIntentFilter);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            unregisterReceiver(mTimeChangedReceiver);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, flags, startId);

            updateTime();

            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTime();
            }
        };

        private void updateTime() {
            mCalendar.setTimeInMillis(System.currentTimeMillis());

            DateFormat df = new SimpleDateFormat("hh:mm");
            String currentTime = df.format(new Date());

            RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.metric_time_widget);
            mRemoteViews.setTextViewText(R.id.widget1label, currentTime);

            ComponentName mComponentName = new ComponentName(this, MetricTimeWidgetProvider.class);
            AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(this);
            mAppWidgetManager.updateAppWidget(mComponentName, mRemoteViews);
        }
    }
}
