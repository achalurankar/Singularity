package com.android.singularity.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.singularity.tasks.Task;
import com.android.singularity.util.DbQuery;
import com.android.singularity.util.EventDispatcher;

public class DeleteHandler extends BroadcastReceiver {

    private static final String TAG = "DeleteHandler";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskId = intent.getStringExtra("taskId");
        Log.e(TAG, "onReceive: " + taskId);
        DbQuery dbQuery = new DbQuery(context);
        Task task = dbQuery.getTask(taskId);
        if (task != null) {
            // dismiss current notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(task.getId());
            // delete task
            dbQuery.deleteTask(task.getId());
            EventDispatcher.callOnDataChange();
        } else {
            Log.e(TAG, "onReceive: getTask returned null");
        }
    }
}
