package com.seshtutoring.seshapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;

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

        setContentView(R.layout.splash_activity);

        final Context context = this;

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(1500);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent i;
                    if (SeshAuthManager.sharedManager(context).isValidSession()) {
                        i = new Intent(context, MainContainerActivity.class);
                    } else {
                        i = new Intent(context, AuthenticationActivity.class);
                    }
                    startActivity(i);
                }
            }
        };
        timerThread.start();
    }
}
