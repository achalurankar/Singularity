package com.android.singularity.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.singularity.R;
import com.android.singularity.activity.TaskEditor;
import com.android.singularity.adapter.EmailAdapter;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;
import com.android.singularity.util.EventDispatcher;
import com.android.singularity.util.Loader;
import com.andromeda.callouts.CalloutManager;
import com.andromeda.callouts.Session;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmailFragment extends Fragment {

    private static final String TAG = "TaskList";
    RecyclerView mRecyclerView;
    TextView DateTV, DayTV;
    public static JSONObject selectedTask;
    EmailAdapter mAdapter;
    LinearLayout NoResultsLayout;
    JSONArray mList;
    View view;

    public EmailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_email, container, false);
        if(!Session.isTokenValid)
            Session.storeAccessToken(Constants.ACCESS_TOKEN_ENDPOINT);
        DateTV = view.findViewById(R.id.date);
        DayTV = view.findViewById(R.id.day);
        NoResultsLayout = view.findViewById(R.id.no_result_layout);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setTouchCallback();
        getTasks();
        //add database change listener
        EventDispatcher.addEventListener(this::getTasks);
        return view;
    }

    void getTasks() {
        Loader.toggleLoading(view, R.id.loader, R.id.container);
        NoResultsLayout.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.loader).setVisibility(View.VISIBLE);
        CalloutManager.makeCall(Constants.API_ENDPOINT, "GET", new JSONObject(), new CalloutManager.ResponseListener() {
            @Override
            public void onSuccess(String response) {
                getActivity().runOnUiThread(() -> {
                    Loader.toggleLoading(view, R.id.loader, R.id.container);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        mList = jsonArray;
                        if (jsonArray.length() != 0) {
                            setRecyclerViewAdapter(jsonArray);
                        } else {
                            NoResultsLayout.setVisibility(View.VISIBLE);
                            setRecyclerViewAdapter(new JSONArray());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onError(String error) {
                getActivity().runOnUiThread(() -> {
                    Loader.toggleLoading(view, R.id.loader, R.id.container);
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void setRecyclerViewAdapter(JSONArray jsonArray) {
        mAdapter = new EmailAdapter(getActivity(), jsonArray);
        mRecyclerView.setAdapter(mAdapter);
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
            // no need to call get tasks as element is already removed from the view. hence no response listener required
            CalloutManager.makeCall(Constants.API_ENDPOINT, "POST", params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}