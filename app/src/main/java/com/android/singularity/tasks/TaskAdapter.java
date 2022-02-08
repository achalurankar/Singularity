package com.android.singularity.tasks;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.singularity.R;
import com.android.singularity.activity.ParentActivity;
import com.android.singularity.util.Constants;
import com.android.singularity.util.DateTime;

import java.util.List;

@SuppressLint("NotifyDataSetChanged")
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.CustomViewHolder> {

    public FragmentActivity mContext;
    List<Task> list;
    static int fragmentType;

    public TaskAdapter(FragmentActivity context, List<Task> list, int type) {
        mContext = context;
        this.list = list;
        fragmentType = type;
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
        View v;
        if (fragmentType == Constants.TYPE_ALERT)
            v = LayoutInflater.from(mContext).inflate(R.layout.tasks_item, parent, false);
        else
            v = LayoutInflater.from(mContext).inflate(R.layout.notes_item, parent, false);
        return new CustomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
        Task task = list.get(position);
        holder.Name.setText(task.getName());
        holder.Description.setText(task.getDescription().length() == 0 ? "No description provided" : task.getDescription());
        holder.Item.setOnClickListener(v -> {
            if (ParentActivity.itemClickListener != null) {
                ParentActivity.itemClickListener.onClick(task);
            }
        });

        if (fragmentType == Constants.TYPE_NOTE) {
            return;
        }
        String tt = task.getTime();
        String[] tt_arr = tt.split(":");
        String task_med = tt_arr[2];
        String time = tt_arr[0] + ":" + tt_arr[1] + " " + task_med; // time
        String date = DateTime.getDisplayDate(task.getDate()); // date
        holder.Time.setText(time);
        if(task.getFrequency() != Constants.ONE_TIME) {
            date = Constants.frequencyOptions[task.getFrequency() - 1];
        }
        holder.Date.setText(date);
        //check status of task
        int isCompleted = task.getIsCompleted();
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

    public static class CustomViewHolder extends RecyclerView.ViewHolder {

        View TaskStatus;
        TextView Name, Description, Time, Date;
        RelativeLayout Item;
        LinearLayout TimeLayout;
        ImageView CompleteBtn;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            Item = itemView.findViewById(R.id.item);
            Name = itemView.findViewById(R.id.name);
            Description = itemView.findViewById(R.id.description);
            CompleteBtn = itemView.findViewById(R.id.complete_btn);
            if (fragmentType != Constants.TYPE_NOTE) {
                TimeLayout = itemView.findViewById(R.id.time_layout);
                TaskStatus = itemView.findViewById(R.id.task_status);
                Time = itemView.findViewById(R.id.time);
                Date = itemView.findViewById(R.id.date);
            }
        }
    }

}
