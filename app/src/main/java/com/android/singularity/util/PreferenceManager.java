package com.android.singularity.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    public static final String PREFERENCE_FILE_NAME = "Singularity_Snooze";

    public static boolean isSnoozed(Context context, int taskId) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean(String.valueOf(taskId), false);
    }

    public static void setSnoozed(Context context, int taskId, boolean value) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(String.valueOf(taskId), value);
        editor.apply();
    }
}
