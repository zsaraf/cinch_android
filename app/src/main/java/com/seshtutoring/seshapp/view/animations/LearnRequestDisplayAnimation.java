package com.seshtutoring.seshapp.view.animations;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment.SideMenuOpenAnimation;

/**
 * Created by nadavhollander on 8/31/15.
 */
public class LearnRequestDisplayAnimation extends SideMenuOpenAnimation {
    private Context mContext;
    private LinearLayout contents;


    public LearnRequestDisplayAnimation(Context context, View learnRequestRowItem) {
        this.mContext = context;
        this.contents = (LinearLayout) learnRequestRowItem.findViewById(R.id.contents);
    }

    public void prepareAnimation() {
        contents.setX(contents.getWidth());
    }

    public void startAnimation() {
        contents.animate().x(0).setDuration(300).start();
    }
}
