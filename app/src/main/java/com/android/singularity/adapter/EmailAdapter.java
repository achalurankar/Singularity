package com.android.singularity.adapter;


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
import com.android.singularity.main.ParentActivity;
import com.android.singularity.modal.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.CustomViewHolder> {
    public final Context mContext;
    JSONArray mList;

    public EmailAdapter(Context context, JSONArray list) {
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
                ParentActivity.itemClickListener.OnJSONObjectClick(task);
            });
            String tt = task.getString("Display_Date_Time__c");
            String[] tt_arr = tt.split(" ");
            tt = tt_arr[0] + " " + tt_arr[1] + " " + tt_arr[2] + "\n     " + tt_arr[3];
            holder.Time.setText(tt);
//            holder.CompleteBtn.setOnClickListener(v -> setComplete(task));
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
