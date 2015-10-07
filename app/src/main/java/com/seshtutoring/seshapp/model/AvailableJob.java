package com.seshtutoring.seshapp.model;

/**
 * Created by lillioetting on 7/30/15.
 */

import android.content.Context;
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
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.model.AvailableBlock;

public class AvailableJob extends SugarRecord<AvailableJob> {
    @Ignore
    private final static String TAG = Tutor.class.getName();

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
    private static final String IS_INSTANT_KEY = "is_instant";
    private static final String ESTIMATED_WAGE_KEY = "estimated_wage_key";


    public String description;
    public String studentName;
    public int numPeople;
    public double latitude;
    public double longitude;
    public double maxTime;
    public int availableJobId;
    public boolean isSelected;
    public double estimatedWage;

    @Ignore
    public Set<AvailableBlock> availableBlocks;

    public Course course;
    public double rate;
    public Boolean isInstant;

    public AvailableJob() {}

    public AvailableJob(String description, String studentName, int numPeople, double latitude, double longitude,
                        double maxTime, int availableJobId, Set<AvailableBlock> availableBlocks, Course course, double rate, Boolean isInstant, double estimatedWage) {
        this.description = description;
        this.studentName = studentName;
        this.numPeople = numPeople;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxTime = maxTime;
        this.availableJobId = availableJobId;
        this.availableBlocks = availableBlocks;
        this.course = course;
        this.rate = rate;
        this.isInstant = isInstant;
        this.estimatedWage = estimatedWage;
    }

    public static AvailableJob createOrUpdateAvailableJobWithObject(JSONObject json) {
        AvailableJob availableJob = null;

        String descriptionVal;
        String studentNameVal;
        int numPeopleVal;
        double latitudeVal;
        double longitudeVal;
        double maxTimeVal;
        int requestIdVal;
        Course courseVal;
        double rateVal;
        Boolean isInstantVal;
        double estimatedWageVal;

        try {
            descriptionVal = json.getString(DESCRIPTION_KEY);
            studentNameVal = json.getString(STUDENT_NAME_KEY);
            numPeopleVal = json.getInt(NUM_PEOPLE_KEY);
            latitudeVal = json.getDouble(LATITUDE_KEY);
            longitudeVal = json.getDouble(LONGITUDE_KEY);
            maxTimeVal = json.getDouble(MAX_TIME_KEY)/60;
            requestIdVal = json.getInt(REQUEST_ID_KEY);
            rateVal = json.getDouble(RATE_KEY);
            courseVal = Course.createOrUpdateCourseWithJSON(json.getJSONObject(CLASS_KEY), null, true);
            isInstantVal = json.getInt(IS_INSTANT_KEY) == 1;
            estimatedWageVal = json.getDouble(ESTIMATED_WAGE_KEY);

            if (AvailableJob.listAll(AvailableJob.class).size() > 0) {
                List<AvailableJob> availableJobsFound = AvailableJob.find(AvailableJob.class, "available_job_id = ?", Integer.toString(requestIdVal));
                if (availableJobsFound.size() > 0) {
                    availableJob = availableJobsFound.get(0);
                } else {
                    availableJob = new AvailableJob();
                    availableJob.isSelected = false;
                }
            } else {
                availableJob = new AvailableJob();
                availableJob.isSelected = false;
            }

            availableJob.description = descriptionVal;
            availableJob.studentName = studentNameVal;
            availableJob.numPeople = numPeopleVal;
            availableJob.latitude = latitudeVal;
            availableJob.longitude = longitudeVal;
            availableJob.maxTime = maxTimeVal;
            availableJob.availableJobId = requestIdVal;
            availableJob.course = courseVal;
            availableJob.rate = rateVal;
            availableJob.isInstant = isInstantVal;
            availableJob.estimatedWage = estimatedWageVal;
            availableJob.save();

            AvailableBlock.deleteAll(AvailableBlock.class, "available_job = ?", Long.toString(availableJob.getId()));
            Set<AvailableBlock> availableBlocksVal = new HashSet<AvailableBlock>();
            if (json.get(AVAILABLE_BLOCKS_KEY) != null) {
                JSONArray availableBlocksJson = json.getJSONArray(AVAILABLE_BLOCKS_KEY);
                for (int i = 0; i < availableBlocksJson.length(); i++) {
                    JSONObject availableBlockJson = availableBlocksJson.getJSONObject(i);
                    AvailableBlock availableBlockObj = AvailableBlock.createAvailableBlock(availableBlockJson);
                    availableBlockObj.availableJob = availableJob;
                    availableBlockObj.save();
                }
            }

            availableJob.save();
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return availableJob;
    }

    public static void deleteAvailableJobsNotInArray(List<AvailableJob> availableJobs) {
        List<AvailableJob> allAvailableJobs = AvailableJob.listAll(AvailableJob.class);
        for (AvailableJob availableJob : allAvailableJobs) {
            if (!availableJobs.contains(availableJob)) {
                availableJob.delete();
            }
        }
    }
}

