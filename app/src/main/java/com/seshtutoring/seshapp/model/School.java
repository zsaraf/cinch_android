package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by zacharysaraf on 8/24/15.
 */
public class School extends SugarRecord<School> {
    @Ignore
    private static final String TAG = User.class.getName();

    public String emailDomain;
    public String schoolName;
    public Boolean enabled;
    public Boolean requestsEnabled;
    public int linePosition;
    public int schoolId;

    // empty constructor necessary for SugarORM to work
    public School() {}

    public School(String schoolName, int schoolId, int linePosition, Boolean enabled, String emailDomain, Boolean requestsEnabled) {
        this.schoolId = schoolId;
        this.linePosition = linePosition;
        this.enabled = enabled;
        this.emailDomain = emailDomain;
        this.schoolName = schoolName;
        this.requestsEnabled = requestsEnabled;
    }

    public static School createOrUpdateSchoolWithObject(JSONObject schoolRow) {
        School school = null;
        try {
            int schoolId = schoolRow.getInt("id");

            if (School.listAll(School.class).size() > 0) {
                List<School> schoolsFound = School.find(School.class, "school_id = ?", Integer.toString(schoolId));
                if (schoolsFound.size() > 0) {
                    school = schoolsFound.get(0);
                } else {
                    school = new School();
                }
            } else {
                school = new School();
            }

            school.schoolId = schoolId;
            school.schoolName = schoolRow.getString("name");
            school.emailDomain = schoolRow.getString("email_domain");
            school.enabled = schoolRow.getInt("enabled") == 1;
            school.linePosition = schoolRow.getInt("line_position");
            school.requestsEnabled = schoolRow.getInt("requests_enabled") == 1;

            school.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update school in db; JSON school object from server is malformed: " + e);
            return null;
        }
        return school;
    }
}
