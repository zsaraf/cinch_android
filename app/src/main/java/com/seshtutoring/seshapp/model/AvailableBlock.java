package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nadavhollander on 7/25/15.
 */
public class AvailableBlock extends SugarRecord<AvailableBlock> {
    @Ignore
    private static final String TAG = AvailableBlock.class.getName();

    public Date startTime;
    public Date endTime;
    public LearnRequest learnRequest;

    public AvailableBlock() {}

    public AvailableBlock(Date startTime, Date endTime, LearnRequest learnRequest) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.learnRequest = learnRequest;
    }

    public static AvailableBlock createAvailableBlock(JSONObject availableBlockJson) {
        AvailableBlock availableBlock = new AvailableBlock();
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ssZ");

            String endTimeString = availableBlockJson.getString("endTime");
            availableBlock.endTime = formatter.parseDateTime(endTimeString).toDate();
            String startTimeString = availableBlockJson.getString("startTime");
            availableBlock.startTime = formatter.parseDateTime(startTimeString).toDate();

            int learnRequestId = availableBlockJson.getInt("learnRequestId");
            List<LearnRequest> learnRequestList =
                    LearnRequest.find(LearnRequest.class, "learn_request_id = ?", Integer.toString(learnRequestId));
            availableBlock.learnRequest = learnRequestList.get(0);

            availableBlock.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create available block; " + e.getMessage());
        }
        return availableBlock;
    }

//    TEMP FIX UNTIL SCHEDULING IMPLEMENTED
    public static AvailableBlock availableBlockForInstantRequest(LearnRequest instantRequest) {
        DateTime startTime = new DateTime(instantRequest.timestamp);
        DateTime endTime = startTime.plusMinutes(instantRequest.estTime + 30);
        return new AvailableBlock(startTime.toDate(), endTime.toDate(), instantRequest);
    }

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ssZ");

        map.put("startTime", formatter.print(new DateTime(startTime)));
        map.put("endTime", formatter.print(new DateTime(endTime)));
        return map;
    }
}
