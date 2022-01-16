package com.android.singularity.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.singularity.R;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;
import com.android.singularity.util.EventDispatcher;
import com.andromeda.callouts.CalloutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TaskEditor extends AppCompatActivity {

    private static final String TAG = "TaskEditor";
    EditText TaskName, Description;
    RelativeLayout CalendarBtn, SaveBtn, ClockBtn;
    TextView DateTextView, TimeTextView;
    JSONObject mTask;
    RelativeLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);
        //initialize variables
        mContainer = findViewById(R.id.container);
        TaskName = findViewById(R.id.task_name_et);
        Description = findViewById(R.id.description);
        CalendarBtn = findViewById(R.id.date_btn);
        ClockBtn = findViewById(R.id.time_btn);
        DateTextView = findViewById(R.id.date_tv);
        TimeTextView = findViewById(R.id.time_tv);
        SaveBtn = findViewById(R.id.save_btn);
        //attaching listeners
        CalendarBtn.setOnClickListener(v -> popupCalendar());
        ClockBtn.setOnClickListener(v -> popupClock());
        SaveBtn.setOnClickListener(v -> updateTask());
        findViewById(R.id.back).setOnClickListener(v -> finish());
        mTask = TaskList.selectedTask;
        if (mTask != null) {
            setupForm();
        } else
            DateTextView.setText(new DateTime().getDateForUser());
    }

    private void setupForm() {
        Map<String, String> datetime = new HashMap<>();
        try {
            datetime = DateTime.getDateTimeForEditorForm(mTask.getString("Display_Date_Time__c"));
            TaskName.setText(mTask.getString("Name"));
            Description.setText(mTask.getString("Description__c"));
        } catch (JSONException ignored) {

        }
        DateTextView.setText(datetime.get("date"));
        TimeTextView.setText(datetime.get("time"));
    }

    private void updateTask() {
        setLoading(true);
        String name = TaskName.getText().toString().trim();
        String date = DateTextView.getText().toString();
        String time = TimeTextView.getText().toString();
        String description = Description.getText().toString().trim();
        String taskId = "";
        if (mTask != null) {
            try {
                taskId = mTask.getString("Id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //validation
        if (name.length() == 0) {
            Toast.makeText(getApplicationContext(), "Enter Name!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.toLowerCase().contains("date")) {
            Toast.makeText(getApplicationContext(), "Date not selected!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (time.toLowerCase().contains("time")) {
            Toast.makeText(getApplicationContext(), "Time not selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        String gmtDateTimeValue = DateTime.getGMTDateTime(date, time);
        JSONObject requestStructure = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            requestStructure.put("id", taskId);
            requestStructure.put("name", name);
            requestStructure.put("taskTime", gmtDateTimeValue);
            requestStructure.put("isCompleted", false);
            requestStructure.put("description", description);
            requestStructure.put("action", "upsert");
            params.put("requestStructure", requestStructure.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CalloutManager.makeCall(Constants.API_ENDPOINT, "POST", params, response -> {
            if(response != null) {
                setLoading(false);
                //call event change listener invoker
                EventDispatcher.callOnDataChange();
                //close current activity
                TaskEditor.this.finish();
            }
        });

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
            String date = day + "/" + month + "/" + year;
            DateTextView.setText(date);
            dialog.dismiss();
        });
        dialog.show();
    }

    public void setLoading(boolean loading) {
        View view = findViewById(R.id.loader);
        if(loading) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(1);
            mContainer.setAlpha(0.4f);
        } else {
            view.setVisibility(View.INVISIBLE);
            mContainer.setAlpha(1);
        }
    }
    private void popupClock() {
        Dialog dialog = new Dialog(TaskEditor.this);
        dialog.setContentView(R.layout.clock_pop_up);
        TextView ConfirmBtn = dialog.findViewById(R.id.confirm_button);
        TimePicker timePicker = dialog.findViewById(R.id.clock);
        ConfirmBtn.setOnClickListener(v -> {
            int hours = timePicker.getHour();
            int minutes = timePicker.getMinute();
            TimeTextView.setText(hours + ":" + minutes);
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}