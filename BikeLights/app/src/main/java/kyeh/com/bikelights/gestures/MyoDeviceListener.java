package kyeh.com.bikelights.gestures;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;

/**
 * Created by kyeh on 10/18/14.
 */
public class MyoDeviceListener extends GestureDetector implements DeviceListener {

    private final String TAG = "MyoDeviceListener";

    private Arm mArm;
    private OnMyoStatusChangedListener statusChangedListener;

    public MyoDeviceListener(Context context, OnMyoStatusChangedListener listener) {
        mContext = context;
        statusChangedListener = listener;

        Hub hub = Hub.getInstance();
        if (!hub.init(context)) {
            Log.e(TAG, "Could not initialize the Hub.");
            Toast.makeText(context, "Could not initialize Myo Hub", Toast.LENGTH_LONG).show();
        } else {
            // This will connect to the first Myo that is found
            Hub.getInstance().pairWithAnyMyo();
            Hub.getInstance().addListener(this);
            activated = true;
        }
    }

    public void onResume() {
        try {
            Hub.getInstance().addListener(this);
        } catch (Exception e) {
            Log.d(TAG, "Already listening to Myo.");
        }
    }

    public void onEnd() {
        Hub.getInstance().removeListener(this);
    }

    @Override
    public void onPair(Myo myo, long l) {
        statusChangedListener.onMyoConnectionChanged("Myo Paired!");
    }

    @Override
    public void onConnect(Myo myo, long l) {
        statusChangedListener.onMyoConnectionChanged("Myo Connected!");
    }

    @Override
    public void onDisconnect(Myo myo, long l) {
        statusChangedListener.onMyoConnectionChanged("Myo Disconnected!");
    }

    @Override
    public void onArmRecognized(Myo myo, long l, Arm arm, XDirection xDirection) {
        mArm = arm;
        statusChangedListener.onMyoArmChanged("ARM: " + (arm == Arm.LEFT ? "LEFT" : "RIGHT"));
    }

    @Override
    public void onArmLost(Myo myo, long l) {
        mArm = Arm.UNKNOWN;
        statusChangedListener.onMyoArmChanged("ARM: UNKNOWN");
    }

    @Override
    public void onPose(Myo myo, long l, Pose pose) {

        if (pose == Pose.FIST) {
            
        }

       // if (pose == Pose.FIST || pose == Pose.FINGERS_SPREAD) {
        //    if (xThreshMin < orientation.x() && orientation.x() < xThreshMax /*&&
        //            yThreshMin < orientation.y() && orientation.y() < yThreshMax &&
        //            zThreshMin < orientation.z() && orientation.z() < zThreshMax*/) {
                // Send Spark Commands
        //        sparkLightsFragment.setSparkText("Activated");
        //    } else {
        //        sparkLightsFragment.setSparkText("Not Activated");
        //    }
        //} else {
        //    sparkLightsFragment.setSparkText("Not Activated");
        //}
        /*if (!mLaunching) {
            mLaunching = true;

            // Pose a delayed runnable when the THUMB_TO_PINKY pose is detected, and remove it
            // if the pose is released before the delay ends. This allows triggering actions
            // only if a pose is held for a certain time.
            if (pose == Pose.FIST || pose == Pose.FINGERS_SPREAD) {
                mHandler.postDelayed(mLaunchRunnable, LAUNCH_HOLD_DURATION);
            } else {
                mHandler.removeCallbacks(mLaunchRunnable);
            }
        } else {
            // Swap wave poses if the Myo is on the left arm. Allows user to "wave" right or left
            // regardless of the Myo arm and have the swipes be in the appropriate direction.
            if (mArm == Arm.LEFT) {
                if (pose == Pose.WAVE_IN) {
                    pose = Pose.WAVE_OUT;
                } else if (pose == Pose.WAVE_OUT) {
                    pose = Pose.WAVE_IN;
                }
            }
        }
        }*/

        statusChangedListener.onMyoConnectionChanged("Myo Pose: " + pose);
    }

    @Override
    public void onOrientationData(Myo myo, long l, Quaternion quaternion) {
        super.onOrientationData(quaternion);
    }


    @Override
    public void onAccelerometerData(Myo myo, long l, Vector3 vector3) {
        //Toast.makeText(mContext, "Myo Accelerometer: " + vector3, Toast.LENGTH_SHORT).show();
        //sparkLightsFragment.setStatusText("Myo Accel: " + vector3);
        Log.i(TAG, vector3.toString());
    }

    @Override
    public void onGyroscopeData(Myo myo, long l, Vector3 vector3) {
        //Toast.makeText(mContext, "Myo Gyroscope: " + vector3, Toast.LENGTH_SHORT).show();
        //sparkLightsFragment.setStatusText("Myo Gyro: " + vector3);
    }

    @Override
    public void onRssi(Myo myo, long l, int i) {
        Toast.makeText(mContext, "Myo Rssi Detected", Toast.LENGTH_SHORT).show();
        statusChangedListener.onMyoConnectionChanged("Myo Rssi Detected!");
    }

    public interface OnMyoStatusChangedListener {
        public void onMyoArmChanged(String text);
        public void onMyoConnectionChanged(String text);
    }
}
