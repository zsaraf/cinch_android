package com.seshtutoring.seshapp.view.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.google.android.gms.maps.model.Circle;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.LearnRequest.LearnRequestTableListener;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.model.Sesh.SeshTableListener;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.ContainerState;
import com.seshtutoring.seshapp.view.animations.LearnRequestDisplayAnimation;
import com.seshtutoring.seshapp.view.animations.SeshDisplayAnimation;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.DummyRequestSeshFragment;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SideMenuFragment extends Fragment implements SlidingMenu.OnOpenListener,
        SlidingMenu.OnOpenedListener, SeshTableListener, LearnRequestTableListener {
    private static final String TAG = SideMenuFragment.class.getName();

    private MainContainerActivity mainContainerActivity;
    private ListView navigationMenu;
    private ListView openRequestsAndSeshesMenu;
    private RequestsAndSeshesAdapter openRequestsAndSeshesAdapter;
    private String menuOpenFlag;
    private SideMenuAdapter sideMenuAdapter;
    private SideMenuOpenAnimation sideMenuOpenAnimation;
    private SelectableItem currentSelectedItem;
    private Spring selectedStateSpring;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.side_menu_fragment, container, false);
        navigationMenu = (ListView) view.findViewById(R.id.side_menu_list);
        openRequestsAndSeshesMenu = (ListView) view.findViewById(R.id.open_requests_and_seshes_list);
        mainContainerActivity = (MainContainerActivity) getActivity();
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sideMenuAdapter = new SideMenuAdapter(getActivity());

        for (int i = 0; i < mainContainerActivity.containerStates.length; i++) {
            ContainerState state = mainContainerActivity.containerStates[i];
            sideMenuAdapter.add(new SideMenuItem(state.title, state.iconRes, i));
        }

        navigationMenu.setAdapter(sideMenuAdapter);

        openRequestsAndSeshesAdapter = new RequestsAndSeshesAdapter(getActivity());
        openRequestsAndSeshesAdapter.setNotifyOnChange(false);
        openRequestsAndSeshesMenu.setAdapter(openRequestsAndSeshesAdapter);

        selectedStateSpring = SpringSystem.create().createSpring();
        selectedStateSpring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9, 6));

        Sesh.setTableListener(this);
        LearnRequest.setTableListener(this);

        updateSelectedItem(sideMenuAdapter.getItem(0));

        (new UpdateRequestAndSeshListTask()).execute();

    }

    public void updateSelectedItem(SelectableItem selectedItem) {
        if (currentSelectedItem != null) {
            currentSelectedItem.setSelected(false);
        }

        currentSelectedItem = selectedItem;
        currentSelectedItem.setSelected(true);
    }

    private abstract class SelectableItem {
        private boolean selected = false;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public abstract View getView();
        public abstract TextView getLabel();
    }

    private class SideMenuItem extends SelectableItem {
        public String tag;
        public int icon;
        public int position;
        public SideMenuItem(String tag, int icon, int position) {
            this.tag = tag;
            this.icon = icon;
            this.position = position;
        }

        public View getView() {
            return navigationMenu.getChildAt(position);
        }

        public TextView getLabel() {
            return (TextView) getView().findViewById(R.id.row_title);
        }
    }

    public void onSideMenuItemSelected(int position) {
        ContainerState selectedMenuOption = mainContainerActivity.HOME;
        switch (position) {
            case 0:
                selectedMenuOption = mainContainerActivity.HOME;
                break;
            case 1:
                selectedMenuOption = mainContainerActivity.PROFILE;
                break;
            case 2:
                selectedMenuOption = mainContainerActivity.PAYMENT;
                break;
            case 3:
                selectedMenuOption = mainContainerActivity.SETTINGS;
                break;
            case 4:
                selectedMenuOption = mainContainerActivity.PROMOTE;
                break;
            default:
                selectedMenuOption = mainContainerActivity.HOME;
                break;
        }

        updateSelectedItem(sideMenuAdapter.getItem(position));
        mainContainerActivity.setCurrentState(selectedMenuOption, null);
        mainContainerActivity.closeDrawerWithDelay(true, 1000);
    }

    public void onOpenRequestAndSeshesItemSelect(int position) {
        RequestsAndSeshesListItem item = openRequestsAndSeshesAdapter.getItem(position);
        if (item.isDivider) return;
        if (item.isSesh) {
            mainContainerActivity.setCurrentState(new ContainerState("Sesh!", 0,
                    ViewSeshFragment.newInstance(item.sesh.seshId, false)));
        } else {
            mainContainerActivity.setCurrentState(new ContainerState("Request!", 0,
                    ViewRequestFragment.newInstance(item.learnRequest.learnRequestId)));
        }

        updateSelectedItem(item);
        mainContainerActivity.closeDrawerWithDelay(true, 1000);
    }

    public class SideMenuAdapter extends ArrayAdapter<SideMenuItem> {

        public SideMenuAdapter(Context context) {
            super(context, 0);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.side_menu_list_row,
                        null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
            icon.setImageResource(getItem(position).icon);
            TextView title = (TextView) convertView.findViewById(R.id.row_title);
            title.setText(getItem(position).tag);

            if (getItem(position).isSelected()) {
                selectLabel(title);
            } else {
                deselectLabel(title);
            }

            convertView.setOnTouchListener(selectedStateTouchListener(getItem(position), new Runnable() {
                @Override
                public void run() {
                    onSideMenuItemSelected(position);
                }
            }));

            return convertView;
        }
    }

    private View.OnTouchListener selectedStateTouchListener(final SelectableItem item, Runnable touchEventUpBlock) {
        final Runnable completionBlock = touchEventUpBlock;

        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    selectedStateTouchDown(item);
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    selectedStateTouchUp(item, completionBlock);
                }
                return true;
            }
        };
    }

    private void selectedStateTouchDown(SelectableItem item) {
        final View view = item.getView();
        view.setPivotX(getResources().getDimensionPixelSize(R.dimen.side_menu_scale_pivot_x));

        selectedStateSpring.removeAllListeners();
        selectedStateSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                view.setScaleX((float) spring.getCurrentValue());
                view.setScaleY((float) spring.getCurrentValue());
            }
        });

        selectedStateSpring.setCurrentValue(1);
        selectedStateSpring.setEndValue(0.9);
    }

    private void selectedStateTouchUp(SelectableItem selectableItem, final Runnable completionBlock) {
        final View view = selectableItem.getView();

        selectedStateSpring.removeAllListeners();
        selectedStateSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                view.setScaleX((float) spring.getCurrentValue());
                view.setScaleY((float) spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                completionBlock.run();
            }
        });

        selectedStateSpring.setCurrentValue(view.getScaleX());
        selectedStateSpring.setEndValue(1);

        if (currentSelectedItem != null) {
            deselectLabel(currentSelectedItem.getLabel());
        }

        selectLabel(selectableItem.getLabel());
    }

    private class RequestsAndSeshesListItem extends SelectableItem {
        public boolean isSesh;
        public boolean isDivider;
        public LearnRequest learnRequest;
        public Sesh sesh;
        public String dividerText;
        public int position;

        public RequestsAndSeshesListItem(boolean isSesh, boolean isDivider, LearnRequest learnRequest, Sesh sesh,
                             String dividerText, int position) {
            this.isSesh = isSesh;
            this.isDivider = isDivider;
            this.learnRequest = learnRequest;
            this.sesh = sesh;
            this.dividerText = dividerText;
            this.position = position;
        }

        public View getView() {
            return openRequestsAndSeshesMenu.getChildAt(position);
        }

        public TextView getLabel() {
            View view = getView();
            if (isSesh) {
                return (TextView) view.findViewById(R.id.open_sesh_list_row_class);
            } else if (isDivider) {
                return null;
            } else {
                return (TextView) view.findViewById(R.id.open_request_list_row_class);
            }
        }
    }

    public class RequestsAndSeshesAdapter extends ArrayAdapter<RequestsAndSeshesListItem> {
        public RequestsAndSeshesAdapter(Context context) { super(context, 0); }

        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "refreshing position " + position);
            RequestsAndSeshesListItem item = getItem(position);
            LayoutUtils utils = new LayoutUtils(getContext());
            if (item.isDivider) {
                // if convertView is not instantiated, or is of the wrong type, we re-instantiate
                if (convertView == null || isOpenRequestRow(convertView) || isOpenSeshRow(convertView)) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_request_divider,
                            null);
                }

                TextView dividerText = (TextView) convertView.findViewById(R.id.divider_text);
                dividerText.setText(item.dividerText);

                convertView.setEnabled(false);
                convertView.setOnClickListener(null);
            } else if (item.isSesh) {
                // if convertView is not instantiated, or is of the wrong type, we re-instantiate
                if (convertView == null || isOpenRequestRow(convertView) || isDividerRow(convertView)) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_sesh_list_row,
                            null);
                }

                TextView classAbbrvTextView = (TextView) convertView.findViewById(R.id.open_sesh_list_row_class);
                classAbbrvTextView.setText(item.sesh.className);

                if (item.isSelected()) {
                    classAbbrvTextView.setTypeface(utils.getMediumGothamTypeface());
                } else {
                    classAbbrvTextView.setTypeface(utils.getBookGothamTypeface());
                }

                TextView timeAbbrvTextView = (TextView) convertView.findViewById(R.id.open_sesh_list_row_time);
                timeAbbrvTextView.setText(item.sesh.getTimeAbbrvString());
                timeAbbrvTextView.setTypeface(utils.getLightGothamTypeface());

                CircleImageView profileImage = (CircleImageView) convertView.findViewById(R.id.profile_image);
                item.sesh.loadImageAsync(profileImage, getActivity());

                if (!item.sesh.isStudent) {
                    ImageView icon = (ImageView)convertView.findViewById(R.id.open_sesh_list_row_status_icon);
                    int drawableId;

                    if (item.sesh.seshSetTime == -1) {
                        drawableId = R.drawable.alert;
                    } else {
                        drawableId = R.drawable.check_green;
                    }

                    if (Build.VERSION.SDK_INT < 21) {
                        icon.setImageDrawable(getResources().getDrawable(drawableId));
                    } else  {
                        icon.setImageDrawable(getResources().getDrawable(drawableId, null));
                    }
                }

                RelativeLayout alertBadge = (RelativeLayout) convertView.findViewById(R.id.alert_badge);

                if (item.sesh.numUnreadMessages > 0) {
                    alertBadge.setVisibility(View.VISIBLE);

                    TextView alertBadgeNumber = (TextView) convertView.findViewById(R.id.alert_badge_number);
                    if (item.sesh.numUnreadMessages < 10) {
                        alertBadgeNumber.setText(Integer.toString(item.sesh.numUnreadMessages));
                    } else {
                        alertBadgeNumber.setText("+");
                    }
                    alertBadgeNumber.setTypeface(utils.getBookGothamTypeface());
                } else {
                    if (alertBadge.getVisibility() == View.VISIBLE) {
                        alertBadge.setVisibility(View.GONE);
                    }
                }

                convertView.setOnTouchListener(
                        selectedStateTouchListener(item, new Runnable() {
                            @Override
                            public void run() {
                                onOpenRequestAndSeshesItemSelect(position);
                            }
                        }));

                if (item.sesh.requiresAnimatedDisplay) {
                    sideMenuOpenAnimation = new SeshDisplayAnimation(mainContainerActivity, item.sesh,
                            convertView);
                }
            } else {
                // if convertView is not instantiated, or is of the wrong type, we re-instantiate
                if (convertView == null || isOpenSeshRow(convertView) || isDividerRow(convertView)) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_request_list_row,
                            null);
                }

                TextView classAbbrvTextView = (TextView) convertView.findViewById(R.id.open_request_list_row_class);
                classAbbrvTextView.setText(item.learnRequest.classString);

                if (item.isSelected()) {
                    classAbbrvTextView.setTypeface(utils.getMediumGothamTypeface());
                } else {
                    classAbbrvTextView.setTypeface(utils.getLightGothamTypeface());
                }

                convertView.setOnTouchListener(
                        selectedStateTouchListener(item, new Runnable() {
                            @Override
                            public void run() {
                                onOpenRequestAndSeshesItemSelect(position);
                            }
                        }));

                if (item.learnRequest.requiresAnimatedDisplay) {
                    sideMenuOpenAnimation = new LearnRequestDisplayAnimation(mainContainerActivity,
                            item.learnRequest, convertView);
                }
            }
            return convertView;
        }
    }

    private boolean isDividerRow(View view) {
        return (view.findViewById(R.id.learn_divider) != null);
    }

    private boolean isOpenRequestRow(View view) {
        return (view.findViewById(R.id.open_request_list_row_class) != null);
    }

    private boolean isOpenSeshRow(View view) {
        return (view.findViewById(R.id.open_sesh_list_row_class) != null);
    }

    public void selectLabel(TextView label) {
        LayoutUtils utils = new LayoutUtils(getActivity());
        label.setTypeface(utils.getMediumGothamTypeface());
    }

    public void deselectLabel(TextView label) {
        LayoutUtils utils = new LayoutUtils(getActivity());
        label.setTypeface(utils.getLightGothamTypeface());
    }

    @Override
    public void onOpen() {
        Log.d(TAG, "ON OPEN");
        if (sideMenuOpenAnimation != null) {
            sideMenuOpenAnimation.prepareAnimation();
        }
    }

    @Override
    public void onOpened() {
        Log.d(TAG, "ON OPENED");
        if (sideMenuOpenAnimation != null) {
            sideMenuOpenAnimation.startAnimation();
        }
        sideMenuOpenAnimation = null;
    }


    public class UpdateRequestAndSeshListTask extends AsyncTask<Void, Void, Void> {
        private List<Sesh> studentSeshes;
        private List<Sesh> tutorSeshes;
        private List<LearnRequest> learnRequests;

        protected Void doInBackground(Void... voids){
            if (Sesh.listAll(Sesh.class).size() > 0) {
                studentSeshes = Sesh.find(Sesh.class, "is_student = ?", Integer.toString(1));
                tutorSeshes = Sesh.find(Sesh.class, "is_student = ?", Integer.toString(0));
            } else {
                studentSeshes = new ArrayList<Sesh>();
                tutorSeshes = new ArrayList<Sesh>();
            }

            learnRequests = LearnRequest.listAll(LearnRequest.class);
            return null;

        }

        protected void onPostExecute(Void result) {
            openRequestsAndSeshesAdapter.clear();

            int position = 0;

            if (tutorSeshes.size() > 0) {
                RequestsAndSeshesListItem teachDivider
                        = new RequestsAndSeshesListItem(false, true, null, null, "TEACH", position++);
                openRequestsAndSeshesAdapter.add(teachDivider);
            }

            for (Sesh sesh : tutorSeshes) {
                RequestsAndSeshesListItem seshItem =
                        new RequestsAndSeshesListItem(true, false, null, sesh, null, position++);
                openRequestsAndSeshesAdapter.add(seshItem);
            }

            if (studentSeshes.size() + learnRequests.size() > 0) {
                RequestsAndSeshesListItem learnDivider
                        = new RequestsAndSeshesListItem(false, true, null, null, "LEARN", position++);
                openRequestsAndSeshesAdapter.add(learnDivider);
            }

            for (Sesh sesh : studentSeshes) {
                RequestsAndSeshesListItem seshItem =
                        new RequestsAndSeshesListItem(true, false, null, sesh, null, position++);
                openRequestsAndSeshesAdapter.add(seshItem);
            }

            for (LearnRequest learnRequest : learnRequests) {
                RequestsAndSeshesListItem requestItem =
                        new RequestsAndSeshesListItem(false, false, learnRequest, null, null, position++);
                openRequestsAndSeshesAdapter.add(requestItem);
            }
            Log.d(TAG, "updateLearnList() end " + new Date().toString());

            openRequestsAndSeshesAdapter.notifyDataSetChanged();

            if (mainContainerActivity.getCurrentState().fragment instanceof ViewSeshFragment) {
                ViewSeshFragment viewSeshFragment = (ViewSeshFragment) mainContainerActivity.getCurrentState().fragment;
                viewSeshFragment.refresh();
            }
        }
    }

    public static abstract class SideMenuOpenAnimation {
        public abstract void prepareAnimation();
        public abstract void startAnimation();
        public abstract void onAnimationCompleted();
    }

    @Override
    public void tableUpdated() {
        Log.d(TAG, "TABLE UPDATED HAS BEEN CALLED");
        (new UpdateRequestAndSeshListTask()).execute();
    }
}