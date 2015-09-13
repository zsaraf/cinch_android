package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.JsonMultipartRequest;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.VolleyNetworkingWrapper;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;
import com.seshtutoring.seshapp.view.components.SeshIconTextView;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileBioViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileStudentViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileTutorViewFragment;
import com.seshtutoring.seshapp.view.fragments.TeachViewFragment;
import com.squareup.picasso.Callback;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nadavhollander on 7/14/15.
 */

public class ProfileFragment extends Fragment implements FragmentOptionsReceiver {

    private static final int SELECT_PICTURE = 1;

    private Map<String, Object> options;
    private User user;
    private MainContainerActivity mainContainerActivity;
    private View homeView;
    private ViewPager viewPager;
    private ImageView viewPagerDots;
    private ProfileBioViewFragment profileBioViewFragment;
    private ProfileStudentViewFragment profileStudentViewFragment;
    private ProfileTutorViewFragment profileTutorViewFragment;

    private String selectedImagePath;


    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.homeView = layoutInflater.inflate(R.layout.profile_fragment, null);

        LayoutUtils layUtils = new LayoutUtils(getActivity());
        this.homeView.setPadding(0, layUtils.getActionBarHeightPx(), 0, 0);

        mainContainerActivity = (MainContainerActivity) getActivity();
        user = User.currentUser(mainContainerActivity.getApplicationContext());

        this.profileBioViewFragment = new ProfileBioViewFragment();
        this.profileStudentViewFragment = new ProfileStudentViewFragment();
        this.profileTutorViewFragment = new ProfileTutorViewFragment();

        this.viewPagerDots = (ImageView) this.homeView.findViewById(R.id.view_pager_dots);
        this.viewPagerDots.setImageResource(R.drawable.sign_up_dots_page1);

        this.viewPager = (ViewPager) this.homeView.findViewById(R.id.view_pager);
        this.viewPager.setAdapter(new ProfileViewPagerAdapter(getChildFragmentManager()));
        this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    viewPagerDots.setImageResource(R.drawable.sign_up_dots_page1);
                } else if (position == 1) {
                    viewPagerDots.setImageResource(R.drawable.sign_up_dots_page2);
                } else if (position == 2) {
                    viewPagerDots.setImageResource(R.drawable.sign_up_dots_page3);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        final ImageView profileImageView = (ImageView)this.homeView.findViewById(R.id.profile_picture);
        SeshNetworking seshNetworking = new SeshNetworking(mainContainerActivity);
        seshNetworking.downloadProfilePictureAsync(user.profilePictureUrl, profileImageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

            }
        });

        return this.homeView;

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseJson) {
                        onUploadResponse(responseJson);
                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        int i = 0;
                    }
                };

                Map<String, String> params = new HashMap<>();
                params.put("session_id", SeshAuthManager.sharedManager(mainContainerActivity).getAccessToken());
                JsonMultipartRequest request = new JsonMultipartRequest("https://www.cinchtutoring.com/users/lilli/upload_profile_picture.php", SeshAuthManager.sharedManager(mainContainerActivity).getAccessToken(), responseListener, errorListener, getRealPathFromURI(selectedImageUri));
                VolleyNetworkingWrapper.getInstance(mainContainerActivity).addToRequestQueue(request);
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(contentUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = mainContainerActivity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    private void onUploadResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                int i = 0;
            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
            }
        } catch (JSONException e) {
        }
    }

    private class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_TABS = 3;

        public ProfileViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return profileBioViewFragment;
            }else if (position == 1) {
                return profileStudentViewFragment;
            }else {
                return profileTutorViewFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_TABS;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            // Do nothing here!!  A bit hacky, but this is a fix for something weird in Android's
            // way of handling ViewPagers within fragments
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
