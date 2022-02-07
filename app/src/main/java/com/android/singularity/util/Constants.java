package com.android.singularity.util;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    // task types
    public static final int TYPE_NOTE = 1;
    public static final int TYPE_ALERT = 2;
    public static final int TYPE_EMAIL = 3;

    // api endpoints
    public static final String ACCESS_TOKEN_ENDPOINT = "https://login.salesforce.com/services/oauth2/token?grant_type=password&client_id=3MVG9pRzvMkjMb6mVmRLS1jKALgWzMgGDQhnBh4MfyEQv_ATznjlpPw9ffz3Ur.ZxRF3K6347v7GnsdgUeuJh&client_secret=6781FB6C8723F54B5C352552D8386E7ADD2CFB9F9D863C627F15155AF7E82DA4&username=achal_urankar@playful-otter-5orsso.com&password=achal123gm1o0SIIbHZfQbkKCpMTmEvw";
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
