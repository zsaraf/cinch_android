package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by nadavhollander on 7/25/15.
 */
public class Sesh extends SugarRecord<Sesh> {
    @Ignore
    private static final String TAG = Sesh.class.getName();

    public int seshId;
    public String className;
    public boolean hasBeenSeen;
    public boolean hasStarted;
    public boolean isStudent;
    public double latitude;
    public String locationNotes;
    public double longitude;
    public int pastRequestId;
    public String seshDescription;
    public int seshEstTime;
    public int seshNumStudents;
    public Timestamp seshSetTime;
    public Timestamp startTime;
    public double tutorLatitude;
    public double tutorLongitude;
    public String userDescription;
    public String userImageUrl;
    public String userMajor;
    public String userName;
    public String userSchool;
    public boolean isInstant;
//    public Set<AvailableBlock> availableBlocks;
//    public Set<String> messages;

    // empty constructor necessary for SugarORM to work
    public Sesh() {
    }

    public Sesh(String class_name, boolean has_been_seen, boolean has_started, boolean is_student,
                double latitude, String locationNotes, double longitude, int past_request_id,
                String sesh_description, int sesh_est_time, int sesh_id, int sesh_num_students,
                Timestamp sesh_set_time, Timestamp start_time, double tutor_latitude, double tutor_longitude,
                String user_description, String user_image_url, String user_major, String user_name,
                String user_school, boolean is_instant) {

        this.className = class_name;
        this.hasBeenSeen = has_been_seen;
        this.hasStarted = has_started;
        this.isStudent = is_student;
        this.latitude = latitude;
        this.locationNotes = locationNotes;
        this.longitude = longitude;
        this.pastRequestId = past_request_id;
        this.seshDescription = sesh_description;
        this.seshEstTime = sesh_est_time;
        this.seshId = sesh_id;
        this.seshNumStudents = sesh_num_students;
        this.seshSetTime = sesh_set_time;
        this.startTime = start_time;
        this.tutorLatitude = tutor_latitude;
        this.tutorLongitude = tutor_longitude;
        this.userDescription = user_description;
        this.userImageUrl = user_image_url;
        this.userMajor = user_major;
        this.userName = user_name;
        this.userSchool = user_school;
        this.isInstant = is_instant;
//        this.availableBlocks = availableBlocks;
//        this.messages = messages;
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

            DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ssZ");

            sesh.className = seshJson.getString("class_name");
            sesh.hasStarted = (seshJson.getInt("has_started") == 1) ? true : false;
            sesh.isStudent = isStudent;
            sesh.latitude = seshJson.getDouble("latitude");
            sesh.locationNotes = seshJson.getString("location_notes");
            sesh.longitude = seshJson.getDouble("longitude");
            sesh.pastRequestId = seshJson.getInt("past_request_id");
            sesh.seshDescription = seshJson.getString("description");
            sesh.seshEstTime = seshJson.getInt("est_time");
            sesh.seshId = seshId;
            sesh.seshNumStudents = seshJson.getInt("num_students");
            sesh.userDescription = otherPersonRow.getString("bio");
            sesh.userImageUrl = otherPersonRow.getString("profile_picture");
            sesh.userMajor = otherPersonRow.getString("major");
            sesh.userName = otherPersonRow.getString("full_name");
            sesh.isInstant = (seshJson.getInt("is_instant") == 1) ? true : false;

            String seshSetTime = seshJson.getString("set_time");
            if (!seshSetTime.equals("null")) {
                sesh.seshSetTime = new Timestamp(formatter.parseDateTime(seshSetTime).getMillis());
            }
            String startTime = seshJson.getString("start_time");
            if (!startTime.equals("null")) {
                sesh.startTime = new Timestamp(formatter.parseDateTime(startTime).getMillis());
            }

            sesh.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update user in db; JSON user object from server is malformed: " + e.getMessage());
            return null;
        }
        return sesh;
    }

    public String getTimeAbbrvString() {
            if (seshSetTime == null) {
                return "Time TBD";
            }

            DateTime setTime = new DateTime(seshSetTime);

            String day = "";
            String time = "";

            if (setTime.toLocalDate().equals(new LocalDate())) {
                day = "TODAY";
            } else if (setTime.minusDays(1).equals(new LocalDate())) {
                day = "TMRW";
            } else {
                int dayOfWeek = setTime.getDayOfWeek();
                switch (dayOfWeek) {
                    case DateTimeConstants.SUNDAY:
                        day = "SUN";
                        break;
                    case DateTimeConstants.MONDAY:
                        day = "MON";
                        break;
                    case DateTimeConstants.TUESDAY:
                        day = "TUES";
                        break;
                    case DateTimeConstants.WEDNESDAY:
                        day = "WED";
                        break;
                    case DateTimeConstants.THURSDAY:
                        day = "THURS";
                        break;
                    case DateTimeConstants.FRIDAY:
                        day = "FRI";
                        break;
                    case DateTimeConstants.SATURDAY:
                        day = "SAT";
                        break;
                }
            }

        DateFormat hourMinute = new SimpleDateFormat("hh:mm a");
        time = hourMinute.format(setTime.toDate());

        return String.format("%s %s", day, time);
    }

    public static void updateSeshInfoWithObject(Context context, JSONObject jsonObject) {
        try {
            if (jsonObject.get("status").equals("SUCCESS")) {
                JSONArray seshes = jsonObject.getJSONArray(("open_seshes"));
                Sesh.deleteAll(Sesh.class);
                for (int i = 0; i < seshes.length(); i++) {
                    JSONObject seshObject = seshes.getJSONObject(i);
                    Sesh.createOrUpdateSeshWithObject(seshObject, context);
                }

                JSONArray openRequests = jsonObject.getJSONArray(("open_requests"));
                LearnRequest.deleteAll(LearnRequest.class);
                for (int i = 0; i < openRequests.length(); i++) {
                    JSONObject openRequestObject = openRequests.getJSONObject(i);
                    LearnRequest.createOrUpdateLearnRequest(openRequestObject);
                }

//                        JSONArray unseenPastRequests = jsonObject.getJSONArray(("unseen_past_requests"));
//                        for (int i = 0; i < unseenPastRequests.length(); i++) {
//                            JSONObject unseenPastRequestObject = unseenPastRequests.getJSONObject(i);
//                            // DO SOMETHING
//                        }
            } else {
                Log.e(TAG, "Failed to fetch full user info from server: " + jsonObject.getString("message"));
            }

        } catch (JSONException e) {
            Log.e(TAG, "Failed to fetch user info from server; response malformed: " + e.getMessage());
        }
    }
}