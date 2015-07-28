package me.brendanweinstein.pkexample;

import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.brendan.pkexample.R;

/**
 * 
 * @author Brendan Weinstein
 * http://www.brendanweinstein.me
 *
 */
public class PKActivity extends FragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fragment_holder, new PKFragment(), "CardFragment");
		ft.commit();
	}

}