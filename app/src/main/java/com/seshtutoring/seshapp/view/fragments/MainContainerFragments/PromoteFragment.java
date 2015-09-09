package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;

import java.util.List;
import java.util.Map;

/**
 * Created by nadavhollander on 7/14/15.
 */
public class PromoteFragment extends Fragment implements FragmentOptionsReceiver {
    private Map<String, Object> options;

    ImageButton fbShareButton, tweetButton, messageButton, emailButton;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.promote_fragment, null);

        // Add padding to account for action bar
        LayoutUtils utils = new LayoutUtils(getActivity());
        RelativeLayout promoteLayout = (RelativeLayout) v.findViewById(R.id.promote_layout);
        promoteLayout.setPadding(15, utils.getActionBarHeightPx(), 15, 15);

        fbShareButton = (ImageButton) v.findViewById(R.id.promote_share_button);

        tweetButton = (ImageButton) v.findViewById(R.id.promote_tweet_button);

        messageButton = (ImageButton) v.findViewById(R.id.promote_message_button);

        emailButton = (ImageButton) v.findViewById(R.id.promote_email_button);

        View.OnClickListener onButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "share subject");
                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "share text");

                    getActivity().startActivity(Intent.createChooser(shareIntent, "Share Sesh!"));
            }
        };

        fbShareButton.setOnClickListener(onButtonClickListener);
        tweetButton.setOnClickListener(onButtonClickListener);
        messageButton.setOnClickListener(onButtonClickListener);
        emailButton.setOnClickListener(onButtonClickListener);

        return v;
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

    private void shareTweet() {

    }

    private void shareMessage() {

    }

    private void shareEmail() {

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
