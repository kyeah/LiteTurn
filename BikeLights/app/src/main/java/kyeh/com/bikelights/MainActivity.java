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

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.thalmic.myo.Hub;

import kyeh.com.bikelights.gestures.GestureDetector;
import kyeh.com.bikelights.gestures.MyoDeviceListener;
import kyeh.com.bikelights.gestures.NativeSensorListener;
import kyeh.com.bikelights.location.BearingsTracker;
import kyeh.com.bikelights.location.TrackerFragment;
import kyeh.com.bikelights.spark.SparkClient;
import kyeh.com.bikelights.spark.SparkLightsFragment;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener,
        SparkClient.TurnEventListener, MyoDeviceListener.OnMyoStatusChangedListener, BearingsTracker.OnLocationChangedListener {

    private static final String TAG = "MainActivity";
    private static String navItems[] = {"Spark Controller", "Location Tracker"};

    // Navigation Drawer State
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mDrawerView;
    private ListView mDrawerList;

    // Modules
    private GestureDetector gestureDetector;
    private BearingsTracker bearingsTracker;

    // Views
    private SparkLightsFragment sparkLightsFragment;
    private TrackerFragment trackerFragment;


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (bearingsTracker != null) {
            bearingsTracker.stop();
        }
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

        SparkClient.registerTurnEventListener(this);
        bearingsTracker = new BearingsTracker(this, this);

        if (savedInstanceState == null) {
            sparkLightsFragment = new SparkLightsFragment();
            trackerFragment = new TrackerFragment();
            trackerFragment.setGoogleApiClient(bearingsTracker.getGoogleApiClient());
        }

        MapsInitializer.initialize(getApplicationContext());

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
        if (gestureDetector != null) {
            gestureDetector.onEnd();
        }
        try {
            Hub.getInstance().shutdown();
        } catch (Exception e) {
            Log.e(TAG, "Failed to shutdown Myo Hub", e);
        }
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
        if (gestureDetector != null) {
            gestureDetector.onResume();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onTurn(int turnDir) {
        if (trackerFragment == null) return;

        if (turnDir == Turn.TURN_LEFT) {
            trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE), getString(R.string.turning_left));
        } else if (turnDir == Turn.TURN_RIGHT) {
            trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE), getString(R.string.turning_right));
        }
    }

    @Override
    public void onGesture(int gestureStatus) {
        switch (gestureStatus) {
            case Turn.TURN_LEFT:
                sparkLightsFragment.setSparkText(getResources().getString(R.string.turning_left));
                if (SparkClient.turning != Turn.TURN_LEFT) {
                    SparkClient.turnLeft(this);
                }
                break;
            case Turn.TURN_RIGHT:
                sparkLightsFragment.setSparkText(getResources().getString(R.string.turning_right));
                if (SparkClient.turning != Turn.TURN_RIGHT) {
                    SparkClient.turnRight(this);
                }
                break;
            case Turn.TURN_OFF:
                sparkLightsFragment.setSparkText(getResources().getString(R.string.not_turning));
                SparkClient.cancelPendingTurns();
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
        sparkLightsFragment.resetActivationText();
        if (gestureDetector != null) {
            boolean isMyo = gestureDetector instanceof MyoDeviceListener;
            gestureDetector.onEnd();
            gestureDetector = null;
            if (isMyo) return;
        }

        gestureDetector = new MyoDeviceListener(this, this, this);
        if (!gestureDetector.isActivated()) {
            gestureDetector = null;
        } else {
            sparkLightsFragment.activateMyoText();
        }
    }

    public void toggleNativeSensors() {
        sparkLightsFragment.resetActivationText();
        if (gestureDetector != null) {
            boolean isNSL = gestureDetector instanceof NativeSensorListener;
            gestureDetector.onEnd();
            gestureDetector = null;
            if (isNSL) return;
        }

        gestureDetector = new NativeSensorListener(this, this);
        if (!gestureDetector.isActivated()) {
            gestureDetector = null;
        } else {
            sparkLightsFragment.activateNativeText();
        }
    }

    @Override
    public void onLocationChanged(Location location, int status) {
        if (trackerFragment != null) {
            trackerFragment.addTrackPoint(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        if (gestureDetector != null) {
            gestureDetector.setBearing(location.getBearing());
        }

        sparkLightsFragment.setBearingText("Bearing: " + location.getBearing());

        if (status == TURN_COMPLETE) {
            trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED), getString(R.string.turn_end_90));
        } else if (status == U_TURN_COMPLETE) {
            trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED), getString(R.string.turn_end_u));
        }

        if (status != NO_TURN && SparkClient.turning != Turn.TURN_OFF) {
            SparkClient.turnOff(this);
        }
    }
}
