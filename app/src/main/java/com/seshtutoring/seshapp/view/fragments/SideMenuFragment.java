package com.seshtutoring.seshapp.view.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.services.FetchSeshInfoBroadcastReceiver;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.PaymentFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ProfileFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.PromoteFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.SettingsFragment;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SideMenuFragment extends Fragment implements SlidingMenu.OnOpenedListener, FetchSeshInfoBroadcastReceiver.SeshInfoUpdateListener {
    private static final String TAG = SideMenuFragment.class.getName();

    public static enum MenuOption {
        HOME("Home", R.drawable.home, new HomeFragment(), 0),
        PROFILE("Profile", R.drawable.profile, new ProfileFragment(), 1),
        PAYMENT("Payment", R.drawable.payment, new PaymentFragment(), 2),
        SETTINGS("Settings", R.drawable.settings, new SettingsFragment(), 3),
        PROMOTE("Promote", R.drawable.share, new PromoteFragment(), 4);

        public String title;
        public int iconRes;
        public Fragment fragment;
        public int position;

        MenuOption(String title, int iconRes, Fragment fragment, int position) {
            this.title = title;
            this.iconRes = iconRes;
            this.fragment = fragment;
            this.position = position;
        }
    }

    public static final String MAIN_WRAPPER_STATE_KEY = "main_wrapper_state";
    public static final String MENU_OPEN_DISPLAY_NEW_REQUEST = "display_new_request";

    private MainContainerActivity mainContainerActivity;
    private SideMenuItem selectedItem;
    private ListView menu;
    private ListView learnList;
    private LearnListAdapter learnListAdapter;
    private String menuOpenFlag;
    private SideMenuAdapter sideMenuAdapter;

    private TextView[] menuOptionTitles;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.side_menu_fragment, container, false);
        menu = (ListView) view.findViewById(R.id.side_menu_list);
        learnList = (ListView) view.findViewById(R.id.learn_list);
        mainContainerActivity = (MainContainerActivity) getActivity();
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        menuOptionTitles = new TextView[MenuOption.values().length];

        sideMenuAdapter = new SideMenuAdapter(getActivity());

        menu.setAdapter(sideMenuAdapter);

        menu.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                MenuOption selectedMenuOption = MenuOption.HOME;
                switch (position) {
                    case 0:
                        selectedMenuOption = MenuOption.HOME;
                        break;
                    case 1:
                        selectedMenuOption = MenuOption.PROFILE;
                        break;
                    case 2:
                        selectedMenuOption = MenuOption.PAYMENT;
                        break;
                    case 3:
                        selectedMenuOption = MenuOption.SETTINGS;
                        break;
                    case 4:
                        selectedMenuOption = MenuOption.PROMOTE;
                        break;
                    default:
                        selectedMenuOption = MenuOption.HOME;
                        break;
                }

                mainContainerActivity.setCurrentState(selectedMenuOption, null);
                updateSelectedItem();

                mainContainerActivity.closeDrawer();
            }
        });

        updateSelectedItem();

        learnListAdapter = new LearnListAdapter(getActivity());
        learnList.setAdapter(learnListAdapter);

        updateLearnList();
    }

    public void updateSelectedItem() {
        MenuOption selectedMenuOption = mainContainerActivity.getCurrentState();
        sideMenuAdapter.clear();

        for (MenuOption menuOption : MenuOption.values()) {
            sideMenuAdapter.add(new SideMenuItem(menuOption.title, menuOption.iconRes,
                    (selectedMenuOption == menuOption) ? true : false));
        }

        sideMenuAdapter.notifyDataSetChanged();
    }

    private class SideMenuItem {
        public String tag;
        public int icon;
        public boolean isSelected;
        public SideMenuItem(String tag, int icon, boolean isSelected) {
            this.tag = tag;
            this.icon = icon;
            this.isSelected = isSelected;
        }
    }

    public class SideMenuAdapter extends ArrayAdapter<SideMenuItem> {

        public SideMenuAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.side_menu_list_row,
                        null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
            icon.setImageResource(getItem(position).icon);
            TextView title = (TextView) convertView.findViewById(R.id.row_title);
            title.setText(getItem(position).tag);
            if (getItem(position).isSelected) {
                selectItem(title);
                selectedItem = getItem(position);
            } else {
                deselectItem(title);
            }
            return convertView;
        }

    }

    private class LearnListItem {
        public boolean isSesh;
        public boolean isDivider;
        public LearnRequest learnRequest;
        public Sesh sesh;
        public String dividerText;
        public LearnListItem(boolean isSesh, boolean isDivider, LearnRequest learnRequest, Sesh sesh,
                             String dividerText) {
            this.isSesh = isSesh;
            this.isDivider = isDivider;
            this.learnRequest = learnRequest;
            this.sesh = sesh;
            this.dividerText = dividerText;
        }
    }

    public class LearnListAdapter extends ArrayAdapter<LearnListItem> {
        public LearnListAdapter(Context context) { super(context, 0); }

        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "refreshing position " + position);
            LearnListItem item = getItem(position);
            if (item.isDivider) {
                // if convertView is not instantiated, or is of the wrong type, we re-instantiate
                if (convertView == null || isOpenRequestRow(convertView) || isOpenSeshRow(convertView)) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_request_divider,
                            null);
                }

                TextView dividerText = (TextView) convertView.findViewById(R.id.divider_text);
                dividerText.setText(item.dividerText);
            } else if (item.isSesh) {
                // if convertView is not instantiated, or is of the wrong type, we re-instantiate
                if (convertView == null || isOpenRequestRow(convertView)) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_sesh_list_row,
                            null);
                }
                
                TextView classAbbrvTextView = (TextView) convertView.findViewById(R.id.open_sesh_list_row_class);
                classAbbrvTextView.setText(item.sesh.className);

                Typeface  medium = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Medium.otf");
                classAbbrvTextView.setTypeface(medium);

                TextView timeAbbrvTextView = (TextView) convertView.findViewById(R.id.open_sesh_list_row_time);
                timeAbbrvTextView.setText(item.sesh.getTimeAbbrvString());

                if (!item.sesh.isStudent) {
                    ImageView icon = (ImageView)convertView.findViewById(R.id.open_sesh_list_row_status_icon);
                    int drawableId;

                    if (item.sesh.seshSetTime == null) {
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
            } else {
                // if convertView is not instantiated, or is of the wrong type, we re-instantiate
                if (convertView == null || isOpenSeshRow(convertView))
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_request_list_row,
                            null);

                TextView classAbbrvTextView = (TextView) convertView.findViewById(R.id.open_request_list_row_class);
                classAbbrvTextView.setText(item.learnRequest.classString);
            }

            // if view represents the newest request and side menu was opened in context of a new
            // request being created, animate row in to emphasize it to user.
            if (menuOpenFlag == MENU_OPEN_DISPLAY_NEW_REQUEST && position == getCount() - 1) {
                Log.d("meh", "allegadly animating");
                Animation newItemAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_and_slide_in);
                convertView.startAnimation(newItemAnimation);
                Log.d(TAG, "bout to set this null:");
                setStatusFlag(null);
            }
            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            Log.d(TAG, "notify data set changed");
        }
    }

    private boolean isOpenRequestRow(View view) {
        return (view.findViewById(R.id.open_request_list_row_class) != null);
    }

    private boolean isOpenSeshRow(View view) {
        return (view.findViewById(R.id.open_sesh_list_row_class) != null);
    }

    public void selectItem(TextView title) {
        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Bold.otf");
        title.setTypeface(bold);
    }

    public void deselectItem(TextView title) {
        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Light.otf");
        title.setTypeface(light);
    }

    public LearnListAdapter getLearnListAdapter() {
        return learnListAdapter;
    }

    public void setStatusFlag(String flag) {
        Log.d(TAG, "SETTING MENUOPTIONFLAG " + flag);
        this.menuOpenFlag = flag;
    }

    @Override
    public void onOpened() {
        Log.d(TAG, "onOpened called w/ menuOpenFlag: " + menuOpenFlag);
        if (menuOpenFlag == MENU_OPEN_DISPLAY_NEW_REQUEST) {
            updateLearnList();
        }
    }

    @Override
    public void onSeshInfoUpdate() {
        updateLearnList();
    }

    public synchronized void updateLearnList() {
        Log.d(TAG, "updateLearnList() begin " + new Date().toString() + " with instance " + this.getId());
        List<Sesh> studentSeshes = Sesh.find(Sesh.class, "is_student = ?", Integer.toString(1));
        Iterator<LearnRequest> learnRequests = LearnRequest.findAll(LearnRequest.class);

        int beforeCount = learnListAdapter.getCount();

        learnListAdapter.clear();

        for (Sesh sesh : studentSeshes) {
            LearnListItem seshItem = new LearnListItem(true, false, null, sesh, null);
            learnListAdapter.add(seshItem);
        }

        while (learnRequests.hasNext()) {
            LearnListItem requestItem = new LearnListItem(false, false, learnRequests.next(), null, null);
            learnListAdapter.add(requestItem);
        }

        if (learnListAdapter.getCount() > 0) {
            LearnListItem learnDivider = new LearnListItem(false, true, null, null, "Learn");

            learnListAdapter.insert(learnDivider, 0);
        }

        learnListAdapter.notifyDataSetChanged();
        Log.d(TAG, "updateLearnList() end " + new Date().toString());

    }
}