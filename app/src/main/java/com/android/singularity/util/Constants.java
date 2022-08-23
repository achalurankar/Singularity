package com.android.singularity.util;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    // task types
    public static final int TYPE_NOTE = 1;
    public static final int TYPE_ALERT = 2;
    public static final int TYPE_EMAIL = 3;

    // api endpoints
    public static String ACCESS_TOKEN_ENDPOINT = "https://login.salesforce.com/services/oauth2/token?grant_type=password&client_id=" + Credentials.CLIENT_ID + "&client_secret=" + Credentials.CLIENT_SECRET +"&username=" + Credentials.USERNAME + "&password=" + Credentials.PASSWORD + Credentials.SECURITY_TOKEN;
    public static String API_ENDPOINT = "https://playful-otter-5orsso-dev-ed.my.salesforce.com/services/apexrest/Todo";

    // task frequency constants
    public static final int ONE_TIME = 1;
    public static final int DAILY = 2;
    public static final int WEEKLY = 3;
    public static final int MONTHLY = 4;

    public static final Map<String, Integer> freqTextVsIntMap = new HashMap<>();
    public static final String[] frequencyOptions = new String[]{"One time", "Daily", "Weekly", "Monthly"};

    static {
        for (int i = 0; i < frequencyOptions.length; i++) {
            freqTextVsIntMap.put(frequencyOptions[i], i + 1);
        }
    }
}
