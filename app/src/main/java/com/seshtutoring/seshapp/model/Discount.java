package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by nadavhollander on 9/10/15.
 */
public class Discount extends SugarRecord<Discount> {
    @Ignore public static final String TAG = Discount.class.getName();
    @Ignore public static final String BANNER_TITLE_KEY = "discount_banner_title";
    @Ignore public static final String BANNER_MESSAGE_KEY = "discount_banner_message";

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

    public static void displayDiscountNotificationIfNecessary(Context context) {
        (new CreateDiscountNotificationAsyncTask()).execute(context);
    }

    private static class CreateDiscountNotificationAsyncTask extends AsyncTask<Context, Void, Discount> {
        private Context mContext;

        @Override
        public Discount doInBackground(Context... params) {
            mContext = params[0];
            Discount discount = null;

            User currentUser = User.currentUser(mContext);
            List<Discount> discounts = currentUser.getDiscounts();
            if (discounts.size() > 0
                    && LearnRequest.listAll(LearnRequest.class).size() == 0
                    && Sesh.find(Sesh.class, "is_student = ?", "1").size() == 0) {
                discount = discounts.get(0);
            }

            return discount;
        }

        @Override
        public void onPostExecute(Discount discount) {
            if (discount != null) {
                Intent notificationIntent = new Intent(SeshNotificationManagerService.CREATE_DISCOUNT_NOTIFICATION_ACTION, null,
                        mContext, SeshNotificationManagerService.class);
                notificationIntent.putExtra(BANNER_TITLE_KEY, discount.bannerHeader);
                notificationIntent.putExtra(BANNER_MESSAGE_KEY, discount.bannerMessage);
                mContext.startService(notificationIntent);
            }
        }
    }
}
