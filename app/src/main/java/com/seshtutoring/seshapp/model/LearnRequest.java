package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;

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
    public Date timestamp;
    public String locationNotes;

    @Ignore
    public Set<AvailableBlock> availableBlocks;
//    public Set<Favorite> favorites;

    public LearnRequest() {
        this.availableBlocks = new HashSet<AvailableBlock>();
    }

    public LearnRequest(String classId, String classString, String descr, int estTime, boolean isInstant, double latitude,
                        double longitude, int learnRequestId, int numPeople,
                        Date timestamp, String locationNotes, Set<AvailableBlock> availableBlocks) {
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

        if (availableBlocks != null) {
            this.availableBlocks = availableBlocks;
        } else {
            this.availableBlocks = new HashSet<AvailableBlock>();
        }
    }

    public static LearnRequest createOrUpdateLearnRequest(JSONObject jsonObject) {
        LearnRequest learnRequest = null;
        try {
            JSONObject learnRequestJson = jsonObject.getJSONObject("learn_request");

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
            learnRequest.timestamp = formatter.parseDateTime(learnRequestJson.getString("timestamp")).toDate();
            learnRequest.locationNotes = learnRequestJson.getString("location_notes");

            JSONArray availableBlockObjects = learnRequestJson.getJSONArray("available_blocks");
            learnRequest.save();

            for (int i = 0; i < learnRequest.availableBlocks.size(); i++) {
                JSONObject availableBlockJson = availableBlockObjects.getJSONObject(i);
                AvailableBlock.createAvailableBlock(availableBlockJson);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update learn request; " + e.getMessage());
            return null;
        }
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
    public void createAvailableBlockForNow() {
        AvailableBlock block = AvailableBlock.availableBlockForInstantRequest(this);
        availableBlocks.add(block);
    }

    public boolean isInstant() {
        return isInstant;
    }

    public void setIsInstant(boolean isInstant) {
        this.isInstant = isInstant;
        if (isInstant) {
            // set timestamp to now
            this.timestamp = new Date();
        }
    }
}