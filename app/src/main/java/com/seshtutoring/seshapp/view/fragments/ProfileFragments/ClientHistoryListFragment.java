package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.app.ListFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.PastRequest;
import com.seshtutoring.seshapp.model.PastSesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.SeshUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.squareup.picasso.Callback;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

/**
 * Created by lillioetting on 9/1/15.
 */
public class ClientHistoryListFragment extends ListFragment{

    private ListView list;
    private List<PastSesh> pastSeshes;
    private List<PastRequest> pastRequests;
    private TutorHistoryAdapter tutorHistoryAdapter;
    private SeshNetworking seshNetworking;
    private MainContainerActivity mainContainerActivity;
    private User user;

    private Typeface boldTypeFace;
    private TextView emptyTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout view = (RelativeLayout) layoutInflater.inflate(R.layout.profile_list_view, null);

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mainContainerActivity = (MainContainerActivity) getActivity();
        boldTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Book.otf");

        emptyTextView = (TextView) this.getView().findViewById(R.id.list_empty_text);
        emptyTextView.setTypeface(boldTypeFace);

        this.seshNetworking = new SeshNetworking(getActivity());

        this.user = User.currentUser(mainContainerActivity.getApplicationContext());
        this.seshNetworking = new SeshNetworking(mainContainerActivity);

        String emptyStr = "you haven't tutored any seshes yet - try picking up a request today!";
        if (user.tutor == null || !user.tutor.enabled) {
            emptyStr = "you're not registered as a tutor yet, sign up at seshtutoring.com!";
        }
        emptyTextView.setText(emptyStr);

        this.pastSeshes = PastSesh.listAll(PastSesh.class);
        filterPastSeshesForTutor();

        this.tutorHistoryAdapter = new TutorHistoryAdapter(getActivity(), pastSeshes);
        getListView().setAdapter(tutorHistoryAdapter);
    }

    private void filterPastSeshesForTutor() {
        for (int i = 0; i < pastSeshes.size(); i++) {
            if (pastSeshes.get(i).studentUserId == user.userId) {
                //user is the student in this sesh, do not include on this page
                pastSeshes.remove(i);
                i--;
            }
        }
        Collections.reverse(pastSeshes);
    }

    private class ViewHolder {

        public TextView mainTextView;
        public TextView subTextView;
        public TextView rightTextView;
        public de.hdodenhof.circleimageview.CircleImageView profileImageView;

    }

    public class TutorHistoryAdapter extends ArrayAdapter<PastSesh> {

        private Context mContext;
        private LayoutInflater layoutInflater;

        public TutorHistoryAdapter(Context context, List<PastSesh> tutorCourses) {
            super(context, R.layout.profile_list_view_row, tutorCourses);
            this.mContext = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            PastSesh item = (PastSesh) getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.profile_list_view_row,
                        null);

                viewHolder = new ViewHolder();

                viewHolder.mainTextView = (TextView) convertView.findViewById(R.id.profile_list_main_text);
                viewHolder.subTextView = (TextView) convertView.findViewById(R.id.profile_list_sub_text);
                viewHolder.rightTextView = (TextView) convertView.findViewById(R.id.profile_list_right_text);
                viewHolder.profileImageView = (de.hdodenhof.circleimageview.CircleImageView) convertView.findViewById(R.id.profile_list_profile_image);

                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            LayoutUtils layUtils = new LayoutUtils(getActivity().getApplicationContext());
            viewHolder.mainTextView.setText(SeshUtils.abbreviatedNameForName(item.studentFullName));
            viewHolder.subTextView.setText(item.className);

            String rightText = "cancelled";
            if (!item.wasCancelled) {
                DecimalFormat df = new DecimalFormat("0.00");
                rightText = "$" + df.format(item.tutorEarnings);
                viewHolder.rightTextView.setTextColor(getResources().getColor(R.color.seshgreen));
            }else {
                viewHolder.rightTextView.setTextColor(getResources().getColor(R.color.seshlightgray));
            }
            viewHolder.rightTextView.setText(rightText);

            seshNetworking.downloadProfilePictureAsync(item.studentProfilePicture, viewHolder.profileImageView, new Callback() {
                @Override
                public void onSuccess() {
                    // do nothing
                }

                @Override
                public void onError() {
                    // do nothing
                }
            });

            return convertView;
        }

    }

}
