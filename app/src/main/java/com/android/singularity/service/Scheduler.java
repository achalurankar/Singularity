package com.android.singularity.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.singularity.modal.Task;

import java.util.Calendar;

public class Scheduler {

    private static final String TAG = "Scheduler";

    public static void schedule(Task task, Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("taskId", task.getId());
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        int year, month, date, hour, minute, second;
        String dateSplit[] = task.getDate().split("/");
        String timeSplit[] = task.getTime().split(":");
        //date
        year = Integer.parseInt(dateSplit[2]);
        month = Integer.parseInt(dateSplit[1]);
        month--; // month index starts from 0
        date = Integer.parseInt(dateSplit[0]);
        //time
        hour = Integer.parseInt(timeSplit[0]);
        minute = Integer.parseInt(timeSplit[1]);
        if(timeSplit[2].equals("PM")){
            //time is in PM
            if(hour != 12) //if 12 do nothing
                hour += 12;
        } else {
            //time is in AM
            if(hour == 12) //if 12AM change it to 0 for 24 hr format
                hour = 0;
        }
        second = 0;
        cal.set(year, month, date, hour, minute, second);
        Log.e(TAG, "schedule: date val " + task.getDate() + " time " + task.getTime());
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
    }
}
