package com.android.singularity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.android.singularity.R;
import com.android.singularity.main.ParentActivity;
import com.android.singularity.util.Constants;
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
        openActivity();
    }

    private void openActivity() {
        //splash for 1 second = 1000 milliseconds
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splash.this, ParentActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1000);
    }
}