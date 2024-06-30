package com.example.val_info.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "Val-Info.DB";
    private static final int DB_VERSION = 1;
    public static final String KEY_ID = "KEY_ID";

    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }
}
