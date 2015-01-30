package kyeh.com.bikelights;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.thalmic.myo.Hub;

import java.util.ArrayList;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener,
        SparkClient.TurnEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MyoDeviceListener.OnMyoStatusChangedListener {

    private static final String TAG = "MainActivity";
    private static String navItems[] = {"Spark Controller", "Location Tracker"};

    // Navigation Drawer State
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mDrawerView;
    private ListView mDrawerList;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private GestureDetector gestureDetector;

    private SparkLightsFragment sparkLightsFragment;
    private TrackerFragment trackerFragment;

    private LocationListener locationListener;

    @Override
    protected void onStart() {
        super.onStart();

        // Connect the Google API Client.
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        mDrawerLayout = (DrawerLayout)          findViewById(R.id.drawer_layout);
        mDrawerView =   (View)                  findViewById(R.id.left_drawer);
        mDrawerList =   (ListView)              findViewById(R.id.drawer_list);

        // Initialize the navigation drawer
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, navItems));

        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                selectNavItem(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (savedInstanceState == null) {
            sparkLightsFragment = new SparkLightsFragment();
            trackerFragment = new TrackerFragment();
            trackerFragment.setGoogleApiClient(mGoogleApiClient);
        }

        SparkClient.registerTurnEventListener(this);

        MapsInitializer.initialize(getApplicationContext());
        locationListener = new LocationListener() {

            private static final int bearingsWindow = 5;  // Keep last 10 bearings
            private static final float bearingsTolerance = 15;  // 30-degree turn tolerance on each side

            ArrayList<Float> lastBearings = new ArrayList<Float>();
            private int count = 0;
            private int ucount = 0;

            @Override
            public void onLocationChanged(Location location) {
                if (trackerFragment != null) {
                    trackerFragment.addTrackPoint(new LatLng(location.getLatitude(), location.getLongitude()));
                }

                if (location.hasBearing()) {
                    Float bearing = location.getBearing();

                    if (gestureDetector != null) {
                        gestureDetector.setBearing(bearing);

                        for (int i = 0; i < lastBearings.size(); i++) {
                            float absDiff = Math.abs(lastBearings.get(i) - bearing);
                            if (absDiff > 180) {
                                absDiff = 360 - absDiff;
                            }

                            if (Math.abs(90 - absDiff) < bearingsTolerance) {
                                if (SparkClient.turning != Turning.TURN_OFF) {
                                    SparkClient.turnOff(MainActivity.this);
                                }
                                Log.i(TAG, "Detected 90-degreeish turn: " + absDiff);
                                trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN), getString(R.string.turn_end_90));
                                lastBearings.clear();  // Got our turn; only keep that new bearing
                                count++;
                                break;
                            } else if (Math.abs(180 - absDiff) < bearingsTolerance) {
                                if (SparkClient.turning != Turning.TURN_OFF) {
                                    SparkClient.turnOff(MainActivity.this);
                                }
                                Log.i(TAG, "Detected U-turn: " + absDiff);
                                trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED), getString(R.string.turn_end_u));
                                lastBearings.clear();
                                ucount++;
                                break;
                            }
                        }
                    }

                    lastBearings.add(bearing);
                    if (lastBearings.size() > bearingsWindow) {
                        lastBearings.remove(0);
                    }

                    Log.i(TAG, Float.toString(location.getBearing()));
                    Log.i(TAG, lastBearings.toString());
                    sparkLightsFragment.setBearingText("Got Bearing: " + bearing + " with total turns: " + count + ", u-turns: " + ucount + " and history " + lastBearings.toString());
                }
            }
        };

        selectNavItem(0);
    }

    private void selectNavItem(int position) {
        Fragment f;
        switch (position) {
            case 0: f = sparkLightsFragment; break;
            default: f = trackerFragment;
        }

        getFragmentManager().beginTransaction().replace(R.id.content_frame, f).commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(navItems[position]);
        mDrawerLayout.closeDrawer(mDrawerView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        gestureDetector.onEnd();
        Hub.getInstance().shutdown();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (trackerFragment != null) {
            trackerFragment.saveKML("spark_track");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gestureDetector.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onTurn(int turnDir) {
        if (trackerFragment == null) return;

        if (turnDir == Turning.TURN_LEFT) {
            trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE), getString(R.string.turning_left));
        } else if (turnDir == Turning.TURN_RIGHT) {
            trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE), getString(R.string.turning_right));
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Request updates with GPS accuracy at a 1s update rate and 10m position change
        // Typical city blocks are about 100m long, for reference.
        // Need to check the units of these arguments.
        // 25 10f
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setSmallestDisplacement(10);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationListener);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection Suspended - " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection Failed: " + connectionResult);
    }

    @Override
    public void onGesture(int gestureStatus) {
        switch (gestureStatus) {
            case Turning.TURN_LEFT:  sparkLightsFragment.setSparkText(getResources().getString(R.string.turning_left));
            case Turning.TURN_RIGHT: sparkLightsFragment.setSparkText(getResources().getString(R.string.turning_right));
            case Turning.TURN_OFF:   sparkLightsFragment.setSparkText(getResources().getString(R.string.not_turning));
        }
    }

    @Override
    public void onMyoArmChanged(String text) {
        sparkLightsFragment.setArmText(text);
    }

    @Override
    public void onMyoConnectionChanged(String text) {
        sparkLightsFragment.setStatusText(text);
    }

    public void toggleMyo() {
        if (gestureDetector != null) {
            boolean isMyo = gestureDetector instanceof MyoDeviceListener;
            gestureDetector.onEnd();
            gestureDetector = null;
            if (isMyo) return;
        }

        gestureDetector = new MyoDeviceListener(this, this);
        if (!gestureDetector.isActivated()) {
            gestureDetector = null;
        }
    }

    public void toggleNativeSensors() {
        if (gestureDetector != null) {
            boolean isNSL = gestureDetector instanceof NativeSensorListener;
            gestureDetector.onEnd();
            gestureDetector = null;
            if (isNSL) return;
        }

        gestureDetector = new NativeSensorListener(this);
        if (!gestureDetector.isActivated()) {
            gestureDetector = null;
        }
    }
}
