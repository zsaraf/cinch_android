package com.seshtutoring.seshapp.view.fragments.OnboardingFragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.OnboardingActivity;
import com.seshtutoring.seshapp.view.OnboardingActivity.OnboardingRequirement;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshViewPager;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nadavhollander on 9/12/15.
 */
public class OnboardingPhotoFragment extends SeshViewPager.InputFragment {
    private static final String TAG = OnboardingPhotoFragment.class.getName();
    private static final int REQUEST_TAKE_PHOTO = 1337;
    private SeshViewPager seshViewPager;
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

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.onboarding_photo_fragment, container, false);

        this.profilePicture = (CircleImageView) view.findViewById(R.id.photo);
        this.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateOptionsCardUp();
            }
        });

        Bundle args;
        if (savedInstanceState != null) {
            args = savedInstanceState;
        } else {
            args = getArguments();
        }

        TextView addPhotoLabel = (TextView) view.findViewById(R.id.add_photo_label);
        String addPhotoText;
        if (args.getBoolean(OnboardingActivity.IS_STUDENT_ONBOARDING_KEY)) {
            addPhotoText = "Add a photo so your tutor knows who you are!";
        } else {
            addPhotoText = "Add a photo so your student knows who you are!";
        }
        addPhotoLabel.setText(addPhotoText);

        LayoutUtils utils = new LayoutUtils(getActivity());

        this.addPhotosOptionsCardView = (CardView) view.findViewById(R.id.add_photo_options_card_view);
        addPhotosOptionsCardView.setY(utils.getScreenHeightPx());

        this.blackOverlay = view.findViewById(R.id.black_overlay);
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

        SeshButton takePhotoButton = (SeshButton) view.findViewById(R.id.take_photo_button);
        SeshButton chooseFromGalleryButton = (SeshButton) view.findViewById(R.id.choose_from_gallery_button);
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
        return view;
    }

    private void animateOptionsCardUp() {
        LayoutUtils utils = new LayoutUtils(getActivity());
        blackOverlay.setVisibility(View.VISIBLE);
        blackOverlay.animate().setListener(null).alpha(0.8f).setDuration(300).start();
        spring.setCurrentValue(addPhotosOptionsCardView.getY());
        spring.setEndValue(utils.getScreenHeightPx() - utils.dpToPixels(35) - addPhotosOptionsCardView.getHeight());
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

    public void attachSeshViewPager(SeshViewPager seshViewPager) {
        this.seshViewPager = seshViewPager;
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
            Log.e(TAG, "Failed to open Camera app, couldn't create photo file: " + ex);
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
                Crop.of(photoUri, croppedPhotoUri).asSquare().start(getActivity(), this);
            } else {
                Log.e(TAG, "Something went wrong, failed to get photo from device.");
            }
        } else if (requestCode == Crop.REQUEST_PICK) {
            Crop.of(intent.getData(), croppedPhotoUri).asSquare().start(getActivity(), this);
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            try {
                photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), croppedPhotoUri);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(photo, 600, 600, false);
                FileOutputStream out = new FileOutputStream(resizedPhotoFile);
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                profilePicture.setImageBitmap(resizedBitmap);

                isCompleted = true;
                User currentUser = User.currentUser(getActivity());
                currentUser.profilePictureUrl = "local_saved_image.png";
                currentUser.save();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((OnboardingActivity) getActivity())
                                .setRequirementFulfilled(OnboardingRequirement.PROFILE_PICTURE);
                    }
                }, 1000);
            } catch (IOException e) {
                Log.e(TAG, "Failed to take picture, couldn't retrieve saved image" + e);
            }
        }
    }
}
