package com.android.singularity.email;

import org.json.JSONException;
import org.json.JSONObject;

public class TaskWrapper {

    public String id;
    public String name;
    public String action;
    public String description;
    public String frequency;
    public String taskTime;
    public String displayDateTime;
    public boolean isCompleted;

    public TaskWrapper(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString("id");
            this.name = jsonObject.getString("name");
            this.description = jsonObject.getString("description");
            this.description = !this.description.equals("null") ? this.description : null;
            this.frequency = jsonObject.getString("frequency");
            this.taskTime = jsonObject.getString("taskTime");
            this.displayDateTime = jsonObject.getString("displayDateTime");
            this.isCompleted = jsonObject.getBoolean("isCompleted");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
