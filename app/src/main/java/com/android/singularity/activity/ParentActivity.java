package com.android.singularity.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.android.singularity.R;
import com.android.singularity.email.EmailEditor;
import com.android.singularity.email.TaskWrapper;
import com.android.singularity.tasks.TaskEditor;
import com.android.singularity.email.EmailFragment;
import com.android.singularity.tasks.NotesFragment;
import com.android.singularity.tasks.TasksFragment;
import com.android.singularity.tasks.Task;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ParentActivity extends AppCompatActivity {

    TextView DateTV, DayTV, AddBtnText;
    BottomNavigationView bottomNavigationView;

    public interface ItemClickListener {
        void onClick(Task task);

        void OnWrapperObjectClick(TaskWrapper jsonObject);
    }

    public static ItemClickListener itemClickListener;
    public static Task selectedTask;
    public static TaskWrapper mTaskWrapper;

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
        transaction.replace(R.id.frame_layout, new EmailFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        setupBottomNavBar();
        itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(Task task) {
                openEditor(task, null);
            }

            @Override
            public void OnWrapperObjectClick(TaskWrapper taskWrapper) {
                openEditor(null, taskWrapper);
            }
        };
    }

    @SuppressLint("NonConstantResourceId")
    private void openEditor(Task task, TaskWrapper taskWrapper) {
        int type;
        Class editor = TaskEditor.class;
        switch (bottomNavigationView.getSelectedItemId()) {
            case R.id.alerts:
                type = Constants.TYPE_ALERT;
                break;
            case R.id.email:
                type = Constants.TYPE_EMAIL;
                editor = EmailEditor.class;
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
            mTaskWrapper = taskWrapper;
        }
        Intent intent = new Intent(getApplicationContext(), editor);
        intent.putExtra("type", type);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @SuppressLint("NonConstantResourceId")
    public void setupBottomNavBar() {
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.email);
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
                    setAddBtnText("Add Note");
                    transaction.replace(R.id.frame_layout, new NotesFragment());
                    break;
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