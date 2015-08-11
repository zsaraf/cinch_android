package com.seshtutoring.seshapp.view;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.services.SeshGCMListenerService;
import com.seshtutoring.seshapp.util.LaunchPrerequisiteUtil;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.view.AuthenticationActivity.EntranceType;

/**
 * Created by nadavhollander on 7/10/15.
 */
public class SplashActivity extends SeshActivity {
    private static final String TAG = SplashActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

        Thread timerThread = new Thread(){
            public void run(){
                try{
                        sleep(1500);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    startInitialActivity();
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

        setContentView(R.layout.splash_activity);
    }

    private void startInitialActivity() {
        if (!SeshApplication.IS_LIVE) {
            Intent warmWelcomeIntent = new Intent(getApplicationContext(), WarmWelcomeActivity.class);
            startActivity(warmWelcomeIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else if (SeshAuthManager.sharedManager(this).isValidSession()) {
            LaunchPrerequisiteUtil.asyncPrepareForLaunch(this, new Runnable() {
                @Override
                public void run() {
                    Intent mainContainerIntent = new Intent(getApplicationContext(), MainContainerActivity.class);
                    startActivity(mainContainerIntent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
        } else {
            Intent i = new Intent(this, AuthenticationActivity.class);
            i.putExtra(AuthenticationActivity.ENTRANCE_TYPE_KEY, EntranceType.LOGIN);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}
