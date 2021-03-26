package com.android.singularity.util;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTime {
    //variables for date
    private int mDay, mMonth, mYear;
    //variables for time
    private int mHour, mMinute, mSeconds;

    //constructor
    public DateTime() {
        getCurrentDate();
    }

    //constructor function
    public void getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date obj = new Date();
        //this will separate date and time
        String[] splitFormat = dateFormat.format(obj).split(" ");

        //whole date as string
        String compoundDate = splitFormat[0];
        //date as array
        String[] splitDate = compoundDate.split("/");
        //setting those values
        this.setDay(Integer.parseInt(splitDate[0]));
        this.setMonth(Integer.parseInt(splitDate[1]));
        this.setYear(Integer.parseInt(splitDate[2]));

        //whole time as string
        String compoundTime = splitFormat[1];
        //time as array
        String[] splitTime = compoundTime.split(":");
        //setting those values
        this.setHour(Integer.parseInt(splitTime[0]));
        this.setMinute(Integer.parseInt(splitTime[1]));
        this.setSeconds(Integer.parseInt(splitTime[2]));
    }

    //method related to current instance of date
    public String getDateForUser() {
        String day, month;
        if(this.mDay < 10)
            day = "0" + this.mDay;
        else
            day = "" + this.mDay;
        if(this.mMonth < 10)
            month = "0" + this.mMonth;
        else
            month = "" + this.mMonth;
        return day + "/" + month + "/" + this.mYear;
    }

    //method related to current instance of time
    public String getTimeForUser() {
        String hour, minute, seconds;
        if(this.mHour < 10)
            hour = "0" + this.mHour;
        else
            hour = "" + this.mHour;
        if(this.mMinute < 10)
            minute = "0" + this.mMinute;
        else
            minute = "" + this.mMinute;
        if(this.mSeconds < 10)
            seconds = "0" + this.mSeconds;
        else
            seconds = "" + this.mSeconds;
        return hour + ":" + minute + ":" + seconds;
    }

    // static utility methods
    @NonNull public static String getDayOfWeek(@NonNull String date) {
        try {
            Date obj;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat dayFormat = new SimpleDateFormat("EEEE");
            obj = dateFormat.parse(date);
            return dayFormat.format(obj);
        }
        catch (ParseException e) {
            System.out.println("Unparseable using " + e.getMessage());
        }
        return "";
    }

    @NonNull public static String getNextDate(@NonNull String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e){
            System.out.println(e.getMessage());
        }
        c.add(Calendar.DATE, 1);  // number of days to add
        date = sdf.format(c.getTime());  // dt is now the new date
        return date;
    }

    @NonNull public static String getPreviousDate(@NonNull String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        c.add(Calendar.DATE, -1);  // number of days to remove
        date = sdf.format(c.getTime());  // dt is now the new date
        return date;
    }

    //method to gate date time format value for database
    public static String getDateTimeValue(String date, String timeValue) {
        String[] dateArr = date.split("/");
        String[] timeArr = timeValue.split(":");
        int hour = Integer.parseInt(timeArr[0]);
        int min = Integer.parseInt(timeArr[1]);
        if(timeArr[2].equals("PM")) {
            if(hour != 12)
                hour += 12;
        }
        else{
            if(hour == 12)
                hour = 0;
        }
        String minStr = "", hourStr = "";
        if(min < 10)
            minStr = "0" + min;
        else
            minStr = min + "";
        if(hour < 10)
            hourStr = "0" + hour;
        else
            hourStr = hour + "";
        //required
        //2020-07-01T05:10:00
        //YYYY-MM-DDTHH:MM:SS
        return dateArr[2] + "-" + // YYYY
                dateArr[1] + "-" + // MM
                dateArr[0] + "T" + // DD
                hourStr + ":" + // HH
                minStr + ":00"; //MM:SS
    }

    //getters and setters
    public int getDay() {
        return mDay;
    }

    public void setDay(int day) {
        this.mDay = day;
    }

    public int getMonth() {
        return mMonth;
    }

    public void setMonth(int month) {
        this.mMonth = month;
    }

    public int getYear() {
        return mYear;
    }

    public void setYear(int year) {
        this.mYear = year;
    }

    public int getHour() {
        return mHour;
    }

    public void setHour(int hour) {
        this.mHour = hour;
    }

    public int getMinute() {
        return mMinute;
    }

    public void setMinute(int minute) {
        this.mMinute = minute;
    }

    public int getSeconds() {
        return mSeconds;
    }

    public void setSeconds(int mSeconds) {
        this.mSeconds = mSeconds;
    }
}
