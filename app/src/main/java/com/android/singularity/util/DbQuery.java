package com.android.singularity.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.content.ContextCompat;

public class DbQuery {
    public  SQLiteDatabase mRef;
    Context mContext;

    public DbQuery(Context context){
        mContext = context;
    }

    public void createOrOpenDatabase(){
        mRef = mContext.openOrCreateDatabase("singularity", Context.MODE_PRIVATE, null);
    }

    public void createTasksTableIfNotExists(){
        mRef.execSQL("CREATE TABLE IF NOT EXISTS tasks(task_id VARCHAR, " +
                "task_name VARCHAR, " +
                "task_date VARCHAR, " +
                "task_time VARCHAR, " +
                "description VARCHAR, " +
                "is_notified int, " +
                "is_completed int, " +
                "date_time DATETIME);");
    }
}
