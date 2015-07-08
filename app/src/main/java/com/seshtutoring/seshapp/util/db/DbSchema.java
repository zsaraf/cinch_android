package com.seshtutoring.seshapp.util.db;

import android.provider.BaseColumns;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nadavhollander on 7/6/15.
 */
public final class DbSchema {
    public DbSchema() {}

    public static abstract class UserTable implements BaseColumns {
        public static final String TABLE_NAME = "users";

        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_SESSION_ID = "session_id";
        public static final String COLUMN_NAME_FULL_NAME = "full_name";
//        public static final String COLUMN_NAME_PROFILE_PICTURE_URL = "profile_picture_url";
//        public static final String COLUMN_NAME_BIO = "bio";
//        public static final String COLUMN_NAME_STRIPE_CUSTOMER_ID = "stripe_customer_id";
//        public static final String COLUMN_NAME_CLASS_YEAR = "class_year";
//        public static final String COLUMN_NAME_MAJOR = "major";
//        public static final String COLUMN_NAME_TUTOR_OFFLINE_PING = "tutor_offline_ping";
//        public static final String COLUMN_NAME_COMPLETED_APP_TOUR = "completed_app_tour";
//        public static final String COLUMN_NAME_IS_VERIFIED = "is_verified";
//        public static final String COLUMN_NAME_FULL_LEGAL_NAME = "full_legal_name";
//        public static final String COLUMN_NAME_SHARE_CODE = "share_code";

        // text type of each column in DB
        public static final String TEXT_TYPE_USER_ID = "INT";
        public static final String TEXT_TYPE_EMAIL = "TEXT";
        public static final String TEXT_TYPE_SESSION_ID = "TEXT";
        public static final String TEXT_TYPE_FULL_NAME = "TEXT";
//        public static final String TEXT_TYPE_PROFILE_PICTURE_URL = "TEXT";
//        public static final String TEXT_TYPE_BIO = "TEXT";
//        public static final String TEXT_TYPE_STRIPE_CUSTOMER_ID = "TEXT";
//        public static final String TEXT_TYPE_CLASS_YEAR = "INT";
//        public static final String TEXT_TYPE_MAJOR = "TEXT";
//        public static final String TEXT_TYPE_TUTOR_OFFLINE_PING = "INT";
//        public static final String TEXT_TYPE_COMPLETED_APP_TOUR = "INT";
//        public static final String TEXT_TYPE_IS_VERIFIED = "INT";
//        public static final String TEXT_TYPE_FULL_LEGAL_NAME = "TEXT";
//        public static final String TEXT_TYPE_SHARE_CODE = "TEXT";
    }
}
