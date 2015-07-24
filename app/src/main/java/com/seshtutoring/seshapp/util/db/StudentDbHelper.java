package com.seshtutoring.seshapp.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.seshtutoring.seshapp.model.Student;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.db.schema.DbOpenHelper;
import com.seshtutoring.seshapp.util.db.schema.DbSchema;

/**
 * Created by nadavhollander on 7/24/15.
 */
public class StudentDbHelper {
    private static final String TAG = StudentDbHelper.class.getName();

    private DbOpenHelper dbOpenHelper;

    public StudentDbHelper(Context context) {
        this.dbOpenHelper = new DbOpenHelper(context);
    }

}
