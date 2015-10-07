package com.seshtutoring.seshapp.view;

import android.os.Handler;
import android.support.v4.app.Fragment;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.PaymentFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ProfileFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.PromoteFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.SettingsFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.ViewRequestFragment;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragment;

import java.util.Map;

/**
 * Created by nadavhollander on 9/10/15.
 */
public class MainContainerStateManager {
    private MainContainerActivity mainContainerActivity;
    private SideMenuFragment sideMenuFragment;
    private ContainerState currentMainContainerState;

    public enum NavigationItemState {
        HOME, PROFILE, PAYMENT, SETTINGS, PROMOTE
    }

    public MainContainerStateManager(MainContainerActivity mainContainerActivity,
                                     SideMenuFragment sideMenuFragment) {
        this.mainContainerActivity = mainContainerActivity;
        this.sideMenuFragment = sideMenuFragment;
    }

    private void setContainerState(ContainerState state) {
        setContainerState(state, null);
    }

    public void setContainerStateForNavigation(NavigationItemState navigationItemState) {
        setContainerStateForNavigation(navigationItemState, null);
    }

    public void setContainerStateForNavigationIndex(int index) {
        setContainerStateForNavigationIndex(index, null);
    }

    public void setContainerStateForNavigationIndex(int index, Map<String, Object> options) {
        setContainerStateForNavigation(NavigationItemState.values()[index], options);
    }

    public void setContainerStateForSesh(Sesh sesh) {
        setContainerState(new ContainerState("Sesh", 0,
                ViewSeshFragment.newInstance(sesh.seshId, false),
                sesh.getContainerStateTag(),
                false));
    }

    public void setContainerStateForSeshWithMessaging(Sesh sesh) {
        setContainerState(new ContainerState("Sesh", 0,
                ViewSeshFragment.newInstance(sesh.seshId, true),
                sesh.getContainerStateTag(),
                false));
    }

    public void setContainerStateForLearnRequest(LearnRequest learnRequest) {
        setContainerState(new ContainerState("Request", 0,
                ViewRequestFragment.newInstance(learnRequest.learnRequestId),
                learnRequest.getContainerStateTag(),
                false));
    }

    public void setContainerStateForNavigation(NavigationItemState navigationItemState, Map<String, Object> options) {
        setContainerState(getContainerStateForNavigationItem(navigationItemState), options);
    }

    public ContainerState getContainerStateForNavigationItem(NavigationItemState item){
        ContainerState containerState = null;
        switch(item) {
            case HOME:
                containerState = new ContainerState("Home", R.drawable.home, new HomeFragment(), "home", true);
                break;
            case PROFILE:
                containerState = new ContainerState("Profile", R.drawable.profile, new ProfileFragment(), "profile", true);
                break;
            case PAYMENT:
                containerState = new ContainerState("Payment", R.drawable.payment, new PaymentFragment(), "payment", true);
                break;
            case SETTINGS:
                containerState = new ContainerState("Settings", R.drawable.settings, new SettingsFragment(), "settings", true);
                break;
            case PROMOTE:
                containerState = new ContainerState("Promote", R.drawable.share, new PromoteFragment(), "promote", true);
                break;
        }
        return containerState;
    }

    public void setContainerState(ContainerState newState, Map<String, Object> options) {
        // handle side menu
        mainContainerActivity.replaceCurrentFragment(currentMainContainerState, newState, options);
        sideMenuFragment.selectItemForContainerState(newState);
        currentMainContainerState = newState;
    }

    public ContainerState getMainContainerState() {
        return currentMainContainerState;
    }

    public void closeDrawer() {
        mainContainerActivity.closeDrawer(true);
    }

    public void closeDrawerWithDelay(final int millisDelay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                closeDrawer();
            }
        }, millisDelay);
    }
}
