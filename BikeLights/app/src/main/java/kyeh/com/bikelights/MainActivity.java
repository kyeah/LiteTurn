package kyeh.com.bikelights;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Quaternion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener {

    private static final int accelWindow = 100;

    private final String TAG = "MainActivity";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mRotationVector;
    private LocationManager mLocationManager;

    private MyoDeviceListener myoDeviceListener;

    private ChartFragment chartFragment;
    private SparkLightsFragment sparkLightsFragment;
    private TrackerFragment trackerFragment;

    private ArrayList<AccelPoint> accelData = new ArrayList<AccelPoint>();
    private AccelPoint lastAvgAccel = new AccelPoint(System.currentTimeMillis(), 0, 0, 0);
    private AccelPoint avgAccel = new AccelPoint(System.currentTimeMillis(), 0, 0, 0);

    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        registerSensors();


        Hub hub = Hub.getInstance();
        if (!hub.init(this)) {
            Log.e(TAG, "Could not initialize the Hub.");
            //finish();
            //return;
        }

        if (savedInstanceState == null) {
            /*chartFragment = new ChartFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, chartFragment)
                    .commit();*/

            sparkLightsFragment = new SparkLightsFragment();
            trackerFragment = new TrackerFragment();
            trackerFragment.setLocationManager(mLocationManager);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, sparkLightsFragment)
                    .commit();
        }

        myoDeviceListener = new MyoDeviceListener(this, sparkLightsFragment, trackerFragment);
        sparkLightsFragment.setMyoDeviceListener(myoDeviceListener);

        // This will connect to the first Myo that is found
        /*
        Hub.getInstance().pairWithAnyMyo();
        Hub.getInstance().addListener(myoDeviceListener);
*/

        LocationListener locationListener = new LocationListener() {

            private static final int bearingsWindow = 5;  // Keep last 10 bearings
            private static final float bearingsTolerance = 15;  // 30-degree turn tolerance on each side
            private int count = 0;
            private int ucount = 0;
            ArrayList<Float> lastBearings = new ArrayList<Float>();

            @Override
            public void onLocationChanged(Location location) {
                if (trackerFragment != null) {
                    trackerFragment.addTrackPoint(new LatLng(location.getLatitude(), location.getLongitude()));
                }

                if (location.hasBearing()) {
                    Float bearing = location.getBearing();
                    if (myoDeviceListener != null) {
                        myoDeviceListener.setBearing(bearing);
                    }

                    for (int i = 0; i < lastBearings.size() /*/ 2*/; i++) {
                        float absDiff = Math.abs(lastBearings.get(i) - bearing);
                        if (absDiff > 180) {
                            absDiff = 360 - absDiff;
                        }

                        if (Math.abs(90 - absDiff) < bearingsTolerance) {
                            if (myoDeviceListener != null) {
                                myoDeviceListener.turnEnded();
                                Log.i(TAG, "Detected 90-degreeish turn: " + absDiff);
                                trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN), "End of 90-degree Turn");
                                lastBearings.clear();  // Got our turn; only keep that new bearing
                                count++;
                                break;
                            }
                        } else if (Math.abs(180 - absDiff) < bearingsTolerance) {
                            if (myoDeviceListener != null) {
                                myoDeviceListener.turnEnded();
                                Log.i(TAG, "Detected U-turn: " + absDiff);
                                trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED), "End of U-Turn");
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
                    sparkLightsFragment.setBearingText("FUCK THE ARM! Got Bearing: " + bearing + " with total turns: " + count + ", u-turns: " + ucount + " and history " + lastBearings.toString());
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        // Request updates with GPS accuracy at a 1s update rate and 10m position change
        // Typical city blocks are about 100m long, for reference.
        // Need to check the units of these arguments.
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 25, 10f, locationListener);

        AsyncHttpClient.getDefaultInstance().websocket(getResources().getString(R.string.debugging_uri),
                null, new AsyncHttpClient.WebSocketConnectCallback() {

                    @Override
                    public void onCompleted(Exception ex, WebSocket webSocket) {
                        if (ex != null) {
                            ex.printStackTrace();
                            return;
                        }

                        MainActivity.this.webSocket = webSocket;
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("acceldata", accelData);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        accelData = (ArrayList<AccelPoint>) savedInstanceState.getSerializable("accelData");
    }

    @Override
    protected void onDestroy() {
        mSensorManager.unregisterListener(this);
        Hub.getInstance().removeListener(myoDeviceListener);
        Hub.getInstance().shutdown();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        //Hub.getInstance().removeListener(myoDeviceListener);
        try {
            saveData();
        } catch (Exception e) {}
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensors();
        if (myoDeviceListener != null) {
            try {
                Hub.getInstance().addListener(myoDeviceListener);
            } catch (Exception e) {
                Log.d(TAG, "Already listening to Myo.");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long time = System.currentTimeMillis();
        Sensor sensor = sensorEvent.sensor;

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelData.add(new AccelPoint(time, x, y, z));
/*                avgAccel.plus(new AccelPoint(time, x, y, z));
                if (accelData.size() > 2*accelWindow) {
                    AccelPoint val = accelData.remove(0);
                    AccelPoint val2 = accelData.get(accelWindow -1);
                    lastAvgAccel.minus(val.div(accelWindow));
                    lastAvgAccel.plus(val2.div(accelWindow));
                    avgAccel.minus(val2.div(accelWindow));
                }

                double turnTolerance = 0.01;
                double dot = lastAvgAccel.getX()*avgAccel.getX() + lastAvgAccel.getY()*avgAccel.getY();// + lastAvgAccel.getZ()*avgAccel.getZ();
                Log.i(TAG, Double.toString(dot));
                if (dot < turnTolerance) {
                    myoDeviceListener.turnEnded();
                }
*/
                long t = accelData.get(0).getTimestamp();
                if (chartFragment != null) {
                    chartFragment.addAccelerometerValue(time - t, x, y, z);
                }
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                float w = FloatMath.sqrt(1 - x * x - y * y - z * z);

                if (webSocket != null && webSocket.isOpen()) {
                    float[] rot = new float[9];
                    float[] rotMap = new float[9];
                    SensorManager.getRotationMatrixFromVector(rot, sensorEvent.values);
                    SensorManager.remapCoordinateSystem(rot, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotMap);

                    String s = Float.toString(rotMap[0]);
                    for (int i = 1; i < rotMap.length; i++) {
                        s += " " + rotMap[i];
                    }
                    webSocket.send(s);
                }
                if (myoDeviceListener != null) {
                    myoDeviceListener.onOrientationData(null, 0, new Quaternion(x, y, z, w));
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void registerSensors() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
    }


    /**
     * Saves the accelerometer data to disk.
     *
     * @throws IOException
     */
    public void saveData() throws IOException {
        File ext = Environment.getExternalStorageDirectory();
        String filename = ext.getAbsolutePath() + "/" + R.string.accel_data_filename;

        File archive = getFileStreamPath(filename);
        if (archive.exists() || archive.createNewFile()) {
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            PrintStream ps = new PrintStream(fos);

            for (AccelPoint pt : accelData) {
                ps.println(pt);
            }

            ps.flush();
            ps.close();
            fos.close();
        }
    }
}
