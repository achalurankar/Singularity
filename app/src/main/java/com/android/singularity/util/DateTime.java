package com.android.singularity.util;

import android.util.Log;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateTime {
    //variables for date
    private int mDay, mMonth, mYear;
    //variables for time
    private int mHour, mMinute, mSeconds;

    //constructor
    public DateTime() {
        getCurrentDate();
    }

    public static String getDisplayDate(String date) {
        //DD/MM/YYYY to DDth MMM YYYY
        String formatted = "";
        String[] arr = date.split("/");
        int day = Integer.parseInt(arr[0]);
        int month = Integer.parseInt(arr[1]);
        formatted = "" + day;
        if (day != 11 && day != 12 && day != 13) {
            switch (arr[0].charAt(1)) {
                case '1':
                    formatted += "st ";
                    break;
                case '2':
                    formatted += "nd ";
                    break;
                case '3':
                    formatted += "rd ";
                    break;
                default:
                    formatted += "th ";
            }
        } else {
            formatted += "th ";
        }
        formatted += numberVsNameMap.get(month);
        formatted += " " + arr[2];
        return formatted;
    }

    public static Map<Integer, String> numberVsNameMap = new HashMap<>();
    public static Map<String, Integer> nameVsNumberMap = new HashMap<>();

    private static final String TAG = "DateTime";

    public static String getGMTDateTime(String date, String time) {
        String gmtString = "";
        // converting ist to gmt for salesforce -
        // required format 2022-01-15T14:30:00.000Z
        // from 2/1/2022 date and time 20:41
        String[] dateSplit = date.split("/");
        String[] timeSplit = time.split(":");
        int year = Integer.parseInt(dateSplit[2]);
        int month = Integer.parseInt(dateSplit[1]);
        int day = Integer.parseInt(dateSplit[0]);
        int hour = Integer.parseInt(timeSplit[0]);
        int min = Integer.parseInt(timeSplit[1]);
        // current format is in IST, convert to GMT which is 5:30 hrs behind IST
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, min, 0);
        // remove 5hrs and 30 mins from IST
        calendar.add(Calendar.HOUR_OF_DAY, -5);
        calendar.add(Calendar.MINUTE, -30);
        //get gmt date time
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        if (month == 0)
            month = 12;
        day = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);
        gmtString = year + "-"
                + (String.valueOf(month).length() == 1 ? "0" + month : month) + "-"
                + (String.valueOf(day).length() == 1 ? "0" + day : day) + "T"
                + (String.valueOf(hour).length() == 1 ? "0" + hour : hour) + ":"
                + (String.valueOf(min).length() == 1 ? "0" + min : min)
                + ":00.000Z";
        Log.e(TAG, "getGMTDateTime: " + calendar.getTimeInMillis());
        return gmtString;
    }

    public static Map<String, String> getDateTimeForEditorForm(String displayDateTime) {
        Map<String, String> datetime = new HashMap<>();
        String[] dtTT = displayDateTime.split(" ");
        String date = dtTT[0] + "/" + nameVsNumberMap.get(dtTT[1]) + "/" + dtTT[2];
        String time = dtTT[3];
        datetime.put("date", date);
        datetime.put("time", time);
        return datetime;
    }

    static {
        String[] monthList = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
        for (int i = 0; i < 12; i++) {
            nameVsNumberMap.put(monthList[i], i + 1);
            numberVsNameMap.put(i + 1, monthList[i]);
        }
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
        if (this.mDay < 10)
            day = "0" + this.mDay;
        else
            day = "" + this.mDay;
        if (this.mMonth < 10)
            month = "0" + this.mMonth;
        else
            month = "" + this.mMonth;
        return day + "/" + month + "/" + this.mYear;
    }

    // static utility methods
    @NonNull
    public static String getDayOfWeek(@NonNull String date) {
        try {
            Date obj;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat dayFormat = new SimpleDateFormat("EEEE");
            obj = dateFormat.parse(date);
            return dayFormat.format(obj);
        } catch (ParseException e) {
            System.out.println("Unparseable using " + e.getMessage());
        }
        return "";
    }

    //method to gate date time format value for database
    public static String getDateTimeValue(String date, String timeValue) {
        String[] dateArr = date.split("/");
        String[] timeArr = timeValue.split(":");
        int hour = Integer.parseInt(timeArr[0]);
        int min = Integer.parseInt(timeArr[1]);
        if (timeArr[2].equals("PM")) {
            if (hour != 12)
                hour += 12;
        } else {
            if (hour == 12)
                hour = 0;
        }
        String minStr = "", hourStr = "";
        if (min < 10)
            minStr = "0" + min;
        else
            minStr = min + "";
        if (hour < 10)
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
