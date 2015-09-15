package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.Image;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.ArraySwipeAdapter;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.AvailableJob;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.LocationManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.OnboardingActivity;
import com.seshtutoring.seshapp.view.OnboardingActivity.OnboardingRequirement;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshIconTextView;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

/**
 * Created by lillioetting on 7/14/15.
 */
public class ViewAvailableJobsFragment extends ListFragment {
    private static final String TAG =ViewAvailableJobsFragment.class.getName();

    private static final int REFRESH_INTERVAL_MILI = 15000;

    private MainContainerActivity mainContainerActivity;
    //    private SwipeRefreshLayout refreshLayout;
    private Typeface boldTypeFace;
    private ArrayList<JobHolder> availableJobs;
    private ArrayList<Course> tutorCourses;
    private ViewAvailableJobsAdapter availableJobsAdapter;
    private SeshNetworking seshNetworking;
    private Queue<ViewHolder> bidQueue;
    private Handler handler;
    private ListFragmentSwipeRefreshLayout mSwipeRefreshLayout;
    private TextView brokenPencilTextView;
    private BroadcastReceiver broadcastReceiver;
    private LocationManager locationManager;

    private class JobHolder {
        public AvailableJob job;
        public int type;
        public int status;
        public boolean selected;

        public void select() {
            this.selected = true;
        }

        public JobHolder(AvailableJob job, int type) {
            this.type = type;
            this.job = job;
            this.selected = false;
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            getAvailableJobs();
            handler.postDelayed(mStatusChecker, REFRESH_INTERVAL_MILI);
        }
    };

    public static String fmtDistance(double d)
    {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%s",d);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.locationManager = LocationManager.sharedInstance(getActivity());
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout v = (RelativeLayout) layoutInflater.inflate(R.layout.view_available_jobs_fragment, null);

        // Now create a SwipeRefreshLayout to wrap the fragment's content view
        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(getActivity());

        // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
        // the SwipeRefreshLayout
        mSwipeRefreshLayout.addView(v,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Make sure that the SwipeRefreshLayout will fill the fragment
        mSwipeRefreshLayout.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAvailableJobs();
            }
        });

        // Now return the SwipeRefreshLayout as this fragment's content view
        return mSwipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mainContainerActivity = (MainContainerActivity) getActivity();
        boldTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Book.otf");

        brokenPencilTextView = (TextView) this.getView().findViewById(R.id.broken_text_view);
        brokenPencilTextView.setTypeface(boldTypeFace);
        this.seshNetworking = new SeshNetworking(getActivity());
        //this.bidQueue = new LinkedList<ViewHolder>();
        this.handler = new Handler();

        this.availableJobs = new ArrayList<JobHolder>();
        this.tutorCourses = new ArrayList<Course>();
        this.availableJobsAdapter = new ViewAvailableJobsAdapter(getActivity(), availableJobs);
        getListView().setAdapter(availableJobsAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = ViewAvailableJobsFragment.this.getListView();
                int first = listView.getFirstVisiblePosition();
                int count = listView.getChildCount();
                for (int i = first; i < count; i++) {
                    View t = (View) listView.getChildAt(i);
                    ViewHolder viewHolder = (ViewHolder) t.getTag();
                    if (viewHolder != null) {
                        viewHolder.setConfirmMode(false);
                    }
                }
            }
        });
//        getListView().setOnItemClickListener(new View.OnItemClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        //get available jobs from server
        getAvailableJobs();
    }

    @Override
    public void onResume() {
        super.onResume();

        broadcastReceiver = actionBroadcastReceiver;
        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.REFRESH_JOBS);
        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().unregisterReceiver(broadcastReceiver);
    }


    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getAvailableJobs();
        }
    };

    private class ViewHolder {
        public TextView nameTextView;
        public TextView rateTextView;
        public ImageView checkImageView;
        public SeshInformationLabel courseInformationLabel;
        public SeshInformationLabel assignmentInformationLabel;
        public SeshInformationLabel distanceInformationLabel;
        public SeshInformationLabel durationInformationLabel;
        public SeshInformationLabel availableBlocksInformationLabel;
        public Button acceptButton;

        private SpringSystem springSystem;
        public Spring animationSpring;

        public ViewGroup topGroup;

        public boolean confirmMode;

        private void setConfirmMode(Boolean confirmMode) {
            if (confirmMode == true) {
                this.confirmMode = true;
                this.acceptButton.setText("Confirm");
                int sdk = android.os.Build.VERSION.SDK_INT;
                Drawable drawable = null;
                if (sdk < Build.VERSION_CODES.LOLLIPOP) {
                    drawable = getActivity().getResources().getDrawable(R.drawable.sesh_green_border_button);
                } else {
                    drawable = getActivity().getResources().getDrawable(R.drawable.sesh_green_border_button, null);
                }
                if(sdk < Build.VERSION_CODES.JELLY_BEAN) {
                    this.acceptButton.setBackgroundDrawable(drawable);
                    this.acceptButton.setTextColor(getActivity().getResources().getColorStateList(R.color.sesh_green_border_button_text_color));
                } else {
                    this.acceptButton.setBackground(drawable);
                    this.acceptButton.setTextColor(getActivity().getResources().getColorStateList(R.color.sesh_green_border_button_text_color));
                }
            } else {
                this.confirmMode = false;
                this.acceptButton.setText("Accept");
                int sdk = android.os.Build.VERSION.SDK_INT;
                Drawable drawable = null;
                if (sdk < Build.VERSION_CODES.LOLLIPOP) {
                    drawable = getActivity().getResources().getDrawable(R.drawable.sesh_gray_border_button);
                } else {
                    drawable = getActivity().getResources().getDrawable(R.drawable.sesh_gray_border_button, null);
                }
                if(sdk < Build.VERSION_CODES.JELLY_BEAN) {
                    this.acceptButton.setBackgroundDrawable(drawable);
                    this.acceptButton.setTextColor(getActivity().getResources().getColorStateList(R.color.sesh_gray_border_button_text_color));
                } else {
                    this.acceptButton.setBackground(drawable);
                    this.acceptButton.setTextColor(getActivity().getResources().getColorStateList(R.color.sesh_gray_border_button_text_color));
                }
            }

        }
    }

    public class ViewAvailableJobsAdapter extends ArrayAdapter<JobHolder> {

        private Context mContext;
        private LayoutInflater layoutInflater;

        public ViewAvailableJobsAdapter(Context context, ArrayList<JobHolder> availableJobs) {
            super(context, R.layout.view_available_jobs_row, availableJobs);
            this.mContext = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getSwipeLayoutResourceId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            final JobHolder holder = (JobHolder) getItem(position);
            final AvailableJob item = holder.job;
            final ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_available_jobs_row, null);

                viewHolder = new ViewHolder();

                viewHolder.springSystem = SpringSystem.create();
                viewHolder.confirmMode = false;

                viewHolder.topGroup = (ViewGroup) convertView.findViewById(R.id.top_wrapper);

                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.student_name);
                viewHolder.nameTextView.setTypeface(boldTypeFace);

                viewHolder.rateTextView = (TextView) convertView.findViewById(R.id.hourly_rate);

                viewHolder.courseInformationLabel = (SeshInformationLabel) convertView.findViewById(R.id.course);

                viewHolder.assignmentInformationLabel = (SeshInformationLabel) convertView.findViewById(R.id.assignment);

                viewHolder.distanceInformationLabel = (SeshInformationLabel) convertView.findViewById(R.id.distance);

                viewHolder.durationInformationLabel = (SeshInformationLabel) convertView.findViewById(R.id.duration);

                viewHolder.availableBlocksInformationLabel = (SeshInformationLabel) convertView.findViewById(R.id.available_blocks);

                viewHolder.checkImageView = (ImageView) convertView.findViewById(R.id.check_mark);

                viewHolder.acceptButton = (Button) convertView.findViewById(R.id.accept_button);

                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    if (!viewHolder.confirmMode) {
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                viewHolder.setConfirmMode(true);
                            }
                        }, 100);
                    } else {
                        (new VerifyTutorOnboardingAsyncTask() {
                            @Override
                            public void onPostExecute(ArrayList<OnboardingRequirement> onboardingRequirements) {
                                if (onboardingRequirements.size() > 0) {
                                    showOnboardingDialog(onboardingRequirements);
                                } else {
                                    viewHolder.acceptButton.
                                            animate()
                                            .setDuration(200)
                                            .alpha(0)
                                            .scaleX(.01f)
                                            .scaleY(.01f)
                                            .y(viewHolder.availableBlocksInformationLabel.getY() +
                                                    viewHolder.availableBlocksInformationLabel.getMeasuredHeight() -
                                                    viewHolder.acceptButton.getMeasuredHeight())
                                            .setListener(new Animator.AnimatorListener() {
                                                @Override
                                                public void onAnimationStart(Animator animation) {

                                                }

                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    ((JobHolder) viewHolder.nameTextView.getTag()).select();
                                                    viewHolder.acceptButton.setVisibility(View.GONE);
                                                    viewHolder.nameTextView.setTextColor(getResources().getColor(R.color.seshgreen));

                                                    viewHolder.rateTextView.setVisibility(View.GONE);
                                                    viewHolder.checkImageView.setVisibility(View.VISIBLE);
                                                    viewHolder.checkImageView.setScaleX(0.1f);
                                                    viewHolder.checkImageView.setScaleY(0.1f);

                                                    viewHolder.animationSpring = viewHolder.springSystem.createSpring();
                                                    viewHolder.animationSpring.setCurrentValue(.1f);
                                                    viewHolder.animationSpring.setEndValue(1.0f);
                                                    viewHolder.animationSpring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));
                                                    viewHolder.animationSpring.addListener(new SimpleSpringListener() {
                                                        @Override
                                                        public void onSpringUpdate(Spring spring) {
                                                            viewHolder.checkImageView.setScaleX((float) (spring.getCurrentValue()));
                                                            viewHolder.checkImageView.setScaleY((float) (spring.getCurrentValue()));
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onAnimationCancel(Animator animation) {

                                                }

                                                @Override
                                                public void onAnimationRepeat(Animator animation) {

                                                }
                                            })
                                            .start();

                                    seshNetworking.createBid(((JobHolder)viewHolder.nameTextView.getTag()).job.requestId, 2, 2,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject responseJson) {
                                                    onJobResponse(responseJson);
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError volleyError) {
                                                    onJobFailure(volleyError.getMessage());
                                                }
                                            });
                                }
                            }
                        }).execute();
                    }
                }
            });

            if (holder.selected) {
                viewHolder.nameTextView.setTextColor(getResources().getColor(R.color.seshgreen));
                viewHolder.checkImageView.setVisibility(View.VISIBLE);
                viewHolder.rateTextView.setVisibility(View.GONE);
                viewHolder.acceptButton.setVisibility(View.GONE);
                viewHolder.checkImageView.setImageResource(R.drawable.check_green);
            }else {
                viewHolder.nameTextView.setTextColor(getResources().getColor(R.color.seshorange));
                viewHolder.checkImageView.setVisibility(View.GONE);
                viewHolder.rateTextView.setVisibility(View.VISIBLE);
                NumberFormat money = NumberFormat.getCurrencyInstance(Locale.US);
                viewHolder.rateTextView.setText(money.format(item.rate * item.maxTime));
                viewHolder.acceptButton.setVisibility(View.VISIBLE);
                viewHolder.setConfirmMode(false);
            }

            viewHolder.nameTextView.setTag(holder);

            viewHolder.nameTextView.setText(item.studentName);
            viewHolder.nameTextView.setVisibility(View.VISIBLE);

            viewHolder.courseInformationLabel.setText(item.course.shortFormatForTextView());
            viewHolder.courseInformationLabel.setVisibility(View.VISIBLE);

            viewHolder.assignmentInformationLabel.setText(item.description);
            viewHolder.assignmentInformationLabel.setVisibility(View.VISIBLE);

            float[] results = new float[3];
            Location currentLocation = locationManager.getCurrentLocation();
            Location.distanceBetween(
                    currentLocation.getLatitude(),
                    item.latitude,
                    currentLocation.getLongitude(),
                    item.longitude,
                    results);
            /* Convert from emters to miles */
            Double miles = results[0] * 0.000621371;
            viewHolder.distanceInformationLabel.setText(fmtDistance(miles)+ " miles");

            viewHolder.durationInformationLabel.setText(item.maxTime + " hours");
            viewHolder.durationInformationLabel.setVisibility(View.VISIBLE);
            List<AvailableBlock> availableBlockList = new ArrayList<AvailableBlock>();
            availableBlockList.addAll(item.availableBlocks);
            if (item.isInstant) {
                viewHolder.availableBlocksInformationLabel.setText("NOW");
            } else {
                viewHolder.availableBlocksInformationLabel.setText(Html.fromHtml(AvailableBlock.getReadableBlocks(availableBlockList)));
            }
            return convertView;
        }
    }

    private void onJobResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {

            } else if (responseJson.get("status").equals("FAILURE")) {
                Log.e(TAG, responseJson.getString("message"));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void onJobFailure(String errorMessage) {

    }

    private void showOnboardingDialog(final ArrayList<OnboardingRequirement> onboardingRequirements) {
        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setDialogType(SeshDialog.SeshDialogType.TWO_BUTTON);
        seshDialog.setTitle("Onboarding");
        seshDialog.setMessage("Hey, we need to know a few things about you first!");
        seshDialog.setFirstChoice("OKAY");
        seshDialog.setSecondChoice("CANCEL");
        seshDialog.setType("onboarding");
        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seshDialog.dismiss(1);
                Intent intent = new Intent(getActivity(), OnboardingActivity.class);
                intent.putExtra(OnboardingActivity.ONBOARDING_REQS_KEY, onboardingRequirements);
                intent.putExtra(OnboardingActivity.IS_STUDENT_ONBOARDING_KEY, false);
                startActivityForResult(intent, OnboardingActivity.ONBOARDING_REQUEST_CODE);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.hold);
            }
        });
        seshDialog.setSecondButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seshDialog.dismiss(2);
            }
        });
        seshDialog.show(getFragmentManager(), null);
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == OnboardingActivity.ONBOARDING_REQUEST_CODE) {
            if (responseCode == OnboardingActivity.ONBOARDING_SUCCESSFUL_RESPONSE_CODE) {
                SeshDialog seshDialog = new SeshDialog();
                seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
                seshDialog.setTitle("Onboarding Complete");
                seshDialog.setMessage("You're good to go!  Swipe again to accept the Sesh request!");
                seshDialog.setFirstChoice("OKAY");
                seshDialog.setType("onboarding_complete");
                seshDialog.showWithDelay(getFragmentManager(), null, 1000);
            }
        }
    }

    private void getAvailableJobs() {
        Log.d(TAG, "REFRESHING JOBS");
        seshNetworking.getAvailableJobs(tutorCourses, new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject jsonResponse) {
                try {
                    if (jsonResponse.get("status").equals("SUCCESS")) {
                        availableJobsAdapter.clear();
                        JSONArray availableJobsArrayJson = jsonResponse.getJSONArray("courses");
                        for (int i = 0; i < availableJobsArrayJson.length(); i++) {
                            JobHolder jobHolder = new JobHolder(AvailableJob.fromJson((availableJobsArrayJson.getJSONObject(i))), 1);
                            availableJobs.add(jobHolder);
                        }
                        availableJobsAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, jsonResponse.getString("message"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.getMessage());
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void startRepeatingTask() {
        mStatusChecker.run();
    }

    public void stopRepeatingTask() {
        handler.removeCallbacks(mStatusChecker);
    }

    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }

        /**
         * As mentioned above, we need to override this method to properly signal when a
         * 'swipe-to-refresh' is possible.
         *
         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
         */
        @Override
        public boolean canChildScrollUp() {
            final ListView listView = getListView();
            if (listView.getVisibility() == View.VISIBLE) {
                return canListViewScrollUp(listView);
            } else {
                return false;
            }
        }

    }

    /**
     * Utility method to check whether a {@link ListView} can scroll up from it's current position.
     * Handles platform version differences, providing backwards compatible functionality where
     * needed.
     */
    private static boolean canListViewScrollUp(ListView listView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // For ICS and above we can call canScrollVertically() to determine this
            return ViewCompat.canScrollVertically(listView, -1);
        } else {
            // Pre-ICS we need to manually check the first visible item and the child view's top
            // value
            return listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
        }
    }

    private abstract class VerifyTutorOnboardingAsyncTask
            extends AsyncTask<Void, Void, ArrayList<OnboardingRequirement>> {
        @Override
        protected ArrayList<OnboardingRequirement> doInBackground(Void... params) {
            User currentUser = User.currentUser(getActivity());
            ArrayList<OnboardingRequirement> onboardingRequirements = new ArrayList<>();

            if (currentUser.profilePictureUrl == null || currentUser.profilePictureUrl.equals("")) {
                onboardingRequirements.add(OnboardingRequirement.PROFILE_PICTURE);
            }

            if (currentUser.major == null || currentUser.major.equals("")) {
                onboardingRequirements.add(OnboardingRequirement.MAJOR);
            }

            if (currentUser.bio == null || currentUser.bio.equals("")) {
                onboardingRequirements.add(OnboardingRequirement.BIO);
            }

            return onboardingRequirements;
        }
    }
}
