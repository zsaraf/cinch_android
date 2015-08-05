package com.seshtutoring.seshapp.model;

/**
 * Created by lillioetting on 7/30/15.
 */

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.JodaTimePermission;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import com.seshtutoring.seshapp.model.AvailableBlock;

public class AvailableJob {
    private static final String TAG = AvailableJob.class.getName();
    private static final String DESCRIPTION_KEY = "description";
    private static final String MAX_TIME_KEY = "est_time";
    private static final String STUDENT_NAME_KEY = "first_name";
    private static final String CLASS_KEY = "class";
    private static final String REQUEST_ID_KEY = "request_id";
    private static final String NUM_PEOPLE_KEY = "num_people";
    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";
    private static final String AVAILABLE_BLOCKS_KEY = "available_blocks";
    private static final String RATE_KEY = "hourly_rate";


    public String description;
    public String studentName;
    public int numPeople;
    public double latitude;
    public double longitude;
    public double maxTime;
    public int requestId;
    public Set<AvailableBlock> availableBlocks;
    public Course course;
    public double rate;
    public String[] days;

    public AvailableJob(String description, String studentName, int numPeople, double latitude, double longitude,
                        double maxTime, int requestId, Set<AvailableBlock> availableBlocks, Course course, double rate) {
        this.description = description;
        this.studentName = studentName;
        this.numPeople = numPeople;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxTime = maxTime;
        this.requestId = requestId;
        this.availableBlocks = availableBlocks;
        this.course = course;
        this.rate = rate;
        String[] days = {"Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"};
        this.days = days;
    }

    public String getReadableBlocks() {

        DateTime now = new DateTime();
        int today = now.getDayOfWeek();

        String blockStr = "";

        for (AvailableBlock block : availableBlocks) {
            DateTime st = new DateTime(block.startTime);
            int day = st.getDayOfWeek();
            String dayStr = days[day];
            if (day == today) {
                dayStr = "TODAY";
            }else if (day == today + 1) {
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


        return blockStr;
    }

    public static AvailableJob fromJson(JSONObject json) {
        AvailableJob availableJobInstance = null;

        String descriptionVal;
        String studentNameVal;
        int numPeopleVal;
        double latitudeVal;
        double longitudeVal;
        double maxTimeVal;
        int requestIdVal;
        Set<AvailableBlock> availableBlocksVal;
        Course courseVal;
        double rateVal;

        try {
            descriptionVal = json.getString(DESCRIPTION_KEY);
            studentNameVal = json.getString(STUDENT_NAME_KEY);
            numPeopleVal = json.getInt(NUM_PEOPLE_KEY);
            latitudeVal = json.getDouble(LATITUDE_KEY);
            longitudeVal = json.getDouble(LONGITUDE_KEY);
            maxTimeVal = json.getDouble(MAX_TIME_KEY)/60;
            requestIdVal = json.getInt(REQUEST_ID_KEY);
            rateVal = json.getDouble(RATE_KEY);
            courseVal = Course.fromClassRowJson(json.getJSONObject(CLASS_KEY));
            availableBlocksVal = new HashSet<AvailableBlock>();
            if (json.get(AVAILABLE_BLOCKS_KEY) == null) {
                JSONArray availableBlocksJson = json.getJSONArray(AVAILABLE_BLOCKS_KEY);
                for (int i = 0; i < availableBlocksJson.length(); i++) {

                    JSONObject availableBlockJson = availableBlocksJson.getJSONObject(i);
                    AvailableBlock availableBlockObj = AvailableBlock.createAvailableBlockForJob(availableBlockJson);
                    availableBlocksVal.add(availableBlockObj);

                }
            }
            availableJobInstance = new AvailableJob(descriptionVal, studentNameVal, numPeopleVal, latitudeVal, longitudeVal,
                    maxTimeVal, requestIdVal, availableBlocksVal, courseVal, rateVal);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return availableJobInstance;
    }
}

