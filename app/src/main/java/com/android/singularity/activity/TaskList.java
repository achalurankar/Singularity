package com.android.singularity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.singularity.R;
import com.android.singularity.adapter.TaskAdapter;
import com.android.singularity.modal.Task;
import com.android.singularity.util.DateTime;
import com.android.singularity.util.DbQuery;
import com.android.singularity.util.EventDispatcher;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class TaskList extends AppCompatActivity {

    private static final String TAG = "TaskList";
    RecyclerView mRecyclerView;
    public TaskAdapter mAdapter;
    List<Task> mList = new ArrayList<>();
    TextView DateTV, DayTV;
    public static Task selectedTask;
    LinearLayout NoResultsLayout;
    DbQuery dbQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        dbQuery = new DbQuery(this);
        DateTV = findViewById(R.id.date);
        DayTV = findViewById(R.id.day);
        NoResultsLayout = findViewById(R.id.no_result_layout);
        // recycler view
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(TaskList.this));
        mAdapter = new TaskAdapter(this, TaskList.this, mList);
        mRecyclerView.setAdapter(mAdapter);
        //action btn
        findViewById(R.id.add_task_btn).setOnClickListener(v -> openTaskAdder());
        setTouchCallback();
        //get today's date and update the view
        String currentDate = new DateTime().getDateForUser();
        setDayDate(currentDate);
        getTasks();
        //add database change listener
        EventDispatcher.addEventListener(this::getTasks);
    }

    private void setDayDate(String date) {
        DateTV.setText(date);
        DayTV.setText(DateTime.getDayOfWeek(date));
    }

    private void openTaskAdder() {
        selectedTask = null;
        startActivity(new Intent(getApplicationContext(), TaskEditor.class));
        overridePendingTransition(0, 0);
    }

    void getTasks() {
        NoResultsLayout.setVisibility(View.INVISIBLE);
        mList = dbQuery.getTasks();
        if (mList.size() != 0) {
            mAdapter = new TaskAdapter(this, TaskList.this, mList);
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
                Task item = mAdapter.get(index);
                mAdapter.remove(index);
                Snackbar snackbar = Snackbar.make(mRecyclerView, "Task removed!", 2500);
                Handler handler = new Handler();
                Runnable runnable = () -> {
                    removeFromDatabase(item);
                };
                handler.postDelayed(runnable, 2600);
                snackbar.setAction("UNDO", v -> {
                    handler.removeCallbacks(runnable);
                    mAdapter.add(index, item);
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

    public void setComplete(Task task) {
        task.setIsCompleted(1);
        dbQuery.upsertTask(task);
        Toast.makeText(getApplicationContext(), "Task completed!", Toast.LENGTH_SHORT).show();
        getTasks();
    }
}