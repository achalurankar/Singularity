package com.android.singularity.tasks;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.singularity.R;
import com.android.singularity.activity.ParentActivity;
import com.android.singularity.service.Scheduler;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;
import com.android.singularity.util.DbQuery;

public class TaskEditor extends AppCompatActivity {

    EditText TaskName, Description;
    RelativeLayout CalendarBtn, SaveBtn, ClockBtn;
    TextView Date, TimeTextView;
    String TimeValue = "Select time";
    Task mTask;
    int taskType;
    // spinner
    Spinner frequencySpinner;
    ArrayAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);
        //initialize variables
        TaskName = findViewById(R.id.task_name_et);
        Description = findViewById(R.id.description);
        CalendarBtn = findViewById(R.id.date_btn);
        ClockBtn = findViewById(R.id.time_btn);
        Date = findViewById(R.id.date_tv);
        TimeTextView = findViewById(R.id.time_tv);
        SaveBtn = findViewById(R.id.save_btn);
        //attaching listeners
        CalendarBtn.setOnClickListener(v -> popupCalendar());
        ClockBtn.setOnClickListener(v -> popupClock());
        SaveBtn.setOnClickListener(v -> updateTask());
        findViewById(R.id.back).setOnClickListener(v -> finish());
        taskType = getIntent().getIntExtra("type", 0);
        mTask = ParentActivity.selectedTask;
        if (taskType == Constants.TYPE_NOTE) {
            CalendarBtn.setVisibility(View.GONE);
            ClockBtn.setVisibility(View.GONE);
            findViewById(R.id.time_label).setVisibility(View.GONE);
            findViewById(R.id.date_label).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.header_label)).setText("Add new note");
        }

        // frequency spinner population
        if (taskType == Constants.TYPE_ALERT) {
            findViewById(R.id.frequency_layout).setVisibility(View.VISIBLE);
            frequencySpinner = findViewById(R.id.frequency_spinner);
            spinnerAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, Constants.frequencyOptions);
            spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_item);
            frequencySpinner.setAdapter(spinnerAdapter);
        }
        if (mTask != null) {
            setupForm();
        } else
            Date.setText(new DateTime().getDateForUser());
    }

    private void setupForm() {
        TaskName.setText(mTask.getName());
        Description.setText(mTask.getDescription());
        Date.setText(mTask.getDate());
        TimeTextView.setText(mTask.getTime());
        TimeValue = mTask.getTime();
    }

    private void updateTask() {
        hideKeyboard();
        String name = TaskName.getText().toString().trim();
        String description = Description.getText().toString().trim();
        int taskId = 0;
        if (mTask != null)
            taskId = mTask.getId();
        int isNotified = 0;
        int isCompleted = 0;

        //validation
        if (name.length() == 0) {
            Toast.makeText(getApplicationContext(), "Enter Name!", Toast.LENGTH_SHORT).show();
            return;
        }
        String date = "";
        if (taskType != Constants.TYPE_NOTE) {
            date = Date.getText().toString();
            if (date.toLowerCase().contains("date")) {
                Toast.makeText(getApplicationContext(), "Date not selected!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TimeValue.contains("time")) {
                Toast.makeText(this, "Time not selected!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //upsert task
        int frequency = 0;
        if(taskType == Constants.TYPE_ALERT)
            frequency = Constants.freqTextVsIntMap.get(frequencySpinner.getSelectedItem());
        Task task = new Task(taskType, frequency, taskId, name, date, TimeValue, description, isNotified, isCompleted);
        task.setCurrentSchedule(Scheduler.getCalendarForTask(task).getTimeInMillis());
        DbQuery dbQuery = new DbQuery(this);
        task.setId(dbQuery.upsertTask(task));
        if (mTask == null) {
            Toast.makeText(getApplicationContext(), "Task added!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Task updated!", Toast.LENGTH_SHORT).show();
        }

        if (taskType == Constants.TYPE_ALERT) {
            // schedule task in future
            Scheduler.schedule(task, this);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.container).getWindowToken(), 0);
    }

    private void popupCalendar() {
        Dialog dialog = new Dialog(TaskEditor.this);
        dialog.setContentView(R.layout.calendar_pop_up);
        TextView ConfirmBtn = dialog.findViewById(R.id.confirm_button);
        DatePicker datePicker = dialog.findViewById(R.id.calendar);
        ConfirmBtn.setOnClickListener(v -> {
            int month = datePicker.getMonth() + 1;
            int day = datePicker.getDayOfMonth();
            int year = datePicker.getYear();
            String dayStr, monStr;
            if (day / 10 == 0)
                dayStr = "0" + day;
            else
                dayStr = String.valueOf(day);
            if (month / 10 == 0)
                monStr = "0" + month;
            else
                monStr = String.valueOf(month);
            String date = dayStr + "/" + monStr + "/" + year;
            Date.setText(date);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void popupClock() {
        Dialog dialog = new Dialog(TaskEditor.this);
        dialog.setContentView(R.layout.clock_pop_up);
        TextView ConfirmBtn = dialog.findViewById(R.id.confirm_button);
        TimePicker timePicker = dialog.findViewById(R.id.clock);
        ConfirmBtn.setOnClickListener(v -> {
            int hours = timePicker.getHour();
            int minutes = timePicker.getMinute();
            String meridiem = "";
            //converting to 12hr format for user perspective
            if (hours > 12) {
                hours -= 12;
                meridiem = "PM";
            } else {
                if (hours == 0) {
                    hours = 12;
                    meridiem = "AM";
                } else {
                    if (hours == 12)
                        meridiem = "PM";
                    else
                        meridiem = "AM";
                }
            }
            //prepending zero if needed
            String minStr = "", hourStr = "";
            if (minutes < 10)
                minStr = "0" + minutes;
            else
                minStr = minutes + "";
            if (hours < 10)
                hourStr = "0" + hours;
            else
                hourStr = hours + "";
            TimeValue = hourStr + ":" + minStr + ":" + meridiem;
            TimeTextView.setText(TimeValue);
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}