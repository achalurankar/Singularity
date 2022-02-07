package com.android.singularity.modal;

public class Task {
    int id, taskType;
    String name, date, time, description;
    int frequency;
    int isNotified, isCompleted;

    public Task() {

    }

    public Task(int taskType, int frequency, int id, String name, String date, String time, String description, int isNotified, int isCompleted) {
        this.taskType = taskType;
        this.frequency = frequency;
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.description = description;
        this.isNotified = isNotified;
        this.isCompleted = isCompleted;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getIsNotified() {
        return isNotified;
    }

    public void setIsNotified(int isNotified) {
        this.isNotified = isNotified;
    }

    public int getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(int isCompleted) {
        this.isCompleted = isCompleted;
    }
}