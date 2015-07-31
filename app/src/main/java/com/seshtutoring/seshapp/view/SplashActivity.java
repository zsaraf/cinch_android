package com.seshtutoring.seshapp.view;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.auth.api.Auth;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Rate;
import com.seshtutoring.seshapp.services.SeshGCMListenerService;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.AuthenticationActivity.EntranceType;

/**
 * Created by nadavhollander on 7/10/15.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
        Intent i;
        if (SeshAuthManager.sharedManager(this).isValidSession()) {
            Rate.fetchHourlyRateFromServer(getApplicationContext());
            i = new Intent(this, MainContainerActivity.class);
        } else {
            i = new Intent(this, AuthenticationActivity.class);
            i.putExtra(AuthenticationActivity.ENTRANCE_TYPE_KEY, EntranceType.LOGIN);
        }
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
