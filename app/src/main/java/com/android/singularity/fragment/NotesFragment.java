package com.android.singularity.fragment;

import android.content.ContentValues;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.singularity.R;
import com.android.singularity.adapter.TaskAdapter;
import com.android.singularity.modal.Task;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DbQuery;
import com.android.singularity.util.EventDispatcher;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotesFragment  extends Fragment {

    private static final String TAG = "TasksFragment";
    // view variables
    RecyclerView mRecyclerView;
    public TaskAdapter mAdapter;
    List<Task> mList = new ArrayList<>();
    LinearLayout NoResultsLayout;
    // db handler
    DbQuery dbQuery;

    public NotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        dbQuery = new DbQuery(getActivity());
        NoResultsLayout = view.findViewById(R.id.no_result_layout);
        // recycler view
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setSwipeCallback();
        getNotes();
        //add database change listener
        EventDispatcher.addEventListener(this::getNotes);
        return view;
    }

    public void getNotes() {
        NoResultsLayout.setVisibility(View.INVISIBLE);
        mList = dbQuery.getTasks(Constants.TYPE_NOTE);
        if (mList.size() == 0) {
            NoResultsLayout.setVisibility(View.VISIBLE);
        }
        configureAdapter();
    }

    public void configureAdapter() {
        mAdapter = new TaskAdapter(getActivity(), mList, Constants.TYPE_NOTE);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setSwipeCallback() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition(); // get from position
                int toPosition = target.getAdapterPosition(); // get to position
                Collections.swap(mList, fromPosition, toPosition);
                mAdapter.notifyItemMoved(fromPosition, toPosition);
                // processing backend swap in new thread to reduce lag while rendering newly sequenced items
                new Thread(() -> {
                    int fromId, toId;
                    // get those object from position
                    Task fromObj = mList.get(fromPosition);
                    Task toObj = mList.get(toPosition);
                    // swap ids of from and to object
                    fromId = fromObj.getId();
                    toId = toObj.getId();
                    fromObj.setId(toId);
                    toObj.setId(fromId);
                    // delete and update new tasks with new sequence
                    DbQuery dbQuery = new DbQuery(getActivity());
                    dbQuery.deleteTask(fromId);
                    dbQuery.deleteTask(toId);
                    dbQuery.insertNote(fromObj);
                    dbQuery.insertNote(toObj);
                }).start();
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
        dbQuery.deleteTask(item.getId());
    }
}