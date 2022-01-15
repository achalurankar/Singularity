package com.android.singularity.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.singularity.R;
import com.android.singularity.modal.Task;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;
import com.android.singularity.util.EventDispatcher;
import com.andromeda.callouts.CalloutManager;
import com.andromeda.callouts.Session;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskList extends AppCompatActivity {

    private static final String TAG = "TaskList";
    RecyclerView mRecyclerView;
    public CustomAdapter mAdapter;
    TextView DateTV, DayTV;
    static JSONObject selectedTask;
    LinearLayout NoResultsLayout;

    // current date for editor
    static String CurrentDateForEditor = "Select Date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Session.storeAccessToken(Constants.ACCESS_TOKEN_ENDPOINT);
        DateTV = findViewById(R.id.date);
        DayTV = findViewById(R.id.day);
        NoResultsLayout = findViewById(R.id.no_result_layout);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(TaskList.this));
        findViewById(R.id.add_task_btn).setOnClickListener(v -> openTaskAdder());
        setTouchCallback();
        //get today's date and update the view
        CurrentDateForEditor = new DateTime().getDateForUser();
        setDayDate(CurrentDateForEditor);
        getTasks();
        //add database change listener
        EventDispatcher.addEventListener(this::getTasks);
    }

    public void setRecyclerViewAdapter(JSONArray jsonArray) {
        mRecyclerView.setAdapter(new CustomAdapter(TaskList.this, jsonArray));
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

    void getTasks() {
        //todo get tasks
        NoResultsLayout.setVisibility(View.INVISIBLE);
        CalloutManager.makeCall(Constants.API_ENDPOINT, "GET", new JSONObject(), response -> {
            if (response == null) return;
            if (response.equals(CalloutManager.TOKEN_INVALID)) {
                getTasks();
                return;
            }
            try {
                JSONArray jsonArray = new JSONArray(response);
                TaskList.this.runOnUiThread(() -> {
                    if (jsonArray.length() != 0) {
                        setRecyclerViewAdapter(jsonArray);
                    } else {
                        setRecyclerViewAdapter(new JSONArray());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });


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
                // todo swiped
//                int index = viewHolder.getLayoutPosition();
//                Task item = mList.get(index);
//                mList.remove(index);
//                Snackbar snackbar = Snackbar.make(mRecyclerView, "Task removed!", 2500);
//                Handler handler = new Handler();
//                Runnable runnable = () -> {
//                    snackbar.dismiss();
//                    removeFromDatabase(item);
//                };
//                handler.postDelayed(runnable, 2600);
//                snackbar.setAction("UNDO", v -> {
//                    handler.removeCallbacks(runnable);
//                    mList.add(index, item);
//                    configureRecyclerView();
//                });
//                snackbar.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void removeFromDatabase(Task item) {
        // todo delete task
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
        public final Context mContext;
        JSONArray mList;
        DateTime date;

        public CustomAdapter(Context context, JSONArray list) {
            mContext = context;
            mList = list;
            date = new DateTime();
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.tasks_item, parent, false);
            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
            // todo manage adapter bind view
            JSONObject task;
            try {
                task = mList.getJSONObject(position);
                holder.Name.setText(task.getString("name"));
//                holder.Description.setText(task.getDescription().length() == 0 ? "No description provided" : task.getDescription());
//                int isCompleted = task.getIsCompleted();
//                int isNotified = task.getIsNotified();
//                holder.Item.setOnClickListener(v -> {
//                    selectedTask = task;
//                    CurrentDateForEditor = DateTV.getText().toString();
//                    startActivity(new Intent(getApplicationContext(), TaskEditor.class));
//                    overridePendingTransition(0, 0);
//                });
//                String tt = task.getTime();
//                String[] tt_arr = tt.split(":");
//                int task_hour = Integer.parseInt(tt_arr[0]);
//                int task_min = Integer.parseInt(tt_arr[1]);
//                String task_med = tt_arr[2];
//                holder.Time.setText(tt_arr[0] + ":" + tt_arr[1] + " " + task_med);
//                holder.CompleteBtn.setOnClickListener(v -> setComplete(task));
//                //check status of task
//                if (isCompleted == 1) {
//                    holder.CompleteBtn.setVisibility(View.INVISIBLE);
//                    holder.TaskStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.completed_status));
//                } else
//                    holder.TaskStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.pending_status));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public int getItemCount() {
            return mList.length();
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {

            View TaskStatus;
            TextView Name, Description, Time;
            RelativeLayout Item;
            ImageView CompleteBtn;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                Item = itemView.findViewById(R.id.item);
                CompleteBtn = itemView.findViewById(R.id.complete_btn);
                TaskStatus = itemView.findViewById(R.id.task_status);
                Name = itemView.findViewById(R.id.name);
                Description = itemView.findViewById(R.id.description);
                Time = itemView.findViewById(R.id.time);
            }
        }

    }

    private void setComplete(Task task) {
        task.setIsCompleted(1);
        // todo set completed
        Toast.makeText(getApplicationContext(), "Task completed!", Toast.LENGTH_SHORT).show();
        getTasks();
    }
}