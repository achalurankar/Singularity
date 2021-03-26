package com.android.singularity.controller;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class AppController extends Application implements LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        isAppInBackground(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        isAppInBackground(true);
    }

    public interface ValueChangeListener {
        void onChanged(Boolean value);
    }

    private ValueChangeListener visibilityChangeListener;

    public void setOnVisibilityChangeListener(ValueChangeListener listener) {
        this.visibilityChangeListener = listener;
    }

    private void isAppInBackground(Boolean isBackground) {
        if (null != visibilityChangeListener) {
            visibilityChangeListener.onChanged(isBackground);
        }
    }

    private static AppController mInstance;

    public static AppController getInstance() {
        return mInstance;
    }

    public static final String FOREGROUND_NOTIFICATION_CHANNEL_ID = "notification_channel_id";
    public static final String TASK_NOTIFICATION_CHANNEL_ID = "task_notification_channel_id";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        // addObserver
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel foregroundChannel = new NotificationChannel(FOREGROUND_NOTIFICATION_CHANNEL_ID, "Event Horizon", NotificationManager.IMPORTANCE_NONE);
            NotificationChannel taskNotificationChannel = new NotificationChannel(TASK_NOTIFICATION_CHANNEL_ID, "Medium", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(foregroundChannel);
            manager.createNotificationChannel(taskNotificationChannel);
        }
    }
}
