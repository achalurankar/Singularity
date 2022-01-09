package com.android.singularity.modal;

public class Task {
    String id, name, date, time, description;
    int isNotified, isCompleted;

    public Task() {

    }

    public Task(String id, String name, String date, String time, String description, int isNotified, int isCompleted) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.description = description;
        this.isNotified = isNotified;
        this.isCompleted = isCompleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int isNotified() {
        return isNotified;
    }

    public void setIsNotified(int isNotified) {
        this.isNotified = isNotified;
    }

    public int isCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(int isCompleted) {
        this.isCompleted = isCompleted;
    }
}
