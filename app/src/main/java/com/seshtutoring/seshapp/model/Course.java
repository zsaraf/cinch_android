package com.seshtutoring.seshapp.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/22/15.
 */
public class Course {
    private static final String TAG = Course.class.getName();
    private static final String CLASS_NAME_KEY = "className";
    private static final String CLASS_NUMBER_KEY = "classNumber";
    private static final String DEPT_ABBREV_KEY = "deptAbbrev";
    private static final String CLASS_ID_KEY = "classId";

    public String className;
    public String classNumber;
    public String deptAbbrev;
    public int classId;

    public Course(String className, String classNumber, String deptAbbrev, int classId) {
        this.className = className;
        this.classNumber = classNumber;
        this.deptAbbrev = deptAbbrev;
        this.classId = classId;
    }

    public static Course fromClassRowJson(JSONObject json) {
        Course courseInstance = null;

        String classNameVal;
        String classNumberVal;
        String deptAbbrevVal;
        int classIdVal;

        try {
            classNameVal = json.getString("name");
            classNumberVal = json.getString("number");
            deptAbbrevVal = json.getJSONObject("department").getString("abbrev");
            classIdVal = json.getInt("id");
            courseInstance = new Course(classNameVal, classNumberVal, deptAbbrevVal, classIdVal);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return courseInstance;
    }

    public static Course fromJson(JSONObject json) {
        Course courseInstance = null;

        String classNameVal;
        String classNumberVal;
        String deptAbbrevVal;
        int classIdVal;

        try {
            classNameVal = json.getString(CLASS_NAME_KEY);
            classNumberVal = json.getString(CLASS_NUMBER_KEY);
            deptAbbrevVal = json.getString(DEPT_ABBREV_KEY);
            classIdVal = json.getInt(CLASS_ID_KEY);
            courseInstance = new Course(classNameVal, classNumberVal, deptAbbrevVal, classIdVal);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return courseInstance;
    }

    public String formatForTextView() {
        return deptAbbrev + " " + classNumber + ": " + className;
    }

    public String shortFormatForTextView() {
        return deptAbbrev + " " + classNumber;
    }

}
