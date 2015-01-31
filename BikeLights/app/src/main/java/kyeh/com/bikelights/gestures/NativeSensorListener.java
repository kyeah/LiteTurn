package kyeh.com.bikelights.gestures;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.FloatMath;
import android.util.Log;

import com.thalmic.myo.Quaternion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import kyeh.com.bikelights.R;

/**
 * Created by kyeh on 1/29/15.
 */
public class NativeSensorListener extends GestureDetector implements SensorEventListener {

    private static final String TAG = "NativeSensorListener";
    private static final int accelWindow = 100;

    private ArrayList<AccelPoint> accelData = new ArrayList<AccelPoint>();
    private AccelPoint lastAvgAccel = new AccelPoint(System.currentTimeMillis(), 0, 0, 0);
    private AccelPoint avgAccel = new AccelPoint(System.currentTimeMillis(), 0, 0, 0);

    private Context mContext;

    // Sensor States
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mRotationVector;

    public NativeSensorListener(Context context, OnGestureListener onGestureListener) {
        super(onGestureListener);
        mContext = context;

        // Initialize the native sensors
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        registerSensors();
    }

    public void registerSensors() {
        try {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
            activated = true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to register sensors", e);
        }
    }

    public void onResume() {
        registerSensors();
    }

    public void onEnd() {
        mSensorManager.unregisterListener(this);
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
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                float w = FloatMath.sqrt(1 - x * x - y * y - z * z);
                Quaternion q = new Quaternion(x, y, z, w);  // TODO: Check how to swap pitch/roll
                super.onOrientationData(q);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Saves the accelerometer data to disk.
     *
     * @throws java.io.IOException
     */
    public void saveData() throws IOException {
        File ext = Environment.getExternalStorageDirectory();
        String filename = ext.getAbsolutePath() + "/" + R.string.accel_data_filename;

        File archive = mContext.getFileStreamPath(filename);
        if (archive.exists() || archive.createNewFile()) {
            FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            PrintStream ps = new PrintStream(fos);

            for (AccelPoint pt : accelData) {
                ps.println(pt);
            }

            ps.flush();
            ps.close();
            fos.close();
        }

        Log.e(TAG, "Saved accel data to " + filename);
    }
}
