package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by zacharysaraf on 9/11/15.
 */
public class OutstandingCharge extends SugarRecord<OutstandingCharge> {
    @Ignore
    public static final String TAG = OutstandingCharge.class.getName();

    public int outstandingChargeId;
    public Double amount;
    public int pastSeshId;
    public Student student;

    public OutstandingCharge() {}

    public static OutstandingCharge createOrUpdateOutstandingChargeWithObject(Context context, JSONObject jsonObject) {
        OutstandingCharge outstandingCharge = null;
        try {
            int outstandingChargeId = jsonObject.getInt("id");

            List<OutstandingCharge> chargesFound = OutstandingCharge.find(OutstandingCharge.class, "outstanding_charge_id = ?",
                    Integer.toString(outstandingChargeId));

            if (chargesFound.size() > 0) {
                outstandingCharge = chargesFound.get(0);
            } else {
                outstandingCharge = new OutstandingCharge();
            }

            outstandingCharge.outstandingChargeId = outstandingChargeId;
            outstandingCharge.amount = jsonObject.getDouble("amount_owed") - jsonObject.getDouble("amount_payed");
            outstandingCharge.pastSeshId = jsonObject.getInt("past_sesh_id");
            outstandingCharge.student = User.currentUser(context).student;

            outstandingCharge.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update discount object; JSON malformed: " + e);
        }
        return outstandingCharge;
    }

}
