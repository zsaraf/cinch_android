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
    public double tutorLatitude;
    public double tutorLongitude;
    public String userDescription;
    public String userImageUrl;
    public String userMajor;
    public String userName;
    public String userSchool;
    public boolean isInstant;
    public boolean requiresAnimatedDisplay;

    // Due to a bug in SugarORM, Date fields cannot be set to null, so, in order to communicate
    // whether or not the following variables are set, we use longs representing Milliseconds since epoch
    // time.  If variable == -1, it has not been set yet by the server/client (eg. as if it were set to null)
    public long seshSetTime; // SET IN MILLIS SINCE EPOCH
    public long startTime; // SET IN MILLIS SINCE EPOCH

    @Ignore
    public Set<AvailableBlock> availableBlocks;

    private static final String AVAILABLE_BLOCKS_KEY = "available_blocks";
    private static final String MESSAGES_KEY = "messages";

    // empty constructor necessary for SugarORM to work
    public Sesh() {
    }

    public interface SeshTableListener {
        void tableUpdated();
    }

    public static Sesh createOrUpdateSeshWithObject(JSONObject seshJson, Context context) {
        Sesh sesh = null;
        try {
            int seshId = seshJson.getInt("sesh_id");
            JSONObject studentRow = seshJson.getJSONObject("student");
            JSONObject tutorRow = seshJson.getJSONObject("tutor");

            Boolean isStudent = User.currentUser(context).userId == studentRow.getInt("user_id");

            JSONObject otherPersonRow = isStudent ? tutorRow : studentRow;

            boolean isNewlyCreatedSesh;

            if (Sesh.listAll(Sesh.class).size() > 0) {
                List<Sesh> seshesFound = Sesh.find(Sesh.class, "sesh_id = ?", Integer.toString(seshId));
                if (seshesFound.size() > 0) {
                    sesh = seshesFound.get(0);
                    isNewlyCreatedSesh = false;
                } else {
                    sesh = new Sesh();
                    isNewlyCreatedSesh = false;
                }
            } else {
                sesh = new Sesh();
                isNewlyCreatedSesh = false;
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

            if (isNewlyCreatedSesh) {
                sesh.requiresAnimatedDisplay = false;
            }

            String seshSetTime = seshJson.getString("set_time");
            if (seshSetTime != null && !seshSetTime.equals("null")) {
                sesh.seshSetTime = formattedTime(seshSetTime).getTime();
            } else {
                // Due to SugarORM bug, we set time to -1 if retrieved result is null
                sesh.seshSetTime = -1;
            }

            String startTime = seshJson.getString("start_time");
            if (startTime != null && !startTime.equals("null")) {
                sesh.startTime = formattedTime(startTime).getTime();
            } else {
                // Due to SugarORM bug, we set time to -1 if retrieved result is null
                sesh.startTime = -1;
            }

            sesh.save();

            AvailableBlock.deleteAll(AvailableBlock.class, "sesh = ?", Long.toString(sesh.getId()));
            Set<AvailableBlock> availableBlocksVal = new HashSet<AvailableBlock>();
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

    public List<Message> getMessages() {
        return Message.find(Message.class, "sesh.sesh_id = ?", Integer.toString(seshId));
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
        // Delete all available blocks associated with this Sesh
        AvailableBlock.deleteAll(AvailableBlock.class, "sesh = ?", Long.toString(this.getId()));

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
        if (seshSetTime == -1) {
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