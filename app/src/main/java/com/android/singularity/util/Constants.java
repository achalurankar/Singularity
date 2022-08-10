package com.android.singularity.util;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    // task types
    public static final int TYPE_NOTE = 1;
    public static final int TYPE_ALERT = 2;
    public static final int TYPE_EMAIL = 3;

    //credentials
    private static final String USERNAME = "your_username";
    private static final String PASSWORD = "your_password";
    private static final String SECURITY_TOKEN = "your_security_token";
    private static final String CLIENT_ID = "your_client_id";
    private static final String CLIENT_SECRET = "your_client_secret";

    // api endpoints
    public static final String ACCESS_TOKEN_ENDPOINT = "https://login.salesforce.com/services/oauth2/token?grant_type=password&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET +"&username=" + USERNAME + "&password=" + PASSWORD + SECURITY_TOKEN;
    public static final String API_ENDPOINT = "https://playful-otter-5orsso-dev-ed.my.salesforce.com/services/apexrest/Todo";

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
