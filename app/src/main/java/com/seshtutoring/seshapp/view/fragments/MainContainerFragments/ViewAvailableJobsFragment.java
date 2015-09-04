package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.AvailableJob;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.SeshIconTextView;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;

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
        RelativeLayout v = (RelativeLayout) layoutInflater.inflate(R.layout.view_available_jobs_fragment, null);

        // Now create a SwipeRefreshLayout to wrap the fragment's content view
        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());

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


        //get available jobs from server
        getAvailableJobs();

        //get courses from server
//        seshNetworking.getTutorCourses(new Response.Listener<JSONObject>() {
//            public void onResponse(JSONObject jsonResponse) {
//                try {
//                    if (jsonResponse.get("status").equals("SUCCESS")) {
//                        tutorCourses.clear();
//                        JSONArray tutorCoursesArrayJson = jsonResponse.getJSONArray("classes");
//                        for (int i = 0; i < tutorCoursesArrayJson.length(); i++) {
//                            JSONObject obj = tutorCoursesArrayJson.getJSONObject(i);
//                            Course c = Course.fromJson(obj);
//                            tutorCourses.add(Course.fromJson((tutorCoursesArrayJson.getJSONObject(i))));
//                        }
//
//
//
//                    } else {
//                        Log.e(TAG, jsonResponse.getString("message"));
//                    }
//                } catch (JSONException e) {
//                    Log.e(TAG, e.getMessage());
//                }
//            }
//        }, new Response.ErrorListener() {
//            public void onErrorResponse(VolleyError volleyError) {
//                Log.e(TAG, volleyError.getMessage());
//            }
//        });

    }

    private class ViewHolder {
        public RelativeLayout bottomWrapper;
        public TextView nameTextView;
        public TextView rateTextView;
        public TextView overlayTextView;
        //public TextView loadingTextView;
        public ImageView checkImageView;
        public ImageView backingCheckImageView;
        public SeshInformationLabel courseInformationLabel;
        public SeshInformationLabel assignmentInformationLabel;
        public SeshInformationLabel distanceInformationLabel;
        public SeshInformationLabel durationInformationLabel;
        public SeshInformationLabel availableBlocksInformationLabel;

        public ViewGroup topGroup;

        public Boolean shouldBid;

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

                viewHolder.shouldBid = false;
                viewHolder.backingCheckImageView = (ImageView) convertView.findViewById(R.id.swipe_view_check_icon);
                viewHolder.backingCheckImageView.setColorFilter(Color.argb(255, 255, 255, 255));

                viewHolder.bottomWrapper = (RelativeLayout) convertView.findViewById(R.id.bottom_wrapper);

                viewHolder.topGroup = (ViewGroup) convertView.findViewById(R.id.top_wrapper);

                viewHolder.overlayTextView = (TextView) convertView.findViewById(R.id.overlay_text);

                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.student_name);
                viewHolder.nameTextView.setTypeface(boldTypeFace);

                viewHolder.rateTextView = (TextView) convertView.findViewById(R.id.hourly_rate);

                viewHolder.courseInformationLabel = (SeshInformationLabel) convertView.findViewById(R.id.course);

                viewHolder.assignmentInformationLabel = (SeshInformationLabel) convertView.findViewById(R.id.assignment);

                viewHolder.distanceInformationLabel = (SeshInformationLabel) convertView.findViewById(R.id.distance);


                viewHolder.durationInformationLabel = (SeshInformationLabel) convertView.findViewById(R.id.duration);

                viewHolder.availableBlocksInformationLabel = (SeshInformationLabel) convertView.findViewById(R.id.available_blocks);

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
//                viewHolder.animation.transitionNow("loading");
//                viewHolder.loadingTextView.setVisibility(View.VISIBLE);
//                viewHolder.loadingTextView.setText("requesting job...");
//            }else if (viewHolder.status == 2) {
//                viewHolder.animation.transitionNow("finished");
//                viewHolder.loadingTextView.setText("submitted! you'll receive a notification if you get the job.");

            if (holder.type == 2) {
                viewHolder.overlayTextView.setText("no available jobs");
                viewHolder.nameTextView.setVisibility(View.GONE);
                viewHolder.assignmentInformationLabel.setVisibility(View.GONE);
                viewHolder.durationInformationLabel.setVisibility(View.GONE);
                viewHolder.rateTextView.setVisibility(View.GONE);
                viewHolder.courseInformationLabel.setVisibility(View.GONE);
                viewHolder.availableBlocksInformationLabel.setVisibility(View.GONE);
                viewHolder.distanceInformationLabel.setVisibility(View.GONE);
            }else {

                SwipeLayout swipeView = (SwipeLayout) convertView.findViewById(R.id.swipe_view);
                swipeView.setShowMode(SwipeLayout.ShowMode.LayDown);
                swipeView.setDragEdge(SwipeLayout.DragEdge.Left);

                swipeView.addSwipeListener(new SwipeLayout.SwipeListener() {

                    @Override
                    public void onClose(SwipeLayout layout) {
                        //when the SurfaceView totally cover the BottomView.
                    }

                    @Override
                    public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                        //you are swiping.
                        if (leftOffset > viewHolder.backingCheckImageView.getMeasuredWidth() + viewHolder.backingCheckImageView.getPaddingLeft()) {
                            viewHolder.bottomWrapper.setBackgroundColor(mContext.getResources().getColor(R.color.seshgreen));
                            viewHolder.backingCheckImageView.setX(leftOffset - viewHolder.backingCheckImageView.getMeasuredWidth() - viewHolder.backingCheckImageView.getPaddingLeft());
                            viewHolder.shouldBid = true;
                        } else {
                            viewHolder.bottomWrapper.setBackgroundColor(mContext.getResources().getColor(R.color.terms_text_light_gray));
                            viewHolder.backingCheckImageView.setX(0);
                            viewHolder.shouldBid = false;
                        }
                    }

                    @Override
                    public void onStartOpen(SwipeLayout layout) {

                    }

                    @Override
                    public void onOpen(SwipeLayout layout) {

                    }

                    @Override
                    public void onStartClose(SwipeLayout layout) {
                    }

                    @Override
                    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                        ViewHolder viewHolder = (ViewHolder) layout.getTag();
                        if (viewHolder.shouldBid) {
                            viewHolder.shouldBid = false;
                            layout.setSwipeEnabled(false);
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
                        }
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

                viewHolder.courseInformationLabel.setText(item.course.shortFormatForTextView());
                viewHolder.courseInformationLabel.setVisibility(View.VISIBLE);

                viewHolder.assignmentInformationLabel.setText(item.description);
                viewHolder.assignmentInformationLabel.setVisibility(View.VISIBLE);

                viewHolder.distanceInformationLabel.setText(".1 miles");
                //calculate distance?
                //viewHolder.distanceTextView.setText(item + " miles");
                //viewHolder.distanceTextView.setIconResourceId(R.drawable.current_location);

                viewHolder.durationInformationLabel.setText(item.maxTime + " hours");
                viewHolder.durationInformationLabel.setVisibility(View.VISIBLE);
                List<AvailableBlock> availableBlockList = new ArrayList<AvailableBlock>();
                availableBlockList.addAll(item.availableBlocks);
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

}
