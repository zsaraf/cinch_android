package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.util.DateUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.squareup.picasso.Callback;

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

        try {
            int pastSeshId = pastSeshObject.getInt("id");
            Course course = Course.createOrUpdateCourseWithJSON(pastSeshObject.getJSONObject("course"), null, true);
            String className = course.shortFormatForTextView();

            pastSeshId = pastSeshObject.getInt("id");

            List<PastSesh> pastSeshesFound = PastSesh.find(PastSesh.class, "past_sesh_id = ?", Integer.toString(pastSeshId));

            if (pastSeshesFound.size() > 0) {
                pastSesh = pastSeshesFound.get(0);
            } else {
                pastSesh = new PastSesh();
            }

            pastSesh.pastSeshId = pastSeshId;

            pastSesh.className = className;
            if (!pastSeshObject.getString("start_time").equals("null")) {
                pastSesh.startTime = DateUtils.djangoFormattedTime(pastSeshObject.getString("start_time")).getTime();
            }
            if (!pastSeshObject.getString("end_time").equals("null")) {
                pastSesh.endTime = DateUtils.djangoFormattedTime(pastSeshObject.getString("end_time")).getTime();
            }

            if (pastSeshObject.get("student") instanceof JSONObject) {
                JSONObject studentObject = pastSeshObject.getJSONObject("student");
                pastSesh.studentFullName = studentObject.getString("full_name");
                pastSesh.studentMajor = studentObject.getString("major");
                pastSesh.studentUserId = studentObject.getInt("user_id");
                pastSesh.studentId = studentObject.getInt("id");
                pastSesh.studentProfilePicture = studentObject.getString("profile_picture");
            } else {
                pastSesh.studentId = pastSeshObject.getInt("student");
            }

            if (pastSeshObject.get("tutor") instanceof JSONObject) {
                JSONObject tutorObject = pastSeshObject.getJSONObject("tutor");
                pastSesh.tutorFullName = tutorObject.getString("full_name");
                pastSesh.tutorMajor = tutorObject.getString("major");
                pastSesh.tutorUserId = tutorObject.getInt("user_id");
                pastSesh.tutorId = tutorObject.getInt("id");
                pastSesh.tutorProfilePicture = tutorObject.getString("profile_picture");
            } else {
                pastSesh.tutorId = pastSeshObject.getInt("tutor");
            }

            pastSesh.creditsUsed = pastSeshObject.getDouble("credits_used");
            pastSesh.paymentUsed = pastSeshObject.getDouble("payment_used");
            pastSesh.cost = pastSeshObject.getDouble("cost");
            pastSesh.tutorEarnings = pastSeshObject.getDouble("tutor_earnings");
            pastSesh.wasCancelled = pastSeshObject.getBoolean("was_cancelled");
            pastSesh.cancellationCharge = pastSeshObject.getDouble("cancellation_charge");
            pastSesh.pastRequestId = pastSeshObject.getInt("past_request");

            pastSesh.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update past sesh; " + e.getMessage());
            return null;
        }

        return pastSesh;
    }

    public boolean isStudent(Context context) {
        return (this.studentId == User.currentUser(context).student.studentId);
    }

}
