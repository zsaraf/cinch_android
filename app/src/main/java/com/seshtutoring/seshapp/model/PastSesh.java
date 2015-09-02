package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by nadavhollander on 8/24/15.
 */
public class PastSesh extends SugarRecord<PastSesh> {
    @Ignore
    private static final String TAG = PastSesh.class.getName();

    public double cancellationCharge;
    public String className;
    public double cost;
    public double creditsUsed;
    public String descr;
    public long endTime;
    public int pastSeshId;
    public double paymentUsed;
    public long startTime;
    public String studentFullName;
    public int studentId;
    public String studentMajor;
    public String studentProfilePicture;
    public int studentUserId;
    public double tutorEarnings;
    public String tutorFullName;
    public int tutorId;
    public String tutorMajor;
    public String tutorProfilePicture;
    public int tutorUserId;
    public boolean wasCancelled;
    public int pastRequestId;

    public PastSesh() {}

    public static PastSesh createOrUpdatePastSesh(JSONObject pastSeshObject) {
        PastSesh pastSesh = null;
        int pastSeshId = -1;

        try {
            if (pastSeshObject.has("past_sesh_id")) {
                pastSeshId = pastSeshObject.getInt("past_sesh_id");

                List<PastSesh> pastSeshesFound = PastSesh.find(PastSesh.class, "past_sesh_id = ?", Integer.toString(pastSeshId));

                if (pastSeshesFound.size() > 0) {
                    pastSesh = pastSeshesFound.get(0);
                } else {
                    pastSesh = new PastSesh();
                }
            } else {
                pastSesh = new PastSesh();
            }

            pastSesh.pastSeshId = pastSeshId;
            pastSesh.className = pastSeshObject.getString("class_name");
            if (!pastSeshObject.getString("start_time").equals("null")) {
                pastSesh.startTime = formattedTime(pastSeshObject.getString("start_time")).getTime();
            }
            if (!pastSeshObject.getString("end_time").equals("null")) {
                pastSesh.endTime = formattedTime(pastSeshObject.getString("end_time")).getTime();
            }

            JSONObject studentObject = pastSeshObject.getJSONObject("student");
            JSONObject tutorObject = pastSeshObject.getJSONObject("tutor");

            pastSesh.studentFullName = studentObject.getString("full_name");
            pastSesh.studentMajor = studentObject.getString("major");
            pastSesh.studentUserId = studentObject.getInt("user_id");
            pastSesh.studentId = studentObject.getInt("id");
            pastSesh.studentProfilePicture = studentObject.getString("profile_picture");

            pastSesh.tutorFullName = tutorObject.getString("full_name");
            pastSesh.tutorMajor = tutorObject.getString("major");
            pastSesh.tutorUserId = tutorObject.getInt("user_id");
            pastSesh.tutorId = tutorObject.getInt("id");
            pastSesh.tutorProfilePicture = tutorObject.getString("profile_picture");

            pastSesh.creditsUsed = pastSeshObject.getDouble("credits_used");
            pastSesh.paymentUsed = pastSeshObject.getDouble("payment_used");
            pastSesh.descr = pastSeshObject.getString("description");
            pastSesh.cost = pastSeshObject.getDouble("cost");
            pastSesh.tutorEarnings = pastSeshObject.getDouble("tutor_earnings");
            pastSesh.wasCancelled = (pastSeshObject.getInt("was_cancelled") == 1);
            pastSesh.cancellationCharge = pastSeshObject.getDouble("cancellation_charge");
            pastSesh.pastRequestId = pastSeshObject.getInt("past_request_id");

            pastSesh.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update past sesh; " + e.getMessage());
            return null;
        }

        return pastSesh;
    }

    private static Date formattedTime(String rawTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();
        return formatter.parseDateTime(rawTimeString).toDate();
    }
}
