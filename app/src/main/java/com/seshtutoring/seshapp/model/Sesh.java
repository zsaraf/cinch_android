package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.util.DateUtils;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.squareup.picasso.Callback;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by nadavhollander on 7/25/15.
 */
public class Sesh extends SugarRecord<Sesh> {
    @Ignore
    private static final String TAG = Sesh.class.getName();

    @Ignore
    private static SeshTableListener listener;

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
    public Date seshSetTime; // DUE TO SUGARORM BUG, CANNOT BE SET NULL
    public Date startTime; // DUE TO SUGARORM BUG, CANNOT BE SET NULL
    public double tutorLatitude;
    public double tutorLongitude;
    public String userDescription;
    public String userImageUrl;
    public String userMajor;
    public String userName;
    public String userSchool;
    public boolean isInstant;
    public boolean requiresAnimatedDisplay;

    @Ignore
    public Set<AvailableBlock> availableBlocks;
    public Set<Message> messages;

    private static final String AVAILABLE_BLOCKS_KEY = "available_blocks";
    private static final String MESSAGES_KEY = "messages";

    // empty constructor necessary for SugarORM to work
    public Sesh() {
    }

    public interface SeshTableListener {
        void tableUpdated();
    }

    public Sesh(String class_name, boolean has_been_seen, boolean has_started, boolean is_student,
                double latitude, String locationNotes, double longitude, int past_request_id,
                String sesh_description, int sesh_est_time, int sesh_id, int sesh_num_students,
                Date sesh_set_time, Date start_time, double tutor_latitude, double tutor_longitude,
                String user_description, String user_image_url, String user_major, String user_name,
                String user_school, boolean is_instant, Set<AvailableBlock> availableBlocks, Set<Message> messages) {

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

            if (Sesh.listAll(Sesh.class).size() > 0) {
                List<Sesh> seshesFound = Sesh.find(Sesh.class, "sesh_id = ?", Integer.toString(seshId));
                if (seshesFound.size() > 0) {
                    sesh = seshesFound.get(0);
                } else {
                    sesh = new Sesh();
                }
            } else {
                sesh = new Sesh();
            }

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
            sesh.requiresAnimatedDisplay = false;

            String seshSetTime = seshJson.getString("set_time");
            if (!seshSetTime.equals("null")) {
                sesh.seshSetTime = formattedTime(seshSetTime);
            } else {
                // DUE TO SUGARORM BUG, WE CANNOT SET DATE TO NULL, SO WE SET DATE TO UNIX EPOCH TIME
                sesh.seshSetTime = new Date(0);
            }
            String startTime = seshJson.getString("start_time");
            if (!startTime.equals("null")) {
                sesh.startTime = formattedTime(startTime);
            } else {
                // DUE TO SUGARORM BUG, WE CANNOT SET DATE TO NULL, SO WE SET DATE TO UNIX EPOCH TIME
                sesh.startTime = new Date(0);
            }

            sesh.save();

            if (seshJson.get(AVAILABLE_BLOCKS_KEY) != null) {
                JSONArray availableBlocksJson = seshJson.getJSONArray(AVAILABLE_BLOCKS_KEY);
                for (int i = 0; i < availableBlocksJson.length(); i++) {

                    JSONObject availableBlockJson = availableBlocksJson.getJSONObject(i);
                    AvailableBlock availableBlockObj = AvailableBlock.createAvailableBlock(availableBlockJson);
                    availableBlockObj.sesh = sesh;
                    availableBlockObj.save();
                }
            }

            if (seshJson.get(MESSAGES_KEY) != null) {
                JSONArray messagesJSON = seshJson.getJSONArray(MESSAGES_KEY);
                for (int i = 0; i < messagesJSON.length(); i++) {

                    JSONObject messageJSON = messagesJSON.getJSONObject(i);
                    Message message = Message.createOrUpdateMessageWithJSON(messageJSON, sesh);
                    message.save();
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update user in db; JSON user object from server is malformed: " + e.getMessage());
            return null;
        }
        return sesh;
    }

    public static void setTableListener(SeshTableListener tableListener) {
        listener = tableListener;
    }

    @Override
    public void save() {
        super.save();
        if (listener != null) {
            listener.tableUpdated();
        }
    }

    @Override
    public void delete() {
        super.delete();
        if (listener != null) {
            listener.tableUpdated();
        }
    }

    private static Date formattedTime(String rawTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();
        return formatter.parseDateTime(rawTimeString).toDate();
    }

    public static synchronized Sesh getCurrentSesh() {
        List<Sesh> startedSeshes = Sesh.find(Sesh.class, "has_started = ?", "1");

        if (startedSeshes.size() > 1) {
            Log.e(TAG, "There should not be more than one Sesh in the server.");
        } else if (startedSeshes.size() == 0) {
            return null;
        }

        return startedSeshes.get(0);
    }

    public void loadImageAsync(ImageView imageView, Context context) {
        SeshNetworking seshNetworking = new SeshNetworking(context);
        seshNetworking.downloadProfilePictureAsync(userImageUrl, imageView, new Callback() {
            @Override
            public void onSuccess() {
                // do nothing
            }

            @Override
            public void onError() {
                // do nothing
            }
        });
    }

    public String getTimeAbbrvString() {
        if (seshSetTime == null) {
            return "Time TBD";
        } else {
            DateTime setTime = new DateTime(seshSetTime);
            return DateUtils.getSeshFormattedDate(setTime);
        }

    }

    public static synchronized void updateSeshInfoWithObject(Context context, JSONObject jsonObject) {
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

    public String abbreviatedNameForOtherPerson() {
        String[] components = userName.split("\\s+");
        String capitalizedFirstName = components[0].substring(0,1).toUpperCase() + components[0].substring(1).toLowerCase();
        if (components.length > 1) {
            return capitalizedFirstName + " " + components[components.length-1].substring(0,1).toUpperCase() + ".";
        } else {
            return capitalizedFirstName;
        }
    }

    public String firstName() {
        String[] components = userName.split("\\s+");
        return components[0].substring(0,1).toUpperCase() + components[0].substring(1).toLowerCase();
    }

    public static Sesh findSeshWithId(int seshId) {
        List<Sesh> seshesFound = Sesh.find(Sesh.class, "sesh_id = ?", Integer.toString(seshId));
        if (seshesFound.size() > 0) {
            return seshesFound.get(0);
        }
        return null;
    }

    public void setLocationNotes(String locationNotes) {
        this.locationNotes = locationNotes;
        this.save();
    }
}