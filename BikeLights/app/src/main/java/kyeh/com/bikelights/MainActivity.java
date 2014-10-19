package kyeh.com.bikelights;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.thalmic.myo.Hub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener {

    private final String TAG = "MainActivity";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mRotationVector;

    private MyoDeviceListener myoDeviceListener;

    private ChartFragment chartFragment;
    private SparkLightsFragment sparkLightsFragment;
    private ArrayList<AccelPoint> accelData = new ArrayList<AccelPoint>();

    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        registerSensors();

        Hub hub = Hub.getInstance();
        if (!hub.init(this)) {
            Log.e(TAG, "Could not initialize the Hub.");
            finish();
            return;
        }

        /*
        Intent intent = new Intent(context, ScanActivity.class);
        context.startActivity(intent);
        */

        if (savedInstanceState == null) {
            /*chartFragment = new ChartFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, chartFragment)
                    .commit();*/

            sparkLightsFragment = new SparkLightsFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, sparkLightsFragment)
                    .commit();
        }

        myoDeviceListener = new MyoDeviceListener(this, sparkLightsFragment);

        // This will connect to the first Myo that is found
        Hub.getInstance().pairWithAnyMyo();
        Hub.getInstance().addListener(myoDeviceListener);

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
                long t = accelData.get(0).getTimestamp();
                if (chartFragment != null) {
                    chartFragment.addAccelerometerValue(time - t, x, y, z);
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                if (webSocket != null && webSocket.isOpen()) {
                    webSocket.send(sensorEvent.values[0] + " " +
                                   sensorEvent.values[1] + " " +
                                   sensorEvent.values[2]);
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void registerSensors() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
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
