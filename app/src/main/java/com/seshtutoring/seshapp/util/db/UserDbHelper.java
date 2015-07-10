package com.seshtutoring.seshapp.util.db;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.db.DbSchema.UserTable;

/**
 * Created by nadavhollander on 7/6/15
 *
 * KEEP IN MIND: ALL OPERATIONS BELOW ARE BLOCKING.  EXECUTE IN ASYNCTASK OR SOMETHING SIMILAR.
 */
public class UserDbHelper {
    private static final String TAG = UserDbHelper.class.getName();

    private DbOpenHelper dbOpenHelper;

    public UserDbHelper(Context context) {
        this.dbOpenHelper = new DbOpenHelper(context);
    }

    // only implementing basic db fill-in for now
    public long createUser(User user)
        throws SQLiteException {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserTable.COLUMN_NAME_USER_ID, user.getUserId());
        values.put(UserTable.COLUMN_NAME_EMAIL, user.getEmail());
        values.put(UserTable.COLUMN_NAME_SESSION_ID, user.getSessionId());
        values.put(UserTable.COLUMN_NAME_FULL_NAME, user.getFullName());

        Log.i(TAG, "Inserting new User into Users table");
        long id = db.insert(UserTable.TABLE_NAME, null, values);

        if (id  == -1) {
            Log.e(TAG, "User database insertion failed.");
            throw new SQLiteException();
        }

        Log.i(TAG, "User w/ userId " + user.getUserId() + " succesfully inserted to database.");

        db.close();
        return id;
    }

    public long createOrUpdateUser(User user) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int numberOfUsersInTableWithMatchingId = userCount(user.getUserId(), db);
        if (numberOfUsersInTableWithMatchingId == 0) {

        } else if (numberOfUsersInTableWithMatchingId > 1) {

        } else {

        }


        Cursor cursor = db.rawQuery(query, new String[] { Integer.toString(user.getUserId()) });
    }

    public User getCurrentUser() {
        User currentUser;

        int userId;
        String email;
        String sessionId;
        String fullName;

        String query = "SELECT * FROM " + UserTable.TABLE_NAME;
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
           userId = cursor.getInt(cursor.getColumnIndex(UserTable.COLUMN_NAME_USER_ID));
            email = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_EMAIL));
            sessionId = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_SESSION_ID));
            fullName = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_FULL_NAME));
            currentUser = new User(userId, email, sessionId, fullName);
        } else {
            Log.e(TAG, "No user found in Users table.");
            currentUser = null;
        }

        db.close();
        cursor.close();

        return currentUser;
    }

    public int userCount(int userId, SQLiteDatabase db) {
        String countQuery = "SELECT COUNT(*) FROM " + UserTable.TABLE_NAME + " WHERE " +
                UserTable.COLUMN_NAME_USER_ID + "=?";
        Cursor cursor = db.rawQuery(countQuery, new String[] {Integer.toString(userId)});
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        return rowCount;
    }

    public void deleteAllUsers() {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.delete(UserTable.TABLE_NAME, null, null);
        db.close();

        Log.i(TAG, "Deleted all user info from Users table.");
    }
}
