package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

//            int learnRequestId = availableBlockJson.getInt("learnRequestId");
//            List<LearnRequest> learnRequestList =
//                    LearnRequest.find(LearnRequest.class, "learn_request_id = ?", Integer.toString(learnRequestId));
//            availableBlock.learnRequest = learnRequestList.get(0);

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

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");

        map.put(START_TIME_KEY, formatter.print(new DateTime(startTime)));
        map.put(END_TIME_KEY, formatter.print(new DateTime(endTime)));
        return map;
    }

    public static String getReadableBlocks(List<AvailableBlock> availableBlocks) {

        DateTime now = new DateTime();
        int today = now.getDayOfWeek();
        String[] days = {"Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"};

        String blockStr = "";

        for (AvailableBlock block : availableBlocks) {
            DateTime st = new DateTime(block.startTime);
            int day = st.getDayOfWeek() - 1;
            String dayStr = days[day];
            if (day == today) {
                dayStr = "TODAY";
            } else if (day == today + 1) {
                dayStr = "TMRW";
            }
            dayStr = "<b>" + dayStr + "</b>";
            String ampm = "a";
            int hour = st.getHourOfDay();
            if (hour > 12) {
                hour = hour - 12;
                ampm = "p";
            }
            int min = st.getMinuteOfHour();
            String minStr = "";
            if (min > 15 && min < 45) {
                minStr = ":30";
            }
            String startStr = dayStr + ": " + hour + minStr + ampm;
            DateTime et = new DateTime(block.endTime);
//            day = et.getDayOfWeek();
//            dayStr = days[day];
            ampm = "a";
            hour = et.getHourOfDay() + 1;
            if (hour > 12) {
                hour = hour - 12;
                ampm = "p";
            }
            min = et.getMinuteOfHour();
            minStr = "";
            if (min > 15 && min < 45) {
                minStr = ":30";
            }
            String endStr = hour + minStr + ampm;
            blockStr += startStr + "-" + endStr + "<br />";
        }

        if (availableBlocks.size() == 0) {
            blockStr = "<b>" + "NOW" + "</b>";
        }
        return blockStr;
    }
}
