package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by nadavhollander on 7/14/15.
 */
public class PromoteFragment extends Fragment implements FragmentOptionsReceiver {
    private Map<String, Object> options;

    private SeshButton redeemButton;
    private SeshEditText redeemEditText;
    private SeshActivityIndicator seshActivityIndicator;
    private ImageButton fbShareButton, tweetButton, instaButton;

    private SeshButton shareButton;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.promote_fragment, null);

        // Add padding to account for action bar
        LayoutUtils utils = new LayoutUtils(getActivity());
        RelativeLayout promoteLayout = (RelativeLayout) v.findViewById(R.id.promote_layout);
        promoteLayout.setPadding(0, utils.getActionBarHeightPx(), 0, 0);

        redeemButton = (SeshButton) v.findViewById(R.id.promote_redeem_button);

        seshActivityIndicator = (SeshActivityIndicator) v.findViewById(R.id.promote_redeem_activity_indicator);

        redeemEditText = (SeshEditText) v.findViewById(R.id.promote_promo_code_edit_text);

        View.OnClickListener onButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = null;
                if (v == fbShareButton) {
                    link = "https://www.facebook.com/seshtutoring";
                } else if (v == tweetButton) {
                    link = "https://twitter.com/seshtutoring";
                } else {
                    link = "https://instagram.com/sesh_tutoring/";
                }

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);
            }
        };

        fbShareButton = (ImageButton) v.findViewById(R.id.promote_share_button);
        tweetButton = (ImageButton) v.findViewById(R.id.promote_tweet_button);
        instaButton = (ImageButton) v.findViewById(R.id.promote_insta_button);

        fbShareButton.setOnClickListener(onButtonClickListener);
        tweetButton.setOnClickListener(onButtonClickListener);
        instaButton.setOnClickListener(onButtonClickListener);

        shareButton = (SeshButton) v.findViewById(R.id.promote_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Try out Sesh!");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Try out Sesh Tutoring, a mobile tutoring app for college campuses. Get instant in-person help on assignments from students who've previously aced the class! Visit http://seshtutoring.com to learn more!");

                getActivity().startActivity(Intent.createChooser(shareIntent, "Share Sesh!"));
            }
        });

        redeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNetworking(true);
                final SeshNetworking seshNetworking = new SeshNetworking(getActivity());
                seshNetworking.redeemCode(redeemEditText.getText(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        setNetworking(false);
                        try {
                            if (jsonObject.getString("status").equals("SUCCESS")) {
                                presentDialog("Success", jsonObject.getString("message"));
                                User.fetchUserInfoFromServer(getActivity());
                            } else {
                                presentDialog("Error", jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            presentDialog("Error", "Something went wrong.  Try again later.");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        setNetworking(false);
                        presentDialog("Error", "Something went wrong.  Try again later.");
                    }
                });
            }
        });

        return v;
    }

    private void setNetworking(Boolean networking) {
        seshActivityIndicator
                .animate()
                .alpha(networking ? 1f : 0f)
                .setDuration(300)
                .setStartDelay(0)
                .start();
    }

    private void presentDialog(String title, String message) {
        SeshDialog.showDialog(getActivity().getFragmentManager(), title,
                message,
                "OKAY", null, "error");
    }

    private void shareFB() {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Content to share");
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent, 0);
        for (final ResolveInfo app : activityList) {
            if ((app.activityInfo.name).contains("facebook")) {
                final ActivityInfo activity = app.activityInfo;
                final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |             Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                shareIntent.setComponent(name);
                getActivity().startActivity(shareIntent);
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainContainerActivity)getActivity()).onFragmentReplacedAndRendered();
    }

    @Override
    public void updateFragmentOptions(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public void clearFragmentOptions() {
        this.options = null;
    }
}
