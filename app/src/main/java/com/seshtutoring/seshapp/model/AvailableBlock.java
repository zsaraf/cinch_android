package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.util.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nadavhollander on 7/25/15.
 */
public class AvailableBlock extends SugarRecord<AvailableBlock> {

    @Ignore
    private static final String TAG = AvailableBlock.class.getName();

    private static final String START_TIME_KEY = "start_time";
    private static final String END_TIME_KEY = "end_time";

    // Due to a bug in SugarORM, Date fields cannot be set to null, so, in order to communicate
    // whether or not the following variables are set, we use longs representing Milliseconds since epoch
    // time.  If variable == -1, it has not been set yet by the server/client (eg. as if it were set to null)
    public long startTime; // SET IN MILLIS SINCE EPOCH
    public long endTime; // SET IN MILLIS SINCE EPOCH

    public LearnRequest learnRequest;
    public Sesh sesh;

    public AvailableBlock() {
    }

    public AvailableBlock(long startTime, long endTime, LearnRequest learnRequest, Sesh sesh) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.learnRequest = learnRequest;
        this.sesh = sesh;
    }

    public static AvailableBlock createAvailableBlock(JSONObject availableBlockJson) {
        AvailableBlock availableBlock = new AvailableBlock();
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();

            String endTimeString = availableBlockJson.getString(END_TIME_KEY);
            if (endTimeString == null || endTimeString.equals("null")) {
                availableBlock.endTime = -1;
            } else {
                availableBlock.endTime = formatter.parseDateTime(endTimeString).getMillis();
            }

            String startTimeString = availableBlockJson.getString(START_TIME_KEY);
            if (startTimeString == null || startTimeString.equals("null")) {
                availableBlock.startTime = -1;
            } else {
                availableBlock.startTime = formatter.parseDateTime(startTimeString).getMillis();
            }

            availableBlock.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create available block; " + e.getMessage());
        }
        return availableBlock;
    }

    //    TEMP FIX UNTIL SCHEDULING IMPLEMENTED
    public static AvailableBlock availableBlockForInstantRequest(LearnRequest instantRequest, int hoursUntilExpiration) {
        DateTime startTime = new DateTime(instantRequest.timestamp);
        DateTime endTime = startTime.plusHours(hoursUntilExpiration);
        return new AvailableBlock(startTime.getMillis(), endTime.getMillis(), instantRequest, null);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");

        json.put(START_TIME_KEY, formatter.print(new DateTime(startTime)));
        json.put(END_TIME_KEY, formatter.print(new DateTime(endTime)));

        return json;
    }

    public static String getReadableBlocks(List<AvailableBlock> availableBlocks) {
        DateTime now = new DateTime();
        int today = now.getDayOfWeek() - 1;
        String[] days = {"Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"};

        String blockString = "";
        List<AvailableBlock> filteredList = new ArrayList<AvailableBlock>();
        for (AvailableBlock availableBlock : availableBlocks) {
            if (availableBlock.endTime >= now.getMillis()) {
                filteredList.add(availableBlock);
            }
        }

        Collections.sort(filteredList, new Comparator<AvailableBlock>() {
            @Override
            public int compare(AvailableBlock lhs, AvailableBlock rhs) {
                return (int)(lhs.startTime - rhs.startTime);
            }
        });

        int counter = 0;

        while (counter < filteredList.size()) {
            DateTime startTime = new DateTime(filteredList.get(counter).startTime);
            int startDay = startTime.getDayOfWeek();
            String dayString = "<b>" + DateUtils.getSeshFormattedDayString(startTime) + "</b> <br />";

            while (counter < filteredList.size() && (startTime = new DateTime(filteredList.get(counter).startTime)).getDayOfWeek() == startDay) {
                DateTime endTime = new DateTime(filteredList.get(counter).endTime);
                dayString = dayString + DateUtils.getSeshFormattedTimeString(startTime) + "-" + DateUtils.getSeshFormattedTimeString(endTime) + ", ";
                counter++;
            }
            // Remove final comma and space
            dayString = dayString.substring(0, dayString.length() - 2) + "<br /><br />";
            blockString = blockString + dayString;
        }
        return blockString;

    }
}
