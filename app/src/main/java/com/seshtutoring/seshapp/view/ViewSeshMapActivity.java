package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Sesh;

import java.util.List;

public class ViewSeshMapActivity extends SeshActivity implements OnMapReadyCallback {
    private static final String TAG = ViewSeshMapActivity.class.getName();
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String TITLE = "title";
    private GoogleMap mMap;
    private Double latitude;
    private Double longitude;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sesh_map);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.sesh_action_bar);
        getSupportActionBar().setElevation(0);
        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf"));

        ImageButton menuButton = (ImageButton) findViewById(R.id.action_bar_menu_button);
        ViewGroup layout = (ViewGroup) menuButton.getParent();
        layout.removeView(menuButton);

        ImageButton backButton = (ImageButton) findViewById(R.id.action_bar_back_button);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });

        // Get the the sesh
        Bundle b = getIntent().getExtras();
        latitude = b.getDouble(LATITUDE);
        longitude = b.getDouble(LONGITUDE);
        this.title = b.getString(TITLE);
        title.setText(this.title);

        setUpMapIfNeeded();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public GoogleMap getMap() {
        return mMap;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
//        if (mMap == null) {
        // Try to obtain the map from the SupportMapFragment.
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);
//        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
    }

    private void setUpMap() {
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                latLng, 15));

        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
    }
}
