package com.seshtutoring.seshapp.view.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.ContainerState;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment.SideMenuOpenAnimation;
import com.seshtutoring.seshapp.view.fragments.ViewRequestFragment;
import com.stripe.android.compat.AsyncTask;

/**
 * Created by nadavhollander on 8/31/15.
 */
public class LearnRequestDisplayAnimation extends SideMenuOpenAnimation {
    private MainContainerActivity mainContainerActivity;
    private LearnRequest learnRequest;
    private LinearLayout contents;


    public LearnRequestDisplayAnimation(MainContainerActivity mainContainerActivity,
                                        LearnRequest learnRequest, View learnRequestRowItem) {
        this.mainContainerActivity = mainContainerActivity;
        this.learnRequest = learnRequest;
        this.contents = (LinearLayout) learnRequestRowItem.findViewById(R.id.contents);
    }

    public void prepareAnimation() {
        contents.setX(contents.getWidth());
    }

    public void startAnimation() {
        contents.animate().x(0).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                onAnimationCompleted();
            }
        }).start();
    }

    public void onAnimationCompleted() {
        (new AnimationCompleteAsyncTask()).execute();
    }

    private class AnimationCompleteAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        public Void doInBackground(Void... params) {
            learnRequest.requiresAnimatedDisplay = false;
            learnRequest.save();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mainContainerActivity.setCurrentState(new ContainerState("Request!", 0,
                    ViewRequestFragment.newInstance(learnRequest.learnRequestId)));
            Handler mainThread = new Handler();
            mainThread.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainContainerActivity.closeDrawer(true);
                }
            }, 1000);
        }
    }
}
