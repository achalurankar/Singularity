package com.android.singularity.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.android.singularity.R;
import com.android.singularity.activity.TaskEditor;
import com.android.singularity.fragment.NotesFragment;
import com.android.singularity.fragment.TasksFragment;
import com.android.singularity.util.DateTime;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ParentActivity extends AppCompatActivity {

    TextView DateTV, DayTV, AddBtnText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        DateTV = findViewById(R.id.date);
        DayTV = findViewById(R.id.day);
        AddBtnText = findViewById(R.id.add_btn_text);
        findViewById(R.id.add_task_btn).setOnClickListener(v -> openTaskAdder());
        //get today's date and update the view
        String currentDate = new DateTime().getDateForUser();
        setDayDate(currentDate);
        loadTasksFragment();
        setupBottomNavBar();
    }

    private void openTaskAdder() {
        TasksFragment.selectedTask = null;
        startActivity(new Intent(getApplicationContext(), TaskEditor.class));
        overridePendingTransition(0, 0);
    }

    private void loadTasksFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new TasksFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
    
    private void loadNotesFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new NotesFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setupBottomNavBar() {
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.alerts);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.alerts:
                    setAddBtnText("Add Task");
                    loadTasksFragment();
                    return true;
                case R.id.notes:
                    //todo load notes fragment
                    setAddBtnText("Add Note");
                    loadNotesFragment();
                    return true;
                default:
                    return false;
            }
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