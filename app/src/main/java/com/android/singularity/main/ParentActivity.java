package com.android.singularity.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.android.singularity.R;
import com.android.singularity.activity.TaskEditor;
import com.android.singularity.fragment.TasksFragment;
import com.android.singularity.modal.Task;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ParentActivity extends AppCompatActivity {

    TextView DateTV, DayTV, AddBtnText;
    BottomNavigationView bottomNavigationView;

    public interface ItemClickListener {
        void onClick(Task task);
    }
    public static ItemClickListener itemClickListener;
    public static Task selectedTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        DateTV = findViewById(R.id.date);
        DayTV = findViewById(R.id.day);
        AddBtnText = findViewById(R.id.add_btn_text);
        findViewById(R.id.add_task_btn).setOnClickListener(v -> openEditor(null));
        //get today's date and update the view
        String currentDate = new DateTime().getDateForUser();
        setDayDate(currentDate);
        loadTasksFragment(Constants.TYPE_ALERT);
        setupBottomNavBar();
        itemClickListener = this::openEditor;
    }

    private void openEditor(Task task) {
        selectedTask = task;
        int type;
        if(bottomNavigationView.getSelectedItemId() == R.id.alerts) {
            type = Constants.TYPE_ALERT;
        } else {
            type = Constants.TYPE_NOTE;
        }
        Intent intent = new Intent(getApplicationContext(), TaskEditor.class);
        intent.putExtra("type", type);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void loadTasksFragment(int type) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new TasksFragment(type));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @SuppressLint("NonConstantResourceId")
    public void setupBottomNavBar() {
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.alerts);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.alerts:
                    setAddBtnText("Add Task");
                    loadTasksFragment(Constants.TYPE_ALERT);
                    return true;
                case R.id.notes:
                    //todo load notes fragment
                    setAddBtnText("Add Note");
                    loadTasksFragment(Constants.TYPE_NOTE);
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