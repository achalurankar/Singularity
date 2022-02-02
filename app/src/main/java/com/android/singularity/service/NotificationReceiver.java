package com.android.singularity.service;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
        if (task != null && task.getIsNotified() == 0) {
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
                .setSmallIcon(R.drawable.app_icon_vector)
                .setContentTitle(task.getName());
        if (task.getDescription().length() < 35) {
            if (task.getDescription().length() == 0) {
                builder.setContentText("No description provided");
            } else {
                builder.setContentText(task.getDescription());
            }
        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(task.getDescription()));
        }
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "SingularityChannel",
                IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(task.getId(), notification);
    }
}
