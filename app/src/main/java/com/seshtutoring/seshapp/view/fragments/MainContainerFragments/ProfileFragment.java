package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.CallbackManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshAnimatedCheckmark;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileBioViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileStudentViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileTutorViewFragment;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nadavhollander on 7/14/15.
 */

public class ProfileFragment extends Fragment implements FragmentOptionsReceiver {

    private static final int REQUEST_TAKE_PHOTO = 1337;

    private Map<String, Object> options;
    private User user;
    private MainContainerActivity mainContainerActivity;
    private View homeView;
    private ViewPager viewPager;
    private ImageView viewPagerDots;
    private ProfileBioViewFragment profileBioViewFragment;
    private ProfileStudentViewFragment profileStudentViewFragment;
    private ProfileTutorViewFragment profileTutorViewFragment;

    private CircleImageView profilePicture;
    private boolean isCompleted;
    private Bitmap photo;
    private File rawPhotoFile;
    private File croppedPhotoFile;
    private File resizedPhotoFile;
    private CardView addPhotosOptionsCardView;
    private View blackOverlay;
    private Spring spring;
    private boolean cardIsUp;
    private BroadcastReceiver broadcastReceiver;
    private SeshNetworking seshNetworking;
    private boolean shouldRefreshProfPic;

    private CallbackManager callbackManager;
    private String selectedImagePath;

    public RelativeLayout requestFlowOverlay;
    public SeshActivityIndicator activityIndicator;
    public SeshAnimatedCheckmark animatedCheckmark;


    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        mainContainerActivity = (MainContainerActivity) getActivity();

        this.homeView = layoutInflater.inflate(R.layout.profile_fragment, null);

        LayoutUtils layUtils = new LayoutUtils(getActivity());


        user = User.currentUser(mainContainerActivity.getApplicationContext());
        this.seshNetworking = new SeshNetworking(mainContainerActivity);

        //initializing fragments here might contribute to detachment issues, I think we're supposed to initialize in viewPager.getItem() instead so viewPager tracks reference
        //this.profileBioViewFragment = new ProfileBioViewFragment();
        //this.profileStudentViewFragment = new ProfileStudentViewFragment();
        //this.profileTutorViewFragment = new ProfileTutorViewFragment();

        this.requestFlowOverlay = (RelativeLayout) this.homeView.findViewById(R.id.request_flow_overlay);
        this.activityIndicator = (SeshActivityIndicator) this.homeView.findViewById(R.id.request_activity_indicator);
        this.animatedCheckmark = (SeshAnimatedCheckmark) this.homeView.findViewById(R.id.animated_check_mark);

        this.viewPagerDots = (ImageView) this.homeView.findViewById(R.id.view_pager_dots);
        this.viewPagerDots.setImageResource(R.drawable.sign_up_dots_page1);

        shouldRefreshProfPic = true;

        this.viewPager = (ViewPager) this.homeView.findViewById(R.id.view_pager);
        this.viewPager.setAdapter(new ProfileViewPagerAdapter(getFragmentManager()));
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

        this.profilePicture = (CircleImageView)this.homeView.findViewById(R.id.profile_picture);
        seshNetworking = new SeshNetworking(mainContainerActivity);
        seshNetworking.downloadProfilePictureAsync(user.profilePictureUrl, this.profilePicture, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });

        this.profilePicture = (CircleImageView) this.homeView.findViewById(R.id.profile_picture);
        this.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateOptionsCardUp();
                //old fb code
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
//                mainContainerActivity.facebookLogin();
            }
        });

        this.addPhotosOptionsCardView = (CardView) this.homeView.findViewById(R.id.add_photo_options_card_view);
        addPhotosOptionsCardView.setY(layUtils.getScreenHeightPx());

        this.blackOverlay = this.homeView.findViewById(R.id.black_overlay);
        blackOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cardIsUp) {
                    animateOptionsCardDown(null);
                }
            }
        });

        this.spring = SpringSystem.create().createSpring();
        spring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9, 6));
        spring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                addPhotosOptionsCardView.setY((int) spring.getCurrentValue());
            }
        });

        SeshButton takePhotoButton = (SeshButton) this.homeView.findViewById(R.id.take_photo_button);
        SeshButton chooseFromGalleryButton = (SeshButton) this.homeView.findViewById(R.id.choose_from_gallery_button);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateOptionsCardDown(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dispatchTakePictureIntent();
                    }
                });
            }
        });
        chooseFromGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateOptionsCardDown(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dispatchPickImageIntent();
                    }
                });
            }
        });

        isCompleted = false;
        cardIsUp = false;

        broadcastReceiver = actionBroadcastReceiver;

        return this.homeView;

    }

    private void animateOptionsCardUp() {
        LayoutUtils utils = new LayoutUtils(getActivity());
        blackOverlay.setVisibility(View.VISIBLE);
        blackOverlay.animate().setListener(null).alpha(0.8f).setDuration(300).start();
        spring.setCurrentValue(addPhotosOptionsCardView.getY());
        spring.setEndValue(((View) addPhotosOptionsCardView.getParent()).getHeight() - addPhotosOptionsCardView.getHeight());
        cardIsUp = true;
    }

    private void animateOptionsCardDown(final AnimatorListenerAdapter listener) {
        LayoutUtils utils = new LayoutUtils(getActivity());
        blackOverlay.animate().alpha(0.0f).setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        blackOverlay.setVisibility(View.GONE);
                        if (listener != null) {
                            listener.onAnimationEnd(animation);
                        }
                    }
                }).start();
        spring.setCurrentValue(addPhotosOptionsCardView.getY());
        spring.setEndValue(utils.getScreenHeightPx());
        cardIsUp = false;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    private void createTmpPictureFiles() {
        rawPhotoFile = null;
        croppedPhotoFile = null;
        resizedPhotoFile = null;
        try {
            rawPhotoFile = createImageFile();
            croppedPhotoFile = createImageFile();
            resizedPhotoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            createTmpPictureFiles();

            // Continue only if the File was successfully created
            if (rawPhotoFile != null  && croppedPhotoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(rawPhotoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void dispatchPickImageIntent() {
        createTmpPictureFiles();
        Crop.pickImage(getActivity(), this);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "SESH_PROFILE_PIC_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Uri photoUri = Uri.fromFile(rawPhotoFile);
        Uri croppedPhotoUri = Uri.fromFile(croppedPhotoFile);

        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap uncroppedBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
                    Log.i("coewkjfnas", "sfdkjfansd");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                shouldRefreshProfPic = false;
                Crop.of(photoUri, croppedPhotoUri).asSquare().start(getActivity(), this);
            } else {
            }
        } else if (requestCode == Crop.REQUEST_PICK) {
            if (resultCode == Activity.RESULT_OK) {
                shouldRefreshProfPic = false;
                Crop.of(intent.getData(), croppedPhotoUri).asSquare().start(getActivity(), this);
            }
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            shouldRefreshProfPic = false;
            try {
                photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), croppedPhotoUri);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(photo, 600, 600, false);
                try {
                    ExifInterface exifInterface = new ExifInterface(croppedPhotoUri.getPath());
                    int exifOrientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                    Log.i("EXIF ORIENTATION", exifOrientation + "");
                    resizedBitmap = rotateBitmap(resizedBitmap, exifOrientation);


                } catch (IOException e) {
                    e.printStackTrace();
                }

                FileOutputStream out = new FileOutputStream(resizedPhotoFile);
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);


                profilePicture.setImageBitmap(resizedBitmap);

                //upload photo
                seshNetworking.uploadProfilePicture(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject.getString("status").equals("SUCCESS")) {
                                //success handler
                                updateUserProfile((JSONObject)jsonObject.get("user"));
                            } else {
                                //server error hadler
                            }
                        } catch (JSONException e) {
                            //json exception handler
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //error response handler
                    }
                }, resizedPhotoFile);

                isCompleted = true;
                user.profilePictureUrl = "local_saved_image.png";
                user.save();
            } catch (IOException e) {
            }
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateUserProfile(JSONObject userObj) {

        try {
            String updatedUrl = userObj.getString("profile_picture");
            user.profilePictureUrl = updatedUrl;
            user.save();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_TABS = 3;
        public ProfileTutorViewFragment tutorViewFragment;
        public ProfileStudentViewFragment studentViewFragment;
        public ProfileBioViewFragment bioViewFragment;

        public ProfileViewPagerAdapter(FragmentManager fm) {
            super(fm);
            bioViewFragment =  new ProfileBioViewFragment();
            studentViewFragment = new ProfileStudentViewFragment();
            tutorViewFragment = new ProfileTutorViewFragment();
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return bioViewFragment;
            }else if (position == 1) {
                return studentViewFragment;
            }else {
                return tutorViewFragment;
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

        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.REFRESH_USER_INFO);

        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        shouldRefreshProfPic = true;
        this.getActivity().unregisterReceiver(broadcastReceiver);
    }

    private void refreshProfilePictureWithUser(User currentUser) {
        boolean needsRefreshing = !currentUser.profilePictureUrl.equals(user.profilePictureUrl);
        this.user = currentUser;
        if (needsRefreshing && shouldRefreshProfPic) {
            seshNetworking.downloadProfilePictureAsync(user.profilePictureUrl, this.profilePicture, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                }
            });
        }
    }

    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            User currentUser = User.currentUser(getActivity());
            refreshProfilePictureWithUser(currentUser);
        }
    };


    @Override
    public void updateFragmentOptions(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public void clearFragmentOptions() {
        this.options = null;
    }

}
