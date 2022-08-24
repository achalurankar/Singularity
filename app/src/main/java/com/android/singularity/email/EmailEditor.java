package com.android.singularity.email;

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
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;
import com.android.singularity.util.EventDispatcher;
import com.andromeda.calloutmanager.CalloutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EmailEditor extends AppCompatActivity {
    EditText TaskName, Description;
    RelativeLayout CalendarBtn, SaveBtn, ClockBtn;
    TextView Date, TimeTextView;
    String TimeValue = "Select time";
    TaskWrapper taskWrapper;
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
        taskWrapper = ParentActivity.mTaskWrapper;

        // frequency spinner population
        findViewById(R.id.frequency_layout).setVisibility(View.VISIBLE);
        frequencySpinner = findViewById(R.id.frequency_spinner);
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, Constants.frequencyOptions);
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_item);
        frequencySpinner.setAdapter(spinnerAdapter);

        if (taskWrapper != null) {
            setupForm();
        } else
            Date.setText(new DateTime().getDateForUser());
    }

    private void setupForm() {
        Map<String, String> datetime = DateTime.getDateTimeForEditorForm(taskWrapper.displayDateTime);
        TaskName.setText(taskWrapper.name);
        frequencySpinner.setSelection(spinnerAdapter.getPosition(taskWrapper.frequency));
        if (taskWrapper.description != null)
            Description.setText(taskWrapper.description);
        Date.setText(datetime.get("date"));
        TimeTextView.setText(datetime.get("time"));
        TimeValue = "";
    }

    private void updateTask() {
        hideKeyboard();
        String name = TaskName.getText().toString().trim();
        String description = Description.getText().toString().trim();

        //validation
        if (name.length() == 0) {
            Toast.makeText(getApplicationContext(), "Enter Name!", Toast.LENGTH_SHORT).show();
            return;
        }
        String date = "";
        date = Date.getText().toString();
        if (date.toLowerCase().contains("date")) {
            Toast.makeText(getApplicationContext(), "Date not selected!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TimeValue.contains("time")) {
            Toast.makeText(this, "Time not selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        //upsert task
        String time = TimeTextView.getText().toString();
        String gmtDateTimeValue = DateTime.getGMTDateTime(date, time);
        JSONObject requestStructure = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            String id = "";
            if (taskWrapper != null)
                id = taskWrapper.id;
            requestStructure.put("id", id);
            requestStructure.put("name", name);
            requestStructure.put("taskTime", gmtDateTimeValue);
            requestStructure.put("isCompleted", false);
            requestStructure.put("description", description);
            requestStructure.put("frequency", frequencySpinner.getSelectedItem());
            requestStructure.put("action", "upsert");
            params.put("requestStructure", requestStructure.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CalloutManager.makeCall(Constants.getApiEndpoint(), "POST", params, new CalloutManager.ResponseListener() {
            @Override
            public void onSuccess(String s) {
                EmailEditor.this.runOnUiThread(() -> {
                    //call event change listener invoker
                    EventDispatcher.callOnDataChange();
                    //close current activity
                    EmailEditor.this.finish();
                });
            }

            @Override
            public void onError(String error) {
                EmailEditor.this.runOnUiThread(() -> {
                    String message = "Something went wrong!";
                    if (error.contains("will never fire"))
                        message = "Task time cannot be in past";
                    Toast.makeText(EmailEditor.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.container).getWindowToken(), 0);
    }

    private void popupCalendar() {
        Dialog dialog = new Dialog(EmailEditor.this);
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
        Dialog dialog = new Dialog(EmailEditor.this);
        dialog.setContentView(R.layout.clock_pop_up);
        TextView ConfirmBtn = dialog.findViewById(R.id.confirm_button);
        TimePicker timePicker = dialog.findViewById(R.id.clock);
        ConfirmBtn.setOnClickListener(v -> {
            int hours = timePicker.getHour();
            int minutes = timePicker.getMinute();
            TimeTextView.setText(hours + ":" + minutes);
            TimeValue = "";
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}