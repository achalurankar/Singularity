package com.android.singularity.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.android.singularity.R;
import com.android.singularity.util.Constants;
import com.android.singularity.util.Credentials;
import com.andromeda.calloutmanager.Session;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //for full screen activity, removing status bar
        //store access token for future use
        if (!Session.isTokenValid)
            Session.storeAccessToken(Constants.ACCESS_TOKEN_ENDPOINT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //open first activity
        loadCredentials();
        openActivity();
    }

    private void loadCredentials() {
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Credentials.CLIENT_ID =  sharedPref.getString("CLIENT_ID", null);
        Credentials.CLIENT_SECRET =  sharedPref.getString("CLIENT_SECRET", null);
        Credentials.PASSWORD =  sharedPref.getString("PASSWORD", null);
        Credentials.SECURITY_TOKEN =  sharedPref.getString("SECURITY_TOKEN", null);
        Credentials.USERNAME =  sharedPref.getString("USERNAME", null);
    }

    private void openActivity() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splash.this, ParentActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 500);
    }
}