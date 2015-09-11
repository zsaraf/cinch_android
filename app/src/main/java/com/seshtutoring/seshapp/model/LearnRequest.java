package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nadavhollander on 7/25/15.
 */
public class LearnRequest extends SugarRecord<LearnRequest> {
    @Ignore
    private static final String TAG = LearnRequest.class.getName();

    @Ignore
    private static LearnRequestTableListener listener;

    @Ignore
    private String estTimeString;

    public String classId;
    public String classString;
    public String descr;
    public int estTime;
    private boolean isInstant;
    public double latitude;
    public double longitude;
    public int learnRequestId;
    public int numPeople;
    public long timestamp;
    public String locationNotes;
    public long expirationTime;
    public boolean requiresAnimatedDisplay;

    @Ignore
    public Set<AvailableBlock> availableBlocks;

    @Ignore
    public Discount discount;

//    public Set<Favorite> favorites;

    public interface LearnRequestTableListener {
        void tableUpdated();
    }

    public LearnRequest() {
        this.availableBlocks = new HashSet<AvailableBlock>();
    }

    public LearnRequest(String classId, String classString, String descr, int estTime, boolean isInstant, double latitude,
                        double longitude, int learnRequestId, int numPeople,
                        long timestamp, String locationNotes, long expirationTime, Set<AvailableBlock> availableBlocks) {
        this.classId = classId;
        this.classString = classString;
        this.descr = descr;
        this.estTime = estTime;
        this.isInstant = isInstant;
        this.latitude = latitude;
        this.longitude = longitude;
        this.learnRequestId = learnRequestId;
        this.numPeople = numPeople;
        this.timestamp = timestamp;
        this.locationNotes = locationNotes;
        this.expirationTime = expirationTime;

        if (availableBlocks != null) {
            this.availableBlocks = availableBlocks;
        } else {
            this.availableBlocks = new HashSet<AvailableBlock>();
        }
    }

    public static LearnRequest createOrUpdateLearnRequest(JSONObject learnRequestJson) {
        LearnRequest learnRequest = null;
        try {
            if (learnRequestJson.has("id")) {
                int learnRequestId = learnRequestJson.getInt("id");

                List<LearnRequest> requestsFound = LearnRequest.find(LearnRequest.class, "learn_request_id = ?", Integer.toString(learnRequestId));

                if (requestsFound.size() > 0) {
                    learnRequest = requestsFound.get(0);
                } else {
                    learnRequest = new LearnRequest();
                }
            } else {
                learnRequest = new LearnRequest();
            }

            java.util.Date date = new java.util.Date();

            DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");

            learnRequest.classId = learnRequestJson.getString("class_id");
            learnRequest.classString = learnRequestJson.getString("class_string");
            learnRequest.descr = learnRequestJson.getString("description");
            learnRequest.estTime = learnRequestJson.getInt("est_time");
            learnRequest.isInstant = (learnRequestJson.getInt("is_instant") == 1) ? true : false;
            learnRequest.latitude = learnRequestJson.getDouble("latitude");
            learnRequest.longitude = learnRequestJson.getDouble("longitude");
            learnRequest.learnRequestId = learnRequestJson.getInt("id");
            learnRequest.numPeople = learnRequestJson.getInt("num_people");
            learnRequest.timestamp = formatter
                    .parseDateTime(learnRequestJson.getString("timestamp")).getMillis();
            learnRequest.locationNotes = learnRequestJson.getString("location_notes");
            learnRequest.requiresAnimatedDisplay = false;

            learnRequest.save();

            if(!learnRequest.isInstant) {
                AvailableBlock.deleteAll(AvailableBlock.class, "learn_request = ?", Long.toString(learnRequest.getId()));
                JSONArray availableBlockObjects = learnRequestJson.getJSONArray("available_blocks");

                for (int i = 0; i < availableBlockObjects.length(); i++) {
                    JSONObject availableBlockJson = availableBlockObjects.getJSONObject(i);
                    AvailableBlock availableBlock = AvailableBlock.createAvailableBlock(availableBlockJson);
                    availableBlock.learnRequest = learnRequest;
                    availableBlock.save();
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update learn request; " + e.getMessage());
            return null;
        }
        return learnRequest;
    }

    public static void setTableListener(LearnRequestTableListener tableListener) {
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
        AvailableBlock.deleteAll(AvailableBlock.class, "learn_request = ?", Long.toString(getId()));

        super.delete();
        if (listener != null) {
            listener.tableUpdated();
        }
    }

    public static LearnRequest learnRequestFromPastRequest(PastRequest pastRequest) {
        LearnRequest learnRequest = new LearnRequest();
        learnRequest.classId = Integer.toString(pastRequest.classId);
        learnRequest.classString = pastRequest.classString;
        learnRequest.descr = pastRequest.descr;
        learnRequest.estTime = pastRequest.estTime;
        learnRequest.isInstant = pastRequest.isInstant;
        learnRequest.latitude = pastRequest.latitude;
        learnRequest.longitude = pastRequest.longitude;
        learnRequest.numPeople = pastRequest.numPeople;
        return learnRequest;
    }

    public List<AvailableBlock> getAvailableBlocks() {
        return AvailableBlock.find(AvailableBlock.class, "learn_request = ?", Long.toString(getId()));
    }

    public void setEstTimeString(String estTimeString) {
        this.estTimeString = estTimeString;
    }

    public String getEstTimeString() {
        return estTimeString;
    }

    //    TEMP FIX UNTIL SCHEDULING IMPLEMENTED ON ANDROID
    public void createAvailableBlockForNow(int hoursUntilExpiration) {
        AvailableBlock block = AvailableBlock.availableBlockForInstantRequest(this, hoursUntilExpiration);
        availableBlocks.add(block);
    }

    public boolean isInstant() {
        return isInstant;
    }

    public void setIsInstant(boolean isInstant) {
        this.isInstant = isInstant;
//        if (isInstant) {
            // set timestamp to now
            this.timestamp = new Date().getTime();
//        }
    }

    public String getContainerStateTag() {
        return "view_request_" + learnRequestId;
    }
}
