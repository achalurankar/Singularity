package com.android.singularity.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.singularity.modal.Task;

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
                "task_type int, " +
                "date_time DATETIME DEFAULT (datetime('now','localtime')));");
    }

    // get tasks for a input date
    public List<Task> getTasks(int type) {
        String[] args = new String[] { String.valueOf(type) };
        Cursor cursor;
        if(type == Constants.TYPE_NOTE) {
            cursor = ref.rawQuery("SELECT * from tasks WHERE task_type = ? ORDER BY task_id;", args);
        } else
            cursor = ref.rawQuery("SELECT * from tasks WHERE task_type = ? ORDER BY is_completed, date_time;", args);
        cursor.moveToFirst();
        List<Task> tasks = new ArrayList<>();
        if (cursor.getCount() != 0) {
            int id, taskType;
            String name, desc, date, time;
            int isNotified, isCompleted;
            for (int i = 0; i < cursor.getCount(); i++) {
                id = cursor.getInt(0);
                name = cursor.getString(1);
                date = cursor.getString(2);
                time = cursor.getString(3);
                desc = cursor.getString(4);
                isNotified = cursor.getInt(5);
                isCompleted = cursor.getInt(6);
                taskType = cursor.getInt(7);
                Task task = new Task(taskType, id, name, date, time, desc, isNotified, isCompleted);
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
        if(cursor.getCount() == 0)  return null;

        int id, taskType;
        String name, desc, date, time;
        int isNotified, isCompleted;
        id = cursor.getInt(0);
        name = cursor.getString(1);
        date = cursor.getString(2);
        time = cursor.getString(3);
        desc = cursor.getString(4);
        isNotified = cursor.getInt(5);
        isCompleted = cursor.getInt(6);
        taskType = cursor.getInt(7);
        return new Task(taskType, id, name, date, time, desc, isNotified, isCompleted);
    }

    public int upsertTask(Task task) {
        //setting row values
        boolean isUpdate = false;
        ContentValues rows = new ContentValues();
        if(task.getId() != 0) {
            isUpdate = true;
        }
        rows.put("task_name", task.getName());
        rows.put("description", task.getDescription());
        rows.put("task_type", task.getTaskType());
        if(task.getTaskType() == Constants.TYPE_ALERT) {
            rows.put("task_date", task.getDate());
            rows.put("task_time", task.getTime());
            rows.put("is_notified", task.getIsNotified());
            rows.put("is_completed", task.getIsCompleted());
            String dateTimeValue = DateTime.getDateTimeValue(task.getDate(), task.getTime());
            rows.put("date_time", dateTimeValue);
        }

        if(isUpdate) {
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
