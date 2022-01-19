package com.android.singularity.activity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.singularity.R;
import com.android.singularity.modal.Task;
import com.android.singularity.util.DateTime;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    private final TaskList taskList;
    public final Context mContext;
    List<Task> list;
    DateTime date;

    public CustomAdapter(TaskList taskList, Context context, List<Task> list) {
        this.taskList = taskList;
        mContext = context;
        this.list = list;
        date = new DateTime();
    }

    public Task get(int index) {
        return list.get(index);
    }


    public void remove(int index) {
        list.remove(index);
        notifyDataSetChanged();
    }

    public void add(int index, Task task) {
        list.add(index, task);
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
        Task task = list.get(position);
        holder.Name.setText(task.getName());
        holder.Description.setText(task.getDescription().length() == 0 ? "No description provided" : task.getDescription());
        int isCompleted = task.getIsCompleted();
        int isNotified = task.getIsNotified();
        holder.Item.setOnClickListener(v -> {
            TaskList.selectedTask = task;
            TaskList.CurrentDateForEditor = taskList.DateTV.getText().toString();
            taskList.startActivity(new Intent(taskList.getApplicationContext(), TaskEditor.class));
            taskList.overridePendingTransition(0, 0);
        });
        String tt = task.getTime();
        String[] tt_arr = tt.split(":");
        int task_hour = Integer.parseInt(tt_arr[0]);
        int task_min = Integer.parseInt(tt_arr[1]);
        String task_med = tt_arr[2];
        holder.Time.setText(tt_arr[0] + ":" + tt_arr[1] + " " + task_med);
        holder.CompleteBtn.setOnClickListener(v -> taskList.setComplete(task));
        //check status of task
        if (isCompleted == 1) {
            holder.CompleteBtn.setVisibility(View.INVISIBLE);
            holder.TaskStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.completed_status));
        } else
            holder.TaskStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.pending_status));
    }

    @Override
    public int getItemCount() {
        return list.size();
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
