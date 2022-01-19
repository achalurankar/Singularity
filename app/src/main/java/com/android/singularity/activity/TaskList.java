package com.android.singularity.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.singularity.R;
import com.android.singularity.modal.Task;
import com.android.singularity.util.DateTime;
import com.android.singularity.util.DbQuery;
import com.android.singularity.util.EventDispatcher;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class TaskList extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TaskList";
    RecyclerView mRecyclerView;
    public CustomAdapter mAdapter;
    List<Task> mList = new ArrayList<>();
    TextView DateTV, DayTV;
    static Task selectedTask;
    LinearLayout NoResultsLayout;
    DbQuery dbQuery;

    //current date for editor
    static String CurrentDateForEditor = "Select Date";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        dbQuery = new DbQuery(this);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(TaskList.this));
        DateTV = findViewById(R.id.date);
        DayTV = findViewById(R.id.day);
        NoResultsLayout = findViewById(R.id.no_result_layout);
        configureAdapter();
        findViewById(R.id.add_task_btn).setOnClickListener(v -> openTaskAdder());
        findViewById(R.id.calendar).setOnClickListener(v -> popupCalendar());
        setTouchCallback();
        //get today's date and update the view
        String currentDate = new DateTime().getDateForUser();
        setDayDate(currentDate);
        getTasks(currentDate);
        setupLeftRightDrags();
        //add database change listener
        EventDispatcher.addEventListener(() -> getTasks(DateTV.getText().toString()));
    }

    private void configureAdapter() {
        mAdapter = new CustomAdapter(this, TaskList.this, mList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupLeftRightDrags() {
        ImageView left, right;
        left = findViewById(R.id.drag_left_btn);
        right = findViewById(R.id.drag_right_btn);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
    }

    private void setDayDate(String date) {
        DateTV.setText(date);
        DayTV.setText(DateTime.getDayOfWeek(date));
    }

    private void openTaskAdder() {
        selectedTask = null;
        CurrentDateForEditor = DateTV.getText().toString();
        startActivity(new Intent(getApplicationContext(), TaskEditor.class));
        overridePendingTransition(0, 0);
    }

    @Override
    public void onClick(View v) {
        String newDate = "";
        if(v.getId() == R.id.drag_left_btn){
            newDate = DateTime.getPreviousDate(DateTV.getText().toString());
        } else if(v.getId() == R.id.drag_right_btn){
            newDate = DateTime.getNextDate(DateTV.getText().toString());
        }
        setDayDate(newDate);
        getTasks(newDate);
    }

    void getTasks(String inputDate) {
        NoResultsLayout.setVisibility(View.INVISIBLE);
        mList = dbQuery.getTasks(inputDate);
        if (mList.size() != 0) {
            mAdapter = new CustomAdapter(this, TaskList.this, mList);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            NoResultsLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(null);
        }
    }

    void setTouchCallback() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item
                int index = viewHolder.getLayoutPosition();
                Task item = mList.get(index);
                mAdapter.remove(index);
                Snackbar snackbar = Snackbar.make(mRecyclerView, "Task removed!", 2500);
                Handler handler = new Handler();
                Runnable runnable = () -> {
                    snackbar.dismiss();
                    removeFromDatabase(item);
                };
                handler.postDelayed(runnable, 2600);
                snackbar.setAction("UNDO", v -> {
                    handler.removeCallbacks(runnable);
                    mAdapter.add(index, item);
                    configureAdapter();
                });
                snackbar.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void removeFromDatabase(Task item) {
        dbQuery.deleteTask(item);
    }

    //to get date for query
    void popupCalendar() {
        Dialog dialog = new Dialog(TaskList.this);
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
            DateTV.setText(date);
            DayTV.setText(DateTime.getDayOfWeek(date));
            getTasks(date);
            dialog.dismiss();
        });
        dialog.show();
    }

    public void setComplete(Task task) {
        task.setIsCompleted(1);
        dbQuery.upsertTask(task);
        Toast.makeText(getApplicationContext(), "Task completed!", Toast.LENGTH_SHORT).show();
        getTasks(task.getDate());
    }
}