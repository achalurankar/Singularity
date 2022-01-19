package com.android.singularity.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
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
        DateTV = findViewById(R.id.date);
        DayTV = findViewById(R.id.day);
        NoResultsLayout = findViewById(R.id.no_result_layout);
        configureRecyclerView();
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

    private void configureRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(new CustomAdapter(TaskList.this, mList));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(TaskList.this));
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
            mAdapter = new CustomAdapter(TaskList.this, mList);
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
                mList.remove(index);
                Snackbar snackbar = Snackbar.make(mRecyclerView, "Task removed!", 2500);
                Handler handler = new Handler();
                Runnable runnable = () -> {
                    snackbar.dismiss();
                    removeFromDatabase(item);
                };
                handler.postDelayed(runnable, 2600);
                snackbar.setAction("UNDO", v -> {
                    handler.removeCallbacks(runnable);
                    mList.add(index, item);
                    configureRecyclerView();
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

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
        public final Context mContext;
        List<Task> mList;
        DateTime date;

        public CustomAdapter(Context context, List<Task> list) {
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
            Task task = mList.get(position);
            holder.Name.setText(task.getName());
            holder.Description.setText(task.getDescription().length() == 0 ? "No description provided" : task.getDescription());
            int isCompleted = task.getIsCompleted();
            int isNotified = task.getIsNotified();
            holder.Item.setOnClickListener(v -> {
                selectedTask = task;
                CurrentDateForEditor = DateTV.getText().toString();
                startActivity(new Intent(getApplicationContext(), TaskEditor.class));
                overridePendingTransition(0, 0);
            });
            String tt = task.getTime();
            String[] tt_arr = tt.split(":");
            int task_hour = Integer.parseInt(tt_arr[0]);
            int task_min = Integer.parseInt(tt_arr[1]);
            String task_med = tt_arr[2];
            holder.Time.setText(tt_arr[0] + ":" + tt_arr[1] + " " + task_med);
            holder.CompleteBtn.setOnClickListener(v -> setComplete(task));
            //check status of task
            if (isCompleted == 1) {
                holder.CompleteBtn.setVisibility(View.INVISIBLE);
                holder.TaskStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.completed_status));
            } else
                holder.TaskStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.pending_status));
        }

        @Override
        public int getItemCount() {
            return mList.size();
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
        dbQuery.upsertTask(task);
        Toast.makeText(getApplicationContext(), "Task completed!", Toast.LENGTH_SHORT).show();
        getTasks(task.getDate());
    }
}