package com.seshtutoring.seshapp.view;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.services.SeshGCMListenerService;
import com.seshtutoring.seshapp.util.LaunchPrerequisiteAsyncTask;
import com.seshtutoring.seshapp.util.LaunchPrerequisiteAsyncTask.PrereqsFulfilledListener;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;


/**
 * Created by nadavhollander on 7/10/15.
 */
public class SplashActivity extends SeshActivity {
    private static final String TAG = SplashActivity.class.getName();
    private ImageView logo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_activity);
        this.logo = (ImageView) findViewById(R.id.logo);

        Thread timerThread = new Thread(){
            public void run(){
                try{
                        sleep(1200);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            logo.animate().yBy(-1000f).setInterpolator(new AccelerateInterpolator(2f)).setDuration(300).start();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startInitialActivity();
                                }
                            }, 300);
                        }
                    });
                }
            }
        };

        Intent intent = getIntent();

        if (!intent.hasExtra(SeshGCMListenerService.NOTIFICATION_ID_EXTRA)) {
            timerThread.start();
        } else {
            int notificationId = intent.getIntExtra(SeshGCMListenerService.NOTIFICATION_ID_EXTRA, -1);
            ((NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE)).cancel(notificationId);
            startInitialActivity();
        }
    }

    private void startInitialActivity() {
        if (SeshAuthManager.sharedManager(this).isValidSession()) {
            if (SeshApplication.IS_LIVE) {

                User currentUser = User.currentUser(this);
                if (currentUser.school.enabled) {
                    (new LaunchPrerequisiteAsyncTask(this, new PrereqsFulfilledListener() {
                        @Override
                        public void onPrereqsFulfilled() {
                            showStatusBar();

                            Intent mainContainerIntent = new Intent(getApplicationContext(), MainContainerActivity.class);
                            mainContainerIntent.putExtra(ViewSeshSetTimeActivity.SET_TIME_SESH_ID_KEY, 99);
                            startActivity(mainContainerIntent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    })).execute();
                } else {
                    (new LaunchPrerequisiteAsyncTask(this, new PrereqsFulfilledListener() {
                        @Override
                        public void onPrereqsFulfilled() {
                            showStatusBar();

                            Intent launchSchoolIntent = new Intent(getApplicationContext(), LaunchSchoolActivity.class);
                            startActivity(launchSchoolIntent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    })).execute();
                }


            } else {
                Intent intent = new Intent(getApplicationContext(), UnreleasedLaunchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        } else {
            Intent warmWelcomeIntent = new Intent(getApplicationContext(), WarmWelcomeActivity.class);
            startActivity(warmWelcomeIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void showStatusBar() {
        try {
            ((View) findViewById(android.R.id.title).getParent())
                    .setVisibility(View.VISIBLE);
        } catch (Exception e) {
        }
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public boolean isSplashScreen() {
        return true;
    }
}
