package com.android.singularity.service;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.android.singularity.R;
import com.android.singularity.modal.Task;
import com.android.singularity.util.DbQuery;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "com.android.singularity.service.channelId";
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskId = intent.getStringExtra("taskId");
        Log.e("AlarmReceiver", "onReceive: " + taskId);
        DbQuery dbQuery = new DbQuery(context);
        Task task = dbQuery.getTask(taskId);
        if(task != null && task.getIsNotified() == 0){
            //show notification for incomplete tasks
            task.setIsNotified(1);
            generateNotification(task, context);
            //update task to notified
            dbQuery.upsertTask(task);
        } else {
            Log.e(TAG, "onReceive: getTask returned null");
        }
    }

    public void generateNotification(Task task, Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon_vector_for_notification)
                .setContentTitle(task.getName())
                .setContentText(task.getDescription().length() == 0 ? "No Description Provided" : task.getDescription())
                .setAutoCancel(true);
        Notification notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "NotificationDemo",
                    IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notification);
    }
}
