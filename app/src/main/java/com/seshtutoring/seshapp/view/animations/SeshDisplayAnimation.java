package com.seshtutoring.seshapp.view.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.view.ContainerState;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.ViewRequestFragment;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragment;
import com.stripe.android.compat.AsyncTask;

/**
 * Created by nadavhollander on 8/31/15.
 */
public class SeshDisplayAnimation extends SideMenuFragment.SideMenuOpenAnimation {
    private MainContainerActivity mainContainerActivity;
    private Sesh sesh;
    private ViewGroup seshRowParentView;
    private View seshRowContents;
    private LinearLayout dummyPastRequestContents;

    public SeshDisplayAnimation(MainContainerActivity mainContainerActivity, Sesh sesh, View seshRowContents) {
        this.mainContainerActivity = mainContainerActivity;
        this.sesh = sesh;
        this.seshRowContents = seshRowContents.findViewById(R.id.contents);

        if (sesh.isStudent) {
            LayoutInflater inflater = mainContainerActivity.getLayoutInflater();
            ViewGroup dummyPastRequestParentView = (ViewGroup) inflater.inflate(R.layout.open_request_list_row, null);
            this.dummyPastRequestContents =
                    (LinearLayout) dummyPastRequestParentView.findViewById(R.id.contents);
            dummyPastRequestParentView.removeView(dummyPastRequestContents);

            TextView classAbbrvTextView =
                    (TextView) dummyPastRequestContents.findViewById(R.id.open_request_list_row_class);
            classAbbrvTextView.setText(sesh.className);
        }
    }

    public void prepareAnimation() {
        seshRowContents.setX(seshRowContents.getWidth());

        if (sesh.isStudent) {
            this.seshRowParentView = (ViewGroup) seshRowContents.getParent();
            seshRowParentView.addView(dummyPastRequestContents, 0);
        }
    }

    public void startAnimation() {
        seshRowContents.animate().x(0).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                onAnimationCompleted();
            }
        }).start();
    }

    public void onAnimationCompleted() {
        if (sesh.isStudent) {
            seshRowParentView.removeView(dummyPastRequestContents);
        }
        (new AnimationCompleteAsyncTask()).execute();
    }

    private class AnimationCompleteAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        public Void doInBackground(Void... params) {
            sesh.requiresAnimatedDisplay = false;
            sesh.save();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mainContainerActivity.setCurrentState(new ContainerState("Sesh!", 0,
                    ViewSeshFragment.newInstance(sesh.seshId, false)));
            Handler mainThread = new Handler();
            mainThread.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainContainerActivity.closeDrawer(true);
                    Notification.currentNotificationHandled(mainContainerActivity, true);
                }
            }, 1000);
        }
    }
}
