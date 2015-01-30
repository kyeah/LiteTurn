package kyeh.com.bikelights.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by kyeh on 1/30/15.
 */
public class BearingsTracker implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "BearingsTracker";
    private static final int bearingsWindow = 5;  // Keep last 10 bearings
    private static final float bearingsTolerance = 15;  // 30-degree turn tolerance on each side

    private ArrayList<Float> lastBearings = new ArrayList<Float>();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private OnLocationChangedListener onLocationChangedListener;

    public BearingsTracker(Context context, OnLocationChangedListener lcl) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Connect the Google API Client.
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
        }

        onLocationChangedListener = lcl;
    }

    public GoogleApiClient getGoogleApiClient() { return mGoogleApiClient; }

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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
    public void onLocationChanged(Location location) {
        if (location.hasBearing()) {
            Float bearing = location.getBearing();

            for (int i = 0; i < lastBearings.size(); i++) {
                float absDiff = Math.abs(lastBearings.get(i) - bearing);
                if (absDiff > 180) {
                    absDiff = 360 - absDiff;
                }

                if (Math.abs(90 - absDiff) < bearingsTolerance) {
                    Log.i(TAG, "Detected 90-degreeish turn: " + absDiff);
                    if (onLocationChangedListener != null) {
                        onLocationChangedListener.onLocationChanged(location, OnLocationChangedListener.TURN_COMPLETE);
                    }
                    lastBearings.clear();  // Got our turn; only keep that new bearing
                    break;
                } else if (Math.abs(180 - absDiff) < bearingsTolerance) {
                    Log.i(TAG, "Detected U-turn: " + absDiff);
                    if (onLocationChangedListener != null) {
                        onLocationChangedListener.onLocationChanged(location, OnLocationChangedListener.U_TURN_COMPLETE);
                    }
                    lastBearings.clear();
                    break;
                } else {
                    if (onLocationChangedListener != null) {
                        onLocationChangedListener.onLocationChanged(location, OnLocationChangedListener.NO_TURN);
                    }
                }
            }

            lastBearings.add(bearing);
            if (lastBearings.size() > bearingsWindow) {
                lastBearings.remove(0);
            }
        }
    }

    public void stop() {
        // Invalidate the Api Client
        mGoogleApiClient.disconnect();
    }

    public interface OnLocationChangedListener {
        public static final int TURN_COMPLETE = 0;
        public static final int U_TURN_COMPLETE = 1;
        public static final int NO_TURN = 2;

        public void onLocationChanged(Location location, int status);
    }
}
