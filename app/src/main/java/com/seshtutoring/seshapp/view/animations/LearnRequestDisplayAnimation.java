package com.seshtutoring.seshapp.view.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.ContainerState;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerStateManager;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment.SideMenuOpenAnimation;
import com.seshtutoring.seshapp.view.fragments.ViewRequestFragment;
import com.stripe.android.compat.AsyncTask;

/**
 * Created by nadavhollander on 8/31/15.
 */
public class LearnRequestDisplayAnimation extends SideMenuOpenAnimation {
    private MainContainerStateManager containerStateManager;
    private LearnRequest learnRequest;
    private LinearLayout contents;
    private Spring spring;


    public LearnRequestDisplayAnimation(MainContainerStateManager containerStateManager,
                                        LearnRequest learnRequest, View learnRequestRowItem) {
        this.containerStateManager = containerStateManager;
        this.learnRequest = learnRequest;
        this.contents = (LinearLayout) learnRequestRowItem.findViewById(R.id.contents);
        this.spring = SpringSystem.create().createSpring();
        spring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(60, 7));
        spring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                contents.setX((int) spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                onAnimationCompleted();
            }
        });
    }

    public void prepareAnimation() {
        contents.setX(contents.getWidth());
    }

    public void startAnimation() {
        spring.setCurrentValue(contents.getWidth());
        spring.setEndValue(0);
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
            containerStateManager.setContainerStateForLearnRequest(learnRequest);
            containerStateManager.closeDrawerWithDelay(1000);
        }
    }
}
