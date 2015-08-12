package com.seshtutoring.seshapp.view.fragments.WarmWelcomeFragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.AuthenticationActivity;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.TextureVideoView;

/**
 * Created by nadavhollander on 8/10/15.
 */
public class FourthWelcomeFragment  extends Fragment {
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        View v = layoutInflater.inflate(R.layout.fourth_welcome_fragment, null);

        TextureVideoView videoView = (TextureVideoView) v.findViewById(R.id.office_time_lapse);
        videoView.setDataSource(getActivity(), Uri.parse("android.resource://com.seshtutoring.seshapp/" + R.raw.office_timelapse_small));
        videoView.setLooping(true);
        videoView.play();
        return v;
    }
}
