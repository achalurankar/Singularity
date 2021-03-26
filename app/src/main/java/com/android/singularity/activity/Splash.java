package com.android.singularity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.android.singularity.R;
import com.android.singularity.controller.AppController;
import com.android.singularity.service.Foreground;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //for full screen activity, removing status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //init controller
        initiateAppController();
        //open first activity
        openActivity();
    }

    private void openActivity() {
        //splash for 1 second = 1000 milliseconds
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splash.this, TaskList.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1000);
    }

    //attach app visibility listener through controller
    private void initiateAppController() {
        AppController.getInstance().setOnVisibilityChangeListener(isBackground -> {
            Intent intent = new Intent(Splash.this, Foreground.class);
            if (isBackground) {
                startService(intent);
            } else {
                stopService(intent);
            }
        });
    }
}