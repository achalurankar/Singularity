package com.android.singularity.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.singularity.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class DbQuery {
    public SQLiteDatabase ref;
    Context context;

    public DbQuery(Context context) {
        this.context = context;
//        dropDatabase(); // for development, when changing schema.
        createOrOpenDatabase();
        createTasksTableIfNotExists();
    }

    private void dropDatabase() {
        context.deleteDatabase("singularity");
    }

    public void createOrOpenDatabase() {
        ref = context.openOrCreateDatabase("singularity", Context.MODE_PRIVATE, null);
    }

    public void createTasksTableIfNotExists() {
        ref.execSQL("CREATE TABLE IF NOT EXISTS tasks(" +
                "task_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "task_name VARCHAR, " +
                "task_date VARCHAR DEFAULT 'note', " +
                "task_time VARCHAR DEFAULT 'note', " +
                "description VARCHAR, " +
                "is_notified int DEFAULT 0, " +
                "is_completed int DEFAULT 0, " +
                "frequency int DEFAULT 1, " +
                "current_schedule long DEFAULT 1, " + // time in milliseconds for denoting current cycle of recurring task
                "task_type int, " +
                "date_time DATETIME DEFAULT (datetime('now','localtime')));");
    }

    // get tasks for a input date
    public List<Task> getTasks(int type) {
        String[] args = new String[]{String.valueOf(type)};
        Cursor cursor;
        if (type == Constants.TYPE_NOTE) {
            cursor = ref.rawQuery("SELECT * from tasks WHERE task_type = ? ORDER BY task_id DESC;", args);
        } else
            cursor = ref.rawQuery("SELECT * from tasks WHERE task_type = ? ORDER BY current_schedule;", args);
        cursor.moveToFirst();
        List<Task> tasks = new ArrayList<>();
        if (cursor.getCount() != 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setName(cursor.getString(1));
                task.setDate(cursor.getString(2));
                task.setTime(cursor.getString(3));
                task.setDescription(cursor.getString(4));
                task.setIsNotified(cursor.getInt(5));
                task.setIsCompleted(cursor.getInt(6));
                task.setFrequency(cursor.getInt(7));
                task.setCurrentSchedule(cursor.getLong(8));
                task.setTaskType(cursor.getInt(9));
                tasks.add(task);
                cursor.moveToNext();
            }
        }
        if (!cursor.isClosed())
            cursor.close();
        return tasks;
    }

    // get particular task
    public Task getTask(String taskId) {
        String[] args = new String[]{taskId};
        Cursor cursor = ref.rawQuery("SELECT * from tasks WHERE task_id = ?", args);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) return null;

        Task task = new Task();
        task.setId(cursor.getInt(0));
        task.setName(cursor.getString(1));
        task.setDate(cursor.getString(2));
        task.setTime(cursor.getString(3));
        task.setDescription(cursor.getString(4));
        task.setIsNotified(cursor.getInt(5));
        task.setIsCompleted(cursor.getInt(6));
        task.setFrequency(cursor.getInt(7));
        task.setCurrentSchedule(cursor.getLong(8));
        task.setTaskType(cursor.getInt(9));
        return task;
    }

    public int upsertTask(Task task) {
        //setting row values
        boolean isUpdate = false;
        ContentValues rows = new ContentValues();
        if (task.getId() != 0) {
            isUpdate = true;
        }
        rows.put("task_name", task.getName());
        rows.put("description", task.getDescription());
        rows.put("task_type", task.getTaskType());
        if (task.getTaskType() == Constants.TYPE_ALERT) {
            rows.put("task_date", task.getDate());
            rows.put("task_time", task.getTime());
            rows.put("is_notified", task.getIsNotified());
            rows.put("is_completed", task.getIsCompleted());
            rows.put("frequency", task.getFrequency());
            rows.put("current_schedule", task.getCurrentSchedule());
            String dateTimeValue = DateTime.getDateTimeValue(task.getDate(), task.getTime());
            rows.put("date_time", dateTimeValue);
        }

        if (isUpdate) {
            String[] args = new String[]{String.valueOf(task.getId())};
            ref.update("tasks", rows, "task_id = ?", args);
            return task.getId();
        } else {
            long newId = ref.insert("tasks", null, rows);
            return (int) newId;
        }
    }

    public void deleteTask(int taskId) {
        String[] args = new String[]{String.valueOf(taskId)};
        ref.delete("tasks", "task_id = ?", args);
    }

    public void insertNote(Task task) {
        ContentValues rows = new ContentValues();
        rows.put("task_id", task.getId());
        rows.put("task_name", task.getName());
        rows.put("description", task.getDescription());
        rows.put("task_type", task.getTaskType());
        ref.insert("tasks", null, rows);
    }
}
