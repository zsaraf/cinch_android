package com.seshtutoring.seshapp.util.db;

import android.database.sqlite.*;
import android.content.Context;


/**
 * Created by nadavhollander on 7/6/15.
 */
public class DbOpenHelper extends SQLiteOpenHelper {
    // DB Version must be incremented anytime schema is changed
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "SeshApp.db";

    private static final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + DbSchema.UserTable.TABLE_NAME + " (" +
            DbSchema.UserTable._ID + " INTEGER PRIMARY KEY," +
            DbSchema.UserTable.COLUMN_NAME_USER_ID + " " + DbSchema.UserTable.TEXT_TYPE_USER_ID + ", " +
            DbSchema.UserTable.COLUMN_NAME_EMAIL + " " + DbSchema.UserTable.TEXT_TYPE_EMAIL + ", " +
            DbSchema.UserTable.COLUMN_NAME_SESSION_ID + " " + DbSchema.UserTable.TEXT_TYPE_SESSION_ID + ", " +
            DbSchema.UserTable.COLUMN_NAME_FULL_NAME + " " + DbSchema.UserTable.TEXT_TYPE_FULL_NAME + ", " +
            DbSchema.UserTable.COLUMN_NAME_PROFILE_PICTURE_URL + " " + DbSchema.UserTable.TEXT_TYPE_PROFILE_PICTURE_URL + ", " +
            DbSchema.UserTable.COLUMN_NAME_BIO + " " + DbSchema.UserTable.TEXT_TYPE_BIO + ", " +
            DbSchema.UserTable.COLUMN_NAME_STRIPE_CUSTOMER_ID + " " + DbSchema.UserTable.TEXT_TYPE_STRIPE_CUSTOMER_ID + ", " +
            DbSchema.UserTable.COLUMN_NAME_MAJOR + " " + DbSchema.UserTable.TEXT_TYPE_MAJOR + ", " +
            DbSchema.UserTable.COLUMN_NAME_TUTOR_OFFLINE_PING + " " + DbSchema.UserTable.TEXT_TYPE_TUTOR_OFFLINE_PING + ", " +
            DbSchema.UserTable.COLUMN_NAME_COMPLETED_APP_TOUR + " " + DbSchema.UserTable.TEXT_TYPE_COMPLETED_APP_TOUR + ", " +
            DbSchema.UserTable.COLUMN_NAME_IS_VERIFIED + " " + DbSchema.UserTable.TEXT_TYPE_IS_VERIFIED + ", " +
            DbSchema.UserTable.COLUMN_NAME_FULL_LEGAL_NAME + " " + DbSchema.UserTable.TEXT_TYPE_FULL_LEGAL_NAME + ", " +
            DbSchema.UserTable.COLUMN_NAME_SHARE_CODE + " " + DbSchema.UserTable.TEXT_TYPE_SHARE_CODE + ")";

    private static final String SQL_DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DROP_TABLE_IF_EXISTS + DbSchema.UserTable.TABLE_NAME);
        onCreate(db);
    }

}
