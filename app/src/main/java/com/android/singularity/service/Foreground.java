package com.android.singularity.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.android.singularity.R;
import com.android.singularity.activity.TaskList;
import com.android.singularity.controller.AppController;
import com.android.singularity.modal.Task;
import com.android.singularity.util.DateTime;
import com.android.singularity.util.DbQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Foreground extends Service {

    Timer timer;
    TimerTask timerTask;
    String TAG = "NotificationService";
    int intoMills = 1000;
    int intoSecs = 60;
    int intervalInMinutes = 5;
    DbQuery dbQuery;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Intent appIntent = new Intent(this, TaskList.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, AppController.FOREGROUND_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Checking tasks...")
                .setSmallIcon(R.drawable.app_icon_vector)
                .setPriority(Notification.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .build();
        dbQuery = new DbQuery(this);
        dbQuery.createOrOpenDatabase();
        dbQuery.createTasksTableIfNotExists();
        startForeground(1, notification);
        startTimer();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimerTask();
    }

    public void startTimer() {
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //config timer
        timer.schedule(timerTask, 0, intervalInMinutes * intoSecs * intoMills);
    }

    //stop time code
    public void stopTimerTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    List<Task> mList = new ArrayList<>();

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                mList.clear();
                DateTime initialDT = new DateTime();
                String currentDateTime = DateTime.getDateTimeValue(initialDT.getDateForUser(), initialDT.getTimeForUser());
                DateTime finalDT = new DateTime();
                finalDT.setMinute(finalDT.getMinute() + 30);
                if(finalDT.getMinute() > 60) {
                    finalDT.setHour(finalDT.getHour() + 1);
                    finalDT.setMinute(finalDT.getMinute() - 60);
                    if(finalDT.getHour() > 24)
                        finalDT.setHour(finalDT.getHour() - 24);
                }
                String finalDateTime = DateTime.getDateTimeValue(finalDT.getDateForUser(), finalDT.getTimeForUser());
                String[] args = new String[]{currentDateTime, finalDateTime};
                Log.e(TAG, "Between " + currentDateTime + " AND "  + finalDateTime);
                Cursor cursor = dbQuery.mRef.rawQuery("SELECT * from tasks WHERE is_notified = 0 AND is_completed = 0 AND date_time BETWEEN ? AND ?", args);
                cursor.moveToFirst();
                if (cursor.getCount() != 0) {
                    String id, name, desc, date, time;
                    int isNotified, isCompleted;
                    for (int i = 0; i < cursor.getCount(); i++) {
                        id = cursor.getString(0);
                        name = cursor.getString(1);
                        date = cursor.getString(2);
                        time = cursor.getString(3);
                        desc = cursor.getString(4);
                        isNotified = cursor.getInt(5);
                        isCompleted = cursor.getInt(6);
                        Task task = new Task(id, name, date, time, desc, isNotified, isCompleted);
                        mList.add(task);
                        cursor.moveToNext();
                    }
                }
                cursor.close();
                generateNotification();
            }
        };
    }

    private String GROUP_ID = "com.android.singularity.service.group_id";

    private void generateNotification() {
        if(mList.size() == 0)
            return;
        System.out.println("tasks to be notified count : " + mList.size());
        if(getApplicationContext() != null) {
            NotificationManager manager;
            int id = 2;
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, TaskList.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            for(Task task : mList){
                NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), AppController.TASK_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.app_icon_vector)
                        .setContentTitle(task.getName())
                        .setContentIntent(pendingIntent)
//                        .setGroup(GROUP_ID)
                        .setContentText(task.getDescription().length() == 0 ? "Description not provided" : task.getDescription())
                        .setAutoCancel(true);
                manager.notify(id++, notification.build());
                updateNotifiedTasks(task);
            }
//            Notification summaryNotification =
//                    new NotificationCompat.Builder(getApplicationContext(), AppController.TASK_NOTIFICATION_CHANNEL_ID)
//                            .setContentTitle("Content Title")
//                            //set content text to support devices running API level < 24
//                            .setContentText("Content Text")
//                            .setSmallIcon(R.drawable.app_icon_vector)
//                            //build summary info into InboxStyle template
//                            .setStyle(new NotificationCompat.InboxStyle()
//                                    .addLine("Alex Faarborg  Check this out")
//                                    .addLine("Jeff Chang    Launch Party")
//                                    .setBigContentTitle("2 new messages")
//                                    .setSummaryText("janedoe@example.com"))
//                            //specify which group this notification belongs to
//                            .setGroup(GROUP_ID)
//                            //set this notification as the summary for the group
//                            .setGroupSummary(true)
//                            .build();
//            manager.notify(0, summaryNotification);
        } else
            Log.e(TAG, "getApplicationContext returned null");
    }

    private void updateNotifiedTasks(Task item) {
        item.setIsNotified(1);
        ContentValues rows = new ContentValues();
        rows.put("task_id", item.getId());
        rows.put("task_name", item.getName());
        rows.put("task_date", item.getDate());
        rows.put("task_time", item.getTime());
        rows.put("description", item.getDescription());
        rows.put("is_notified", item.getIsNotified());
        rows.put("is_completed", item.getIsCompleted());
        String dateTimeValue = DateTime.getDateTimeValue(item.getDate(), item.getTime());
        rows.put("date_time", dateTimeValue);
        String[] args = new String[]{item.getId()};
        dbQuery.mRef.update("tasks", rows, "task_id = ?", args);
    }
}
