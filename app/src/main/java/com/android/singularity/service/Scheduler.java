package com.android.singularity.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.singularity.tasks.Task;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DbQuery;
import com.android.singularity.util.PreferenceManager;

import java.util.Calendar;

public class Scheduler {

    private static final String TAG = "Scheduler";

    public static void schedule(Task task, Context context) {
        setExact(task, context, getCalendarForTask(task));
    }

    public static void snooze(Task task, Context context) {
        // snooze for next 15 minutes
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15);
        PreferenceManager.setSnoozed(context, task.getId(), true); // set snooze flag for this task
        setExact(task, context, calendar);
    }

    public static Calendar getCalendarForTask(Task task) {
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
        if (timeSplit[2].equals("PM")) {
            //time is in PM
            if (hour != 12) //if 12 do nothing
                hour += 12;
        } else {
            //time is in AM
            if (hour == 12) //if 12AM change it to 0 for 24 hr format
                hour = 0;
        }
        second = 0;
        cal.set(year, month, date, hour, minute, second);
        return cal;
    }

    public static void setExact(Task task, Context context, Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.e(TAG, "schedule: date val " + task.getDate() + " time " + task.getTime());
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("taskId", String.valueOf(task.getId()));
        PendingIntent broadcast = PendingIntent.getBroadcast(context, task.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), broadcast);
    }


    public static void setNextAlert(Task task, Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(task.getCurrentSchedule());
        switch (task.getFrequency()) {
            case Constants.DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case Constants.MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
            case Constants.WEEKLY:
                calendar.add(Calendar.DATE, 7);
                break;
            default:
                return;
        }
        if(!PreferenceManager.isSnoozed(context, task.getId())) {
            task.setCurrentSchedule(calendar.getTimeInMillis());
            DbQuery dbQuery = new DbQuery(context);
            dbQuery.upsertTask(task);
        }
        else {
            PreferenceManager.setSnoozed(context, task.getId(), false); // unset snooze flag for this task
        }
        setExact(task, context, calendar);
    }
}
