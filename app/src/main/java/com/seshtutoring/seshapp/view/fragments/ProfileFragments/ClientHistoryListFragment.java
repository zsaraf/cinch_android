package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.PastRequest;
import com.seshtutoring.seshapp.model.PastSesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.squareup.picasso.Callback;

import java.text.NumberFormat;
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

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.list = (ListView) layoutInflater.inflate(R.layout.profile_list_view, null);

        mainContainerActivity = (MainContainerActivity) getActivity();
        this.user = User.currentUser(mainContainerActivity.getApplicationContext());
        this.seshNetworking = new SeshNetworking(mainContainerActivity);

        this.pastSeshes = PastSesh.listAll(PastSesh.class);
        filterPastSeshesForStudent();

        this.tutorHistoryAdapter = new TutorHistoryAdapter(getActivity(), pastSeshes);
        this.list.setAdapter(tutorHistoryAdapter);

        return this.list;

    }

    private void filterPastSeshesForStudent() {
        for (int i = 0; i < pastSeshes.size(); i++) {
            if (pastSeshes.get(i).studentUserId == user.userId) {
                //user is the student in this sesh, do not include on this page
                pastSeshes.remove(i);
            }
        }
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
            String abbrName = item.studentFullName.substring(0,item.studentFullName.lastIndexOf(" ")+2) + ".";
            viewHolder.mainTextView.setText(abbrName);
            viewHolder.subTextView.setText(item.className);
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            viewHolder.rightTextView.setText(formatter.format(item.cost));

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
