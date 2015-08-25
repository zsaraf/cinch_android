package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nadavhollander on 8/24/15.
 */
public class PastRequest extends SugarRecord<PastRequest> {
    @Ignore
    private static final String TAG = PastRequest.class.getName();

    public int classId;
    public String classString;
    public String descr;
    public int estTime;
    public boolean isInstant;
    public double latitude;
    public double longitude;
    public int pastRequestId;
    public String locationNotes;
    public int numPeople;

    public PastRequest() {}

    public PastRequest(int classId, String classString, String descr, int estTime, boolean isInstant,
                       double latitude, double longitude, int pastRequestId, String locationNotes,
                       int numPeople, Set<AvailableBlock> availableBlocks) {
        this.classId = classId;
        this.classString = classString;
        this.descr = descr;
        this.estTime = estTime;
        this.isInstant = isInstant;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pastRequestId = pastRequestId;
        this.locationNotes = locationNotes;
        this.numPeople = numPeople;
    }

    public static PastRequest createOrUpdatePastRequest(JSONObject pastRequestJson) {
        PastRequest pastRequest = null;
        int pastRequestId = -1;

        try {
            if (pastRequestJson.has("id")) {
                pastRequestId = pastRequestJson.getInt("id");

                List<PastRequest> requestsFound = PastRequest.find(PastRequest.class, "past_request_id = ?", Integer.toString(pastRequestId));

                if (requestsFound.size() > 0) {
                    pastRequest = requestsFound.get(0);
                } else {
                    pastRequest = new PastRequest();
                }
            } else {
                pastRequest = new PastRequest();
            }

            pastRequest.pastRequestId = pastRequestId;
            pastRequest.classId = pastRequestJson.getInt("class_id");
            pastRequest.classString = pastRequestJson.getString("class_string");
            pastRequest.descr = pastRequestJson.getString("description");
            pastRequest.estTime = pastRequestJson.getInt("est_time");
            pastRequest.isInstant = (pastRequestJson.getInt("is_instant") == 1) ? true : false;
            pastRequest.latitude = pastRequestJson.getDouble("latitude");
            pastRequest.longitude = pastRequestJson.getDouble("longitude");
            pastRequest.numPeople = pastRequestJson.getInt("num_people");
            pastRequest.locationNotes = (pastRequestJson.has("location_notes") ? pastRequestJson.getString("location_notes") : null);

            pastRequest.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update past request; " + e.getMessage());
            return null;
        }

        return pastRequest;
    }


}
