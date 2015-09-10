package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by nadavhollander on 9/10/15.
 */
public class Discount extends SugarRecord<Discount> {
    @Ignore
    public static final String TAG = Discount.class.getName();

    public float creditAmount;
    public String bannerMessage;
    public String bannerHeader;
    public int discountId;
    public String learnRequestTitle;
    public User user;

    public Discount() {}

    public static Discount createOrUpdateDiscountWithObject(Context context, JSONObject jsonObject) {
        Discount discount = null;
        try {
            int discountId = jsonObject.getInt("id");

            List<Discount> discountsFound = Discount.find(Discount.class, "discount_id = ?",
                    Integer.toString(discountId));

            if (discountsFound.size() > 0) {
                discount = discountsFound.get(0);
            } else {
                discount = new Discount();
            }

            discount.discountId = discountId;
            discount.creditAmount = (float) jsonObject.getDouble("credit_amount");
            discount.bannerHeader = jsonObject.getString("banner_header");
            discount.bannerMessage = jsonObject.getString("banner_message");
            discount.learnRequestTitle = jsonObject.getString("learn_request_title");
            discount.user = User.currentUser(context);

            discount.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update discount object; JSON malformed: " + e);
        }
        return discount;
    }
}
