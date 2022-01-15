package com.android.singularity.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;
import com.android.singularity.util.EventDispatcher;
import com.andromeda.callouts.CalloutManager;
import com.andromeda.callouts.Session;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskList extends AppCompatActivity {

    private static final String TAG = "TaskList";
    RecyclerView mRecyclerView;
    TextView DateTV, DayTV;
    static JSONObject selectedTask;
    CustomAdapter mAdapter;
    LinearLayout NoResultsLayout;
    JSONArray mList;

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
        String date = new DateTime().getDateForUser();
        setDayDate(date);
        getTasks();
        //add database change listener
        EventDispatcher.addEventListener(this::getTasks);
    }

    public void setRecyclerViewAdapter(JSONArray jsonArray) {
        mAdapter = new CustomAdapter(TaskList.this, jsonArray);
        mRecyclerView.setAdapter(mAdapter);
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
        CalloutManager.makeCall(Constants.API_ENDPOINT, "GET", new JSONObject(), response -> {
            if (response == null) return;
            try {
                JSONArray jsonArray = new JSONArray(response);
                mList = jsonArray;
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

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Remove swiped item
                int index = viewHolder.getLayoutPosition();
                JSONObject item = mAdapter.get(index);
                mAdapter.remove(index);
                mAdapter.notifyDataSetChanged();
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
                    mAdapter.notifyDataSetChanged();
                });
                snackbar.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void removeFromDatabase(JSONObject item) {
        JSONObject requestStructure = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            requestStructure.put("id", item.getString("Id"));
            requestStructure.put("action", "delete");
            params.put("requestStructure", requestStructure.toString());
            CalloutManager.makeCall(Constants.API_ENDPOINT, "POST", params, response -> {
                if(response != null)
                    getTasks();
                else
                    TaskList.this.runOnUiThread(() -> Toast.makeText(TaskList.this, "Something went wrong", Toast.LENGTH_SHORT).show());
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
        public final Context mContext;
        JSONArray mList;

        public CustomAdapter(Context context, JSONArray list) {
            mContext = context;
            mList = list;
        }

        public JSONObject get(int index) {
            JSONObject object = null;
            try {
                object = this.mList.getJSONObject(index);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }

        public void remove(int index) {
            mList.remove(index);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void add(int index, JSONObject item) {
            JSONArray jsonArray = new JSONArray();
            if(index == mList.length()) {
                mList.put(item);
                notifyDataSetChanged();
                return;
            }
            for (int i = 0; i < mList.length(); i++) {
                if(i == index) {
                    jsonArray.put(item);
                }
                try {
                    jsonArray.put(mList.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mList = jsonArray;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.tasks_item, parent, false);
            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
            JSONObject task;
            try {
                task = mList.getJSONObject(position);
                holder.Name.setText(task.getString("Name"));
                try {
                    holder.Description.setText(task.getString("Description__c"));
                } catch (JSONException e) {
                    holder.Description.setText("No description provided");
                }
                holder.Item.setOnClickListener(v -> {
                    selectedTask = task;
                    startActivity(new Intent(getApplicationContext(), TaskEditor.class));
                    overridePendingTransition(0, 0);
                });
                String tt = task.getString("Display_Date_Time__c");
                String[] tt_arr = tt.split(" ");
                tt = tt_arr[0] + " " + tt_arr[1] + " " + tt_arr[2] + "\n     " + tt_arr[3];
                holder.Time.setText(tt);
                holder.CompleteBtn.setOnClickListener(v -> setComplete(task));
                //check status of task
                if (task.getBoolean("Is_Completed__c")) {
                    holder.CompleteBtn.setVisibility(View.INVISIBLE);
                    holder.TaskStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.completed_status));
                } else
                    holder.TaskStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.pending_status));
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

    private void setComplete(JSONObject task) {
        try {
            task.put("Is_Completed__c", true);
            // todo set completed
//            Toast.makeText(getApplicationContext(), "Task completed!", Toast.LENGTH_SHORT).show();
//            getTasks();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}