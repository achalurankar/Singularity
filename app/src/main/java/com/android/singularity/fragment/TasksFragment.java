package com.android.singularity.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.singularity.R;
import com.android.singularity.adapter.TaskAdapter;
import com.android.singularity.main.ParentActivity;
import com.android.singularity.modal.Task;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DbQuery;
import com.android.singularity.util.EventDispatcher;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private static final String TAG = "TasksFragment";
    private int FragmentType;
    // view variables
    RecyclerView mRecyclerView;
    public TaskAdapter mAdapter;
    List<Task> mList = new ArrayList<>();
    LinearLayout NoResultsLayout;
    // db handler
    DbQuery dbQuery;

    public TasksFragment() {
        // Required empty public constructor
    }

    public TasksFragment(int type) {
        FragmentType = type;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        dbQuery = new DbQuery(getActivity());
        NoResultsLayout = view.findViewById(R.id.no_result_layout);
        // recycler view
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setSwipeCallback();
        getTasks();
        //add database change listener
        EventDispatcher.addEventListener(this::getTasks);
        if(FragmentType == Constants.TYPE_NOTE) {
            ((TextView)view.findViewById(R.id.no_result_message)).setText("No notes added");
        }
        if(FragmentType == Constants.TYPE_ALERT) {
            ((TextView)view.findViewById(R.id.no_result_message)).setText("No alerts added");
        }
        return view;
    }

    public void getTasks() {
        NoResultsLayout.setVisibility(View.INVISIBLE);
        mList = dbQuery.getTasks(FragmentType);
        Log.e(TAG, "getTasks: type - " + FragmentType + " size = " + mList.size());
        if (mList.size() == 0) {
            NoResultsLayout.setVisibility(View.VISIBLE);
        }
        configureAdapter();
    }

    public void configureAdapter() {
        mAdapter = new TaskAdapter(getActivity(), mList, FragmentType);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setSwipeCallback() {
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
                Snackbar snackbar = Snackbar.make(mRecyclerView, "Task removed!", 1900);
                Handler handler = new Handler();
                Runnable runnable = () -> {
                    removeFromDatabase(item);
                };
                handler.postDelayed(runnable, 2000);
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
}