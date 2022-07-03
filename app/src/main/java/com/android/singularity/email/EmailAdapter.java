package com.android.singularity.email;


import android.annotation.SuppressLint;
import android.content.Context;
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
import com.android.singularity.activity.ParentActivity;
import com.android.singularity.util.DateTime;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.CustomViewHolder> {
    public final Context mContext;
    List<TaskWrapper> mList;

    public EmailAdapter(Context context, List<TaskWrapper> list) {
        mContext = context;
        mList = list;
    }

    public TaskWrapper get(int index) {
        TaskWrapper object = null;
        object = this.mList.get(index);
        return object;
    }

    public void remove(int index) {
        mList.remove(index);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void add(int index, TaskWrapper item) {
        List<TaskWrapper> jsonArray = new ArrayList<>();
        if (index == mList.size()) {
            mList.add(item);
            notifyDataSetChanged();
            return;
        }
        for (int i = 0; i < mList.size(); i++) {
            if (i == index) {
                jsonArray.add(item);
            }
            jsonArray.add(mList.get(i));
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
        TaskWrapper task;
        task = mList.get(position);
        holder.Name.setText(task.name);

        holder.Description.setText(task.description == null ? "No description provided" : task.description);

        holder.Item.setOnClickListener(v -> {
            ParentActivity.itemClickListener.OnWrapperObjectClick(task);
        });
        String tt = task.displayDateTime;
        String[] tt_arr = tt.split(" ");
        switch (task.frequency) {
            case "One time":
                tt = tt_arr[0] + " " + tt_arr[1] + " " + tt_arr[2];
                break;
            case "Daily":
            case "Monthly":
                tt = task.frequency;
                break;
            case "Weekly":
                tt = DateTime.getDayOfWeek(tt_arr);
                break;
        }
        tt += "\n" + DateTime.get12HrFormatTime(tt_arr[3]);
        holder.Time.setText(tt);
//            holder.CompleteBtn.setOnClickListener(v -> setComplete(task));
        //check status of task
        if (task.isCompleted) {
            holder.CompleteBtn.setVisibility(View.INVISIBLE);
            holder.TaskStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.completed_status));
        } else
            holder.TaskStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.pending_status));


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public static class CustomViewHolder extends RecyclerView.ViewHolder {

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
