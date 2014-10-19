package kyeh.com.bikelights;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;

/**
 * Created by kyeh on 10/18/14.
 */
public class MyoDeviceListener implements DeviceListener {

    private final String TAG = "MyoDeviceListener";
    private static final long LAUNCH_HOLD_DURATION = 500;


    private static final double xThreshMin = 0.15;
    private static final double xThreshMax = 0.7;
    private static final double yThreshMin = 0.3;
    private static final double yThreshMax = 0.95;
    private static final double zThreshMin = -0.65;
    private static final double zThreshMax = -0.765;

    private boolean mLaunching;
    private Handler mHandler = new Handler();

    private Quaternion orientation;

    Context mContext;
    SparkLightsFragment sparkLightsFragment;

    Arm mArm;

    private Runnable mLaunchRunnable = new Runnable() {
        @Override
        public void run() {
            // Launch Spark Command
            Log.i(TAG, "Launching Spark Command");
        }
    };

    public MyoDeviceListener(Context context, SparkLightsFragment fragment) {
        mContext = context;
        sparkLightsFragment = fragment;
    }

    @Override
    public void onPair(Myo myo, long l) {
        Toast.makeText(mContext, "Myo Paired!", Toast.LENGTH_SHORT).show();
        sparkLightsFragment.setStatusText("Myo Paired!");
        Log.i(TAG, "Myo Paired!");
    }

    @Override
    public void onConnect(Myo myo, long l) {
        Toast.makeText(mContext, "Myo Connected!", Toast.LENGTH_SHORT).show();
        sparkLightsFragment.setStatusText("Myo Connected!");
        Log.i(TAG, "Myo Connected!");
    }

    @Override
    public void onDisconnect(Myo myo, long l) {
        Toast.makeText(mContext, "Myo Disconnected!", Toast.LENGTH_SHORT).show();
        sparkLightsFragment.setStatusText("Myo Disconnected!");
    }

    @Override
    public void onArmRecognized(Myo myo, long l, Arm arm, XDirection xDirection) {
        Toast.makeText(mContext, "Myo Arm Recognized! - " + xDirection, Toast.LENGTH_SHORT).show();
        sparkLightsFragment.setStatusText("Myo Arm Recognized! - " + xDirection);
        Log.i(TAG, "Myo arm recognized");
        mArm = arm;
        sparkLightsFragment.setArmText("ARM: " + (arm == Arm.LEFT ? "LEFT" : "RIGHT"));
    }

    @Override
    public void onArmLost(Myo myo, long l) {
            Toast.makeText(mContext, "Myo Arm Lost!", Toast.LENGTH_SHORT).show();
            sparkLightsFragment.setStatusText("Myo Arm Lost!");
        Log.i(TAG, "Myo arm lost");
        mArm = Arm.UNKNOWN;
        sparkLightsFragment.setArmText("ARM: UNKNOWN");
    }

    @Override
    public void onPose(Myo myo, long l, Pose pose) {
       // if (pose == Pose.FIST || pose == Pose.FINGERS_SPREAD) {
            if (xThreshMin < orientation.x() && orientation.x() < xThreshMax /*&&
                    yThreshMin < orientation.y() && orientation.y() < yThreshMax &&
                    zThreshMin < orientation.z() && orientation.z() < zThreshMax*/) {
                // Send Spark Commands
                sparkLightsFragment.setSparkText("Activated");
            } else {
                sparkLightsFragment.setSparkText("Not Activated");
            }
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

        Toast.makeText(mContext, "Pose: " + pose, Toast.LENGTH_SHORT).show();
        sparkLightsFragment.setStatusText("Myo Pose: " + pose);
        Log.i(TAG, "Myo Pose: " + pose);
    }

    @Override
    public void onOrientationData(Myo myo, long l, Quaternion quaternion) {
        //Toast.makeText(mContext, "Myo Orientation: " + quaternion, Toast.LENGTH_SHORT).show();
        //sparkLightsFragment.setStatusText("Myo Orientation: " + quaternion);
        Log.i(TAG, "Myo Quaternion: " + quaternion);
        orientation = quaternion;
        if (/*xThreshMin < orientation.x() && orientation.x() < xThreshMax &&
                    /*yThreshMin < orientation.y() && orientation.y() < yThreshMax &&*/
                    zThreshMin < orientation.z() && orientation.z() < zThreshMax) {
            // Send Spark Commands
            sparkLightsFragment.setSparkText("Activated");
        } else {
            sparkLightsFragment.setSparkText("Not Activated");
        }
    }

    @Override
    public void onAccelerometerData(Myo myo, long l, Vector3 vector3) {
        //Toast.makeText(mContext, "Myo Accelerometer: " + vector3, Toast.LENGTH_SHORT).show();
        //sparkLightsFragment.setStatusText("Myo Accel: " + vector3);
    }

    @Override
    public void onGyroscopeData(Myo myo, long l, Vector3 vector3) {
        //Toast.makeText(mContext, "Myo Gyroscope: " + vector3, Toast.LENGTH_SHORT).show();
        //sparkLightsFragment.setStatusText("Myo Gyro: " + vector3);
    }

    @Override
    public void onRssi(Myo myo, long l, int i) {
        Toast.makeText(mContext, "Myo Rssi Detected", Toast.LENGTH_SHORT).show();
        sparkLightsFragment.setStatusText("Myo Rssi Detected!");
    }
}
