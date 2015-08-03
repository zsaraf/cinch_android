package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by nadavhollander on 7/25/15.
 */
public class Sesh extends SugarRecord<Sesh> {
    @Ignore
    private static final String TAG = Sesh.class.getName();

    public String class_name;
    public boolean has_been_seen;
    public boolean has_started;
    public boolean is_student;
    public double latitude;
    public String locationNotes;
    public double longitude;
    public int past_request_id;
    public String sesh_description;
    public int sesh_est_time;
    public int sesh_id;
    public int sesh_num_students;
    public Date sesh_set_time;
    public Date start_time;
    public double tutor_latitude;
    public double tutor_longitude;
    public String user_description;
    public String user_image_url;
    public String user_major;
    public String user_name;
    public String user_school;
    public boolean is_instant;
    public Set<AvailableBlock> availableBlocks;
    public Set<String> messages;

    // empty constructor necessary for SugarORM to work
    public Sesh() {
    }

    public Sesh(String class_name, boolean has_been_seen, boolean has_started, boolean is_student,
                double latitude, String locationNotes, double longitude, int past_request_id,
                String sesh_description, int sesh_est_time, int sesh_id, int sesh_num_students,
                Date sesh_set_time, Date start_time, double tutor_latitude, double tutor_longitude,
                String user_description, String user_image_url, String user_major, String user_name,
                String user_school, boolean is_instant, Set<AvailableBlock> availableBlocks,
                Set<String> messages) {

        this.class_name = class_name;
        this.has_been_seen = has_been_seen;
        this.has_started = has_started;
        this.is_student = is_student;
        this.latitude = latitude;
        this.locationNotes = locationNotes;
        this.longitude = longitude;
        this.past_request_id = past_request_id;
        this.sesh_description = sesh_description;
        this.sesh_est_time = sesh_est_time;
        this.sesh_id = sesh_id;
        this.sesh_num_students = sesh_num_students;
        this.sesh_set_time = sesh_set_time;
        this.start_time = start_time;
        this.tutor_latitude = tutor_latitude;
        this.tutor_longitude = tutor_longitude;
        this.user_description = user_description;
        this.user_image_url = user_image_url;
        this.user_major = user_major;
        this.user_name = user_name;
        this.user_school = user_school;
        this.is_instant = is_instant;
        this.availableBlocks = availableBlocks;
        this.messages = messages;
    }


    public static Sesh createOrUpdateSeshWithObject(JSONObject seshJson, Context context) {
        Sesh sesh = null;
        try {
            int seshId = seshJson.getInt("sesh_id");
            JSONObject studentRow = seshJson.getJSONObject("student");
            JSONObject tutorRow = seshJson.getJSONObject("tutor");

            Boolean isStudent = User.currentUser(context).userId == studentRow.getInt("user_id");

            JSONObject otherPersonRow = isStudent ? tutorRow : studentRow;

            List<Sesh> seshesFound = Sesh.find(Sesh.class, "sesh_id = ?", Integer.toString(seshId));

            if (seshesFound.size() > 0) {
                sesh = seshesFound.get(0);
            } else {
                sesh = new Sesh();
            }

            sesh.class_name = seshJson.getString("class_name");
            sesh.has_started = seshJson.getBoolean("has_started");
            sesh.is_student = isStudent;
            sesh.latitude = seshJson.getDouble("latitude");
            sesh.locationNotes = seshJson.getString("location_notes");
            sesh.longitude = seshJson.getDouble("longitude");
            sesh.past_request_id = seshJson.getInt("past_request_id");
            sesh.sesh_description = seshJson.getString("description");
            sesh.sesh_est_time = seshJson.getInt("est_time");
            sesh.sesh_id = seshId;
            sesh.sesh_num_students = seshJson.getInt("num_students");
//            sesh.sesh_set_time = seshJson.get;
//            sesh.start_time = start_time;
            sesh.user_description = otherPersonRow.getString("bio");
            sesh.user_image_url = otherPersonRow.getString("profile_picture");
            sesh.user_major = otherPersonRow.getString("major");
            sesh.user_name = otherPersonRow.getString("full_name");
            sesh.is_instant = seshJson.getBoolean("is_instant");
//            sesh.availableBlocks = availableBlocks;
//            sesh.messages = messages;

            sesh.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update user in db; JSON user object from server is malformed.");
            return null;
        }
        return sesh;
    }

}