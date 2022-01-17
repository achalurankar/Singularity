package com.android.singularity.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.singularity.R;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;
import com.android.singularity.util.EventDispatcher;
import com.android.singularity.util.ListAdapter;
import com.android.singularity.util.Loader;
import com.andromeda.callouts.CalloutManager;
import com.andromeda.callouts.Session;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskList extends AppCompatActivity implements ListAdapter.OnItemClickListener {

    private static final String TAG = "TaskList";
    RecyclerView mRecyclerView;
    TextView DateTV, DayTV;
    static JSONObject selectedTask;
    ListAdapter mAdapter;
    LinearLayout NoResultsLayout;
    JSONArray mList;
    RelativeLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Session.storeAccessToken(Constants.ACCESS_TOKEN_ENDPOINT);
        DateTV = findViewById(R.id.date);
        mContainer = findViewById(R.id.container);
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

    @Override
    public void onClick(JSONObject clickedItem) {
        selectedTask = clickedItem;
        startActivity(new Intent(getApplicationContext(), TaskEditor.class));
        overridePendingTransition(0, 0);
    }

    public void setRecyclerViewAdapter(JSONArray jsonArray) {
        mAdapter = new ListAdapter(TaskList.this, jsonArray);
        mAdapter.addOnItemClickListener(TaskList.this);
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
        Loader.toggleLoading(this, R.id.loader, R.id.container);
        NoResultsLayout.setVisibility(View.INVISIBLE);
        findViewById(R.id.loader).setVisibility(View.VISIBLE);
        CalloutManager.makeCall(Constants.API_ENDPOINT, "GET", new JSONObject(), new CalloutManager.ResponseListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    mList = jsonArray;
                    TaskList.this.runOnUiThread(() -> {
                        Loader.toggleLoading(TaskList.this, R.id.loader, R.id.container);
                        if (jsonArray.length() != 0) {
                            setRecyclerViewAdapter(jsonArray);
                        } else {
                            NoResultsLayout.setVisibility(View.VISIBLE);
                            setRecyclerViewAdapter(new JSONArray());
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(TaskList.this, error, Toast.LENGTH_SHORT).show();
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
            CalloutManager.makeCall(Constants.API_ENDPOINT, "POST", params, new CalloutManager.ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    TaskList.this.runOnUiThread(() -> getTasks());
                }

                @Override
                public void onError(String error) {
                    TaskList.this.runOnUiThread(() -> Toast.makeText(TaskList.this, error, Toast.LENGTH_SHORT).show());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
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