package com.android.singularity.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.singularity.R;
import com.android.singularity.activity.TaskEditor;
import com.android.singularity.fragment.EmailFragment;
import com.android.singularity.fragment.NotesFragment;
import com.android.singularity.fragment.TasksFragment;
import com.android.singularity.modal.Task;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONObject;

public class ParentActivity extends AppCompatActivity {

    TextView DateTV, DayTV, AddBtnText;
    BottomNavigationView bottomNavigationView;

    public interface ItemClickListener {
        void onClick(Task task);

        void OnJSONObjectClick(JSONObject jsonObject);
    }

    public static ItemClickListener itemClickListener;
    public static Task selectedTask;
    public static JSONObject selectedJSONObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        DateTV = findViewById(R.id.date);
        DayTV = findViewById(R.id.day);
        AddBtnText = findViewById(R.id.add_btn_text);
        findViewById(R.id.add_task_btn).setOnClickListener(v -> openEditor(null, null));
        //get today's date and update the view
        String currentDate = new DateTime().getDateForUser();
        setDayDate(currentDate);
        //load tasks fragment  initially
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new TasksFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        setupBottomNavBar();
        itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(Task task) {
                openEditor(task, null);
            }

            @Override
            public void OnJSONObjectClick(JSONObject jsonObject) {
                openEditor(null, jsonObject);
            }
        };
    }

    @SuppressLint("NonConstantResourceId")
    private void openEditor(Task task, JSONObject jsonObject) {
        int type;
        switch (bottomNavigationView.getSelectedItemId()) {
            case R.id.alerts:
                type = Constants.TYPE_ALERT;
                break;
            case R.id.email:
                type = Constants.TYPE_EMAIL;
                break;
            case R.id.notes:
                type = Constants.TYPE_NOTE;
                break;
            default:
                type = 0;
        }
        if (type != Constants.TYPE_EMAIL) {
            selectedTask = task;
        } else {
            selectedJSONObj = jsonObject;
        }
        Intent intent = new Intent(getApplicationContext(), TaskEditor.class);
        intent.putExtra("type", type);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @SuppressLint("NonConstantResourceId")
    public void setupBottomNavBar() {
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.alerts);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (bottomNavigationView.getSelectedItemId() == item.getItemId()) {
                return false; // already in that fragment, no need to reopen same fragment
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.email:
                    setAddBtnText("Add Alert");
                    transaction.replace(R.id.frame_layout, new EmailFragment());
                    break;
                case R.id.alerts:
                    setAddBtnText("Add Task");
                    transaction.replace(R.id.frame_layout, new TasksFragment());
                    break;
                case R.id.notes:
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Log.e("TAG", "setupBottomNavBar: " + alarmManager.getNextAlarmClock());
                    return false;
            }
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        });
    }

    private void setAddBtnText(String text) {
        AddBtnText.setText(text);
    }

    private void setDayDate(String date) {
        DateTV.setText(date);
        DayTV.setText(DateTime.getDayOfWeek(date));
    }

}