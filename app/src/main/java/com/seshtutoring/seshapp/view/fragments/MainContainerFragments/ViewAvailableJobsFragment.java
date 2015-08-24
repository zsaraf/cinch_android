package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.ArraySwipeAdapter;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.AvailableJob;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.SeshIconTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

/**
 * Created by lillioetting on 7/14/15.
 */
public class ViewAvailableJobsFragment extends ListFragment {
    private static final String TAG =ViewAvailableJobsFragment.class.getName();

    private static final int REFRESH_INTERVAL_MILI = 15000;

    private MainContainerActivity mainContainerActivity;
    private ListView menu;
    private Typeface boldTypeFace;
    private ArrayList<JobHolder> availableJobs;
    private ArrayList<Course> tutorCourses;
    private ViewAvailableJobsAdapter availableJobsAdapter;
    private SeshNetworking seshNetworking;
    private Queue<ViewHolder> bidQueue;
    private Handler handler;

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

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) layoutInflater.inflate(R.layout.view_available_jobs_fragment, null);
        mainContainerActivity = (MainContainerActivity) getActivity();
        boldTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Book.otf");
        this.seshNetworking = new SeshNetworking(getActivity());
        //this.bidQueue = new LinkedList<ViewHolder>();
        this.handler = new Handler();

        this.availableJobs = new ArrayList<JobHolder>();
        this.tutorCourses = new ArrayList<Course>();
        this.availableJobsAdapter = new ViewAvailableJobsAdapter(getActivity(), availableJobs);
        menu.setAdapter(availableJobsAdapter);

        //get courses from server
        seshNetworking.getTutorCourses(new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject jsonResponse) {
                try {
                    if (jsonResponse.get("status").equals("SUCCESS")) {
                        tutorCourses.clear();
                        JSONArray tutorCoursesArrayJson = jsonResponse.getJSONArray("classes");
                        for (int i = 0; i < tutorCoursesArrayJson.length(); i++) {
                            JSONObject obj = tutorCoursesArrayJson.getJSONObject(i);
                            Course c = Course.fromJson(obj);
                            tutorCourses.add(Course.fromJson((tutorCoursesArrayJson.getJSONObject(i))));
                        }

                        //get available jobs from server
                        getAvailableJobs();

                    } else {
                        Log.e(TAG, jsonResponse.getString("message"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.getMessage());
            }
        });

        return menu;
    }

    private class ViewHolder {

        public TextView nameTextView;
        public TextView rateTextView;
        public TextView overlayTextView;
        //public TextView loadingTextView;
        public ImageView checkImageView;
        public SeshIconTextView courseTextView;
        public SeshIconTextView assignmentTextView;
        //public SeshIconTextView distanceTextView;
        public SeshIconTextView durationTextView;
        public SeshIconTextView availableBlocksTextView;

        public ViewGroup topGroup;

        //public MultiStateAnimation animation;

    }

    public class ViewAvailableJobsAdapter extends ArraySwipeAdapter<JobHolder> {

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

                viewHolder.topGroup = (ViewGroup) convertView.findViewById(R.id.top_wrapper);

                viewHolder.overlayTextView = (TextView) convertView.findViewById(R.id.overlay_text);

                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.student_name);
                viewHolder.nameTextView.setTypeface(boldTypeFace);

                viewHolder.rateTextView = (TextView) convertView.findViewById(R.id.hourly_rate);

                viewHolder.courseTextView = (SeshIconTextView) convertView.findViewById(R.id.course);
                viewHolder.courseTextView.setIconResourceId(R.drawable.book);

                viewHolder.assignmentTextView = (SeshIconTextView) convertView.findViewById(R.id.assignment);
                viewHolder.assignmentTextView.setIconResourceId(R.drawable.assignment_gray);

                //viewHolder.distanceTextView = (SeshIconTextView) convertView.findViewById(R.id.distance);
                viewHolder.durationTextView = (SeshIconTextView) convertView.findViewById(R.id.duration);
                viewHolder.durationTextView.setIconResourceId(R.drawable.clock_orange);

                viewHolder.availableBlocksTextView = (SeshIconTextView) convertView.findViewById(R.id.available_blocks);
                viewHolder.availableBlocksTextView.setIconResourceId(R.drawable.calendar_unfilled);

                //viewHolder.loadingTextView = (TextView) convertView.findViewById(R.id.loading_text);

                viewHolder.checkImageView = (ImageView) convertView.findViewById(R.id.check_mark);
                //viewHolder.animation = MultiStateAnimation.fromJsonResource(getActivity(), viewHolder.animationView, R.raw.sample_animation);

                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

//            if (viewHolder.status == 1) {
//                //viewHolder.nameTextView.setTextColor(getResources().getColor(R.color.seshgreen));
//                viewHolder.topGroup.setBackgroundColor(getResources().getColor(R.color.seshgreen));
//                viewHolder.nameTextView.setVisibility(View.INVISIBLE);
//                viewHolder.assignmentTextView.setVisibility(View.INVISIBLE);
//                viewHolder.durationTextView.setVisibility(View.INVISIBLE);
//                viewHolder.rateTextView.setVisibility(View.INVISIBLE);
//                viewHolder.courseTextView.setVisibility(View.INVISIBLE);
//                viewHolder.availableBlocksTextView.setVisibility(View.INVISIBLE);
//                viewHolder.animation.transitionNow("loading");
//                viewHolder.loadingTextView.setVisibility(View.VISIBLE);
//                viewHolder.loadingTextView.setText("requesting job...");
//            }else if (viewHolder.status == 2) {
//                viewHolder.animation.transitionNow("finished");
//                viewHolder.loadingTextView.setText("submitted! you'll receive a notification if you get the job.");

            if (holder.type == 2) {
                viewHolder.overlayTextView.setText("no available jobs");
                viewHolder.nameTextView.setVisibility(View.GONE);
                viewHolder.assignmentTextView.setVisibility(View.GONE);
                viewHolder.durationTextView.setVisibility(View.GONE);
                viewHolder.rateTextView.setVisibility(View.GONE);
                viewHolder.courseTextView.setVisibility(View.GONE);
                viewHolder.availableBlocksTextView.setVisibility(View.GONE);
            }else {

                SwipeLayout swipeView = (SwipeLayout) convertView.findViewById(R.id.swipe_view);
                swipeView.setShowMode(SwipeLayout.ShowMode.LayDown);

                swipeView.addSwipeListener(new SwipeLayout.SwipeListener() {

                    @Override
                    public void onClose(SwipeLayout layout) {
                        //when the SurfaceView totally cover the BottomView.
                    }

                    @Override
                    public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                        //you are swiping.
                    }

                    @Override
                    public void onStartOpen(SwipeLayout layout) {

                    }

                    @Override
                    public void onOpen(SwipeLayout layout) {

                        layout.close();
                        layout.setSwipeEnabled(false);
                        ViewHolder viewHolder = (ViewHolder) layout.getTag();
                        ((JobHolder)viewHolder.nameTextView.getTag()).select();
                        viewHolder.nameTextView.setTextColor(getResources().getColor(R.color.seshgreen));
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
                        //replace 2s with lat/long

                    }

                    @Override
                    public void onStartClose(SwipeLayout layout) {

                    }

                    @Override
                    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

                    }
                });

                if (holder.selected) {
                    viewHolder.nameTextView.setTextColor(getResources().getColor(R.color.seshgreen));
                    viewHolder.checkImageView.setVisibility(View.VISIBLE);
                    viewHolder.rateTextView.setVisibility(View.GONE);
                    viewHolder.checkImageView.setImageResource(R.drawable.check_green);
                }else {
                    viewHolder.nameTextView.setTextColor(getResources().getColor(R.color.seshorange));
                    viewHolder.checkImageView.setVisibility(View.GONE);
                    viewHolder.rateTextView.setVisibility(View.VISIBLE);
                    NumberFormat money = NumberFormat.getCurrencyInstance(Locale.US);
                    viewHolder.rateTextView.setText(money.format(item.rate * item.maxTime));
                }

                viewHolder.nameTextView.setTag(holder);

                viewHolder.overlayTextView.setVisibility(View.GONE);

                viewHolder.nameTextView.setText(item.studentName);
                viewHolder.nameTextView.setVisibility(View.VISIBLE);

                viewHolder.courseTextView.setText(item.course.shortFormatForTextView());
                viewHolder.courseTextView.setVisibility(View.VISIBLE);

                viewHolder.assignmentTextView.setText(item.description);
                viewHolder.assignmentTextView.setVisibility(View.VISIBLE);

                //calculate distance?
                //viewHolder.distanceTextView.setText(item + " miles");
                //viewHolder.distanceTextView.setIconResourceId(R.drawable.current_location);

                viewHolder.durationTextView.setText(item.maxTime + " hours");
                viewHolder.durationTextView.setVisibility(View.VISIBLE);

                viewHolder.availableBlocksTextView.setText(Html.fromHtml(item.getReadableBlocks()));
                viewHolder.availableBlocksTextView.setVisibility(View.VISIBLE);

                //viewHolder.loadingTextView.setVisibility(View.GONE);
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
                        if (availableJobs.size() == 0) {
                            //No available jobs
                            availableJobs.add(new JobHolder(null, 2));
                        }
                        availableJobsAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, jsonResponse.getString("message"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.getMessage());
            }
        });
    }

    public void startRepeatingTask() {
        mStatusChecker.run();
    }

    public void stopRepeatingTask() {
        handler.removeCallbacks(mStatusChecker);
    }

}
