package com.seshtutoring.seshapp.util.networking;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Response;
import com.seshtutoring.seshapp.SeshApplication;

import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Wrapper for any asynchronous interactions with the API.
 */
public class SeshNetworking {
    private static final String TAG = SeshNetworking.class.getName();

    private Context mContext;

    public SeshNetworking(Context context) {
        this.mContext = context;
    }

    public void postWithRelativeUrl(String relativeUrl, JSONObject params,
                                          Response.Listener<JSONObject> successListener,
                                          Response.ErrorListener errorListener) {
        String absoluteUrl = baseUrl() + relativeUrl;

        Log.i(TAG, "Issuing POST request to URL:  " + absoluteUrl + "with params: " +
                params.toString());
        JsonPostRequestWithAuth requestWithAuth = new JsonPostRequestWithAuth(absoluteUrl,
                params, successListener, errorListener);
        Log.i(TAG, "Params body looks like this:\n" +  requestWithAuth.getBody());


        VolleyNetworkingWrapper.getInstance(mContext).addToRequestQueue(requestWithAuth);
    }

    public void signUpWithName(String name, String email, String password,
                               Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        JSONObject params = new JSONObject();
        try  {
            params.put("format", "json");
            params.put("email", email);
            params.put("full_name", name);
            params.put("password", password);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

        postWithRelativeUrl("create_user.php", params, successListener, errorListener);
    }

    public void loginWithEmail(String email, String password,
                               Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        JSONObject params = new JSONObject();
        try  {
            params.put("format", "json");
            params.put("email", email);
            params.put("password", password);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

        postWithRelativeUrl("login.php", params, successListener, errorListener);
    }

    public void forgotPasswordWithEmail(String email,
                               Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        JSONObject params = new JSONObject();
        try  {
            params.put("email", email);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

        postWithRelativeUrl("forgot_password.php", params, successListener, errorListener);
    }

//    @TODO
//    implement once Stripe functionality in place
//    public void addCardWithCustomerToken(...)
//    public void getCardsForCurrentUserWithSuccess(...)
//    public void deleteCardWithId(...)
//    public void makeDefaultCard(...)

    public void searchForClassName(String className,
                                        Response.Listener<JSONObject> successListener,
                                        Response.ErrorListener errorListener) {
        JSONObject params = new JSONObject();
        try  {
            params.put("format", "json");

        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

        postWithRelativeUrl("forgot_password.php", params, successListener, errorListener);
    }

    private String baseUrl() {
        String baseUrl;
        if (SeshApplication.IS_LIVE) {
            baseUrl = "https://www.seshtutoring.com/ios-php/";
        } else {
            baseUrl = "https://www.cinchtutoring.com/ios-php/";
        }
        return baseUrl;
    }
}
