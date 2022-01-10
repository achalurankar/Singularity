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
        createOrOpenDatabase();
        createTasksTableIfNotExists();
    }

    public void createOrOpenDatabase() {
        ref = context.openOrCreateDatabase("singularity", Context.MODE_PRIVATE, null);
    }

    public void createTasksTableIfNotExists() {
        ref.execSQL("CREATE TABLE IF NOT EXISTS tasks(task_id VARCHAR, " +
                "task_name VARCHAR, " +
                "task_date VARCHAR, " +
                "task_time VARCHAR, " +
                "description VARCHAR, " +
                "is_notified int, " +
                "is_completed int, " +
                "date_time DATETIME);");
    }

    // get tasks for a input date
    public List<Task> getTasks(String inputDate) {
        String[] args = new String[]{inputDate};
        Cursor cursor = ref.rawQuery("SELECT * from tasks WHERE task_date = ? ORDER BY is_completed, date_time;", args);
        cursor.moveToFirst();
        List<Task> tasks = new ArrayList<>();
        if (cursor.getCount() != 0) {
            String id, name, desc, date, time;
            int isNotified, isCompleted;
            for (int i = 0; i < cursor.getCount(); i++) {
                id = cursor.getString(0);
                name = cursor.getString(1);
                date = cursor.getString(2);
                time = cursor.getString(3);
                desc = cursor.getString(4);
                isNotified = cursor.getInt(5);
                isCompleted = cursor.getInt(6);
                Task task = new Task(id, name, date, time, desc, isNotified, isCompleted);
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

        String id, name, desc, date, time;
        int isNotified, isCompleted;
        id = cursor.getString(0);
        name = cursor.getString(1);
        date = cursor.getString(2);
        time = cursor.getString(3);
        desc = cursor.getString(4);
        isNotified = cursor.getInt(5);
        isCompleted = cursor.getInt(6);
        return new Task(id, name, date, time, desc, isNotified, isCompleted);
    }

    public static final int UPDATE = 0;
    public static final int INSERT = 1;

    public int upsertTask(Task task) {
        //setting row values
        boolean isUpdate = true;
        if(task.getId() == null) {
            task.setId(System.currentTimeMillis() + "");
            isUpdate = false;
        }
        ContentValues rows = new ContentValues();
        rows.put("task_id", task.getId());
        rows.put("task_name", task.getName());
        rows.put("task_date", task.getDate());
        rows.put("task_time", task.getTime());
        rows.put("description", task.getDescription());
        rows.put("is_notified", task.isNotified());
        rows.put("is_completed", task.isCompleted());
        String dateTimeValue = DateTime.getDateTimeValue(task.getDate(), task.getTime());
        rows.put("date_time", dateTimeValue);

        if(isUpdate) {
            String[] args = new String[]{task.getId()};
            ref.update("tasks", rows, "task_id = ?", args);
            return UPDATE;
        } else {
            ref.insert("tasks", null, rows);
            return INSERT;
        }
    }

    public void deleteTask(Task task) {
        String[] args = new String[]{task.getId()};
        ref.delete("tasks", "task_id = ?", args);
    }
}
