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

    public long createUser(User user)
        throws SQLiteException {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        ContentValues values = getContentValuesForUser(user);

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
        int existingUsersWithId = userCount(user.getUserId(), db);

        ContentValues values = getContentValuesForUser(user);
        long id;

        if (existingUsersWithId == 0) {
            id = db.insert(UserTable.TABLE_NAME, null, values);
        } else if (existingUsersWithId > 1) {
            Log.e(TAG, "More than one user with id " + user.getUserId() + " in user table");
            return -1;
        } else {
            id = db.update(UserTable.TABLE_NAME, values,
                    UserTable.COLUMN_NAME_USER_ID + "=" + user.getUserId(), null);
        }
        db.close();
        return id;
    }

    public User getCurrentUser() {
        User currentUser;

        String query = "SELECT * FROM " + UserTable.TABLE_NAME;
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 1) {
            Log.e(TAG, "Big problem: more than one user in DB");
            currentUser = null;
        } else if (cursor.getCount() < 1){
            Log.i(TAG, "No user found in Users table.");
            currentUser = null;
        } else {
            currentUser = getUserForCursor(cursor);
        }

        db.close();
        cursor.close();

        return currentUser;
    }

    private int userCount(int userId, SQLiteDatabase db) {
        String countQuery = "SELECT COUNT(*) FROM " + UserTable.TABLE_NAME + " WHERE " +
                UserTable.COLUMN_NAME_USER_ID + "=?";
        Cursor cursor = db.rawQuery(countQuery, new String[] {Integer.toString(userId)});
        cursor.moveToFirst();
        int rowCount = cursor.getInt(0);
        cursor.close();

        return rowCount;
    }

    public void deleteAllUsers() {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.delete(UserTable.TABLE_NAME, null, null);
        db.close();

        Log.i(TAG, "Deleted all user info from Users table.");
    }

    public ContentValues getContentValuesForUser(User user) {
        ContentValues values = new ContentValues();
        values.put(UserTable.COLUMN_NAME_USER_ID, user.getUserId());
        values.put(UserTable.COLUMN_NAME_EMAIL, user.getEmail());
        values.put(UserTable.COLUMN_NAME_SESSION_ID, user.getSessionId());
        values.put(UserTable.COLUMN_NAME_FULL_NAME, user.getFullName());
        values.put(UserTable.COLUMN_NAME_PROFILE_PICTURE_URL, user.getProfilePictureUrl());
        values.put(UserTable.COLUMN_NAME_BIO, user.getBio());
        values.put(UserTable.COLUMN_NAME_STRIPE_CUSTOMER_ID, user.getStripeCustomerId());
        values.put(UserTable.COLUMN_NAME_MAJOR, user.getMajor());
        values.put(UserTable.COLUMN_NAME_TUTOR_OFFLINE_PING, user.isTutorOfflinePing());
        values.put(UserTable.COLUMN_NAME_COMPLETED_APP_TOUR, user.completedAppTour());
        values.put(UserTable.COLUMN_NAME_IS_VERIFIED, user.isVerified());
        values.put(UserTable.COLUMN_NAME_FULL_LEGAL_NAME, user.getFullLegalName());
        values.put(UserTable.COLUMN_NAME_SHARE_CODE, user.getShareCode());
        return values;
    }

    public User getUserForCursor(Cursor cursor) {
        int userId = cursor.getInt(cursor.getColumnIndex(UserTable.COLUMN_NAME_USER_ID));
        String email = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_EMAIL));
        String sessionId = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_SESSION_ID));
        String fullName = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_FULL_NAME));
        String profilePictureUrl = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_PROFILE_PICTURE_URL));
        String bio = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_BIO));
        String stripeCustomerId = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_STRIPE_CUSTOMER_ID));
        String major = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_MAJOR));
        boolean tutorOfflinePing =
                (cursor.getInt(cursor.getColumnIndex(UserTable.COLUMN_NAME_TUTOR_OFFLINE_PING)) == 1) ? true : false;
        boolean completedAppTour = (cursor.getInt(cursor.getColumnIndex(UserTable.COLUMN_NAME_COMPLETED_APP_TOUR)) == 1) ? true : false;
        boolean isVerified = (cursor.getInt(cursor.getColumnIndex(UserTable.COLUMN_NAME_IS_VERIFIED)) == 1) ? true : false;
        String fullLegalName = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_FULL_LEGAL_NAME));
        String shareCode = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_NAME_SHARE_CODE));

        return new User(userId, email, sessionId, fullName, profilePictureUrl, bio,
                stripeCustomerId, major, tutorOfflinePing, completedAppTour, isVerified,
                fullLegalName, shareCode);
    }
}
