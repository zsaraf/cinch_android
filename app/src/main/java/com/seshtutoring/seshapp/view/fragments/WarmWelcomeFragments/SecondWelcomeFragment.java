package com.seshtutoring.seshapp.view.fragments.WarmWelcomeFragments;

import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.WarmWelcomeActivity;
import com.seshtutoring.seshapp.view.components.SeshButton;

/**
 * Created by nadavhollander on 8/10/15.
 */
public class SecondWelcomeFragment extends Fragment {
    private TextView title;
    private TextView content;
    private CardView cardView;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.second_welcome_fragment, null);

        this.title = (TextView) v.findViewById(R.id.welcome_fragment_title);
        this.content = (TextView) v.findViewById(R.id.welcome_fragment_content);
        this.cardView = (CardView) v.findViewById(R.id.card);

        adjustCardViewForCurrentAPI();
        return v;
    }

    private void adjustCardViewForCurrentAPI() {
        ViewGroup.MarginLayoutParams titleParams = (ViewGroup.MarginLayoutParams) title.getLayoutParams();
        ViewGroup.MarginLayoutParams contentParams = (ViewGroup.MarginLayoutParams) content.getLayoutParams();

        LayoutUtils utils = new LayoutUtils(getActivity());

        if (Build.VERSION.SDK_INT < 21) {
            titleParams.topMargin = utils.dpToPixels(18);

            contentParams.leftMargin = utils.dpToPixels(20);
            contentParams.rightMargin = utils.dpToPixels(20);
            contentParams.topMargin = utils.dpToPixels(65);

            cardView.setPreventCornerOverlap(false);
        } else {
            titleParams.topMargin = utils.dpToPixels(20);

            contentParams.leftMargin = utils.dpToPixels(30);
            contentParams.rightMargin = utils.dpToPixels(30);
            contentParams.topMargin = utils.dpToPixels(80);
        }

        title.setLayoutParams(titleParams);
        content.setLayoutParams(contentParams);
    }
}
