package kyeh.com.bikelights;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    private static final long HOLD_DURATION = 300;

    private static final int TURN_OFF = 0;
    private static final int TURN_LEFT = 1;
    private static final int TURN_RIGHT = 2;

    private static final int turnYawWindow = 2;  // Within two divs away from desired yaw value
    private static final int turnPitchCutoff = 17;  // >= Turn inwards, < Turn outwards
    float bearing = 0;
    int bearing_w = 0;

    private int turning = TURN_OFF;

    private int r, g, b;
    private long lastColorChange;

    private boolean mLaunching;
    private Handler mHandler = new Handler();

    private Quaternion orientation;

    int roll_w, pitch_w, yaw_w;
    int yaw_base;

    Context mContext;
    SparkLightsFragment sparkLightsFragment;
    TrackerFragment trackerFragment;

    Arm mArm;

    private Runnable colorChangeRunnable = new Runnable() {

        @Override
        public void run() {
            makeRequest("setColor", String.format("%03d %03d %03d", r, g, b));
            lastColorChange = System.currentTimeMillis();
        }
    };

    private Runnable mLeftRunnable = new Runnable() {
        @Override
        public void run() {
            makeRequest("on", "LEFT");
        }
    };

    private Runnable mRightRunnable = new Runnable() {
        @Override
        public void run() {
            makeRequest("on", "RIGHT");
        }
    };

    private Runnable mOffRunnable = new Runnable() {
        @Override
        public void run() {
            makeRequest("off", "");
        }
    };

    public MyoDeviceListener(Context context, SparkLightsFragment fragment, TrackerFragment tfragment) {
        mContext = context;
        sparkLightsFragment = fragment;
        trackerFragment = tfragment;
        r = 255;
        g = b = 0;
    }

    public void calibrateYaw() {
        yaw_base = yaw_w;
    }  // TODO: Check Calibration accuracy...

    private void makeRequest(String addUrl, String otherParams) {
        new SparkAsyncTask(mContext).execute(addUrl, otherParams);
    }

    public void turnRight() { turnRight(false); }
    public void turnLeft() { turnLeft(false); }

    public void turnRight(boolean mapPt) {
        makeRequest("on", "RIGHT");
        turning = TURN_RIGHT;
        if (mapPt && trackerFragment != null) {
            trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE), "Turning Right");
        }
    }

    public void turnLeft(boolean mapPt) {
        makeRequest("on", "LEFT");
        turning = TURN_LEFT;
        if (mapPt && trackerFragment != null) {
            trackerFragment.marker(null, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE), "Turning Left");
        }
    }

    public void turnOff() {
        makeRequest("off", "");
        turning = TURN_OFF;
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

        Toast.makeText(mContext, "Pose: " + pose, Toast.LENGTH_SHORT).show();
        sparkLightsFragment.setStatusText("Myo Pose: " + pose);
        Log.i(TAG, "Myo Pose: " + pose);
    }

    @Override
    public void onOrientationData(Myo myo, long l, Quaternion quaternion) {

        orientation = quaternion;
        double yaw = Quaternion.yaw(quaternion);
        double pitch = Quaternion.roll(quaternion);
        double roll = Quaternion.pitch(quaternion);

        // Convert the floating point angles in radians to a scale from 0 to 19.
        int roll_w_2 = (int)((roll + (float)Math.PI)/(Math.PI * 2.0f) * 20);
        int pitch_w_2 = (int)((pitch + (float)Math.PI/2.0f)/Math.PI * 20);
        int yaw_w_2 = (int)((yaw + (float)Math.PI)/(Math.PI * 2.0f) * 20);

        long time = System.currentTimeMillis();

        roll_w = roll_w_2;
        pitch_w = pitch_w_2;
        yaw_w = yaw_w_2;

        if ((isArmOutStraight() || isArmDown()) && pitch_w < turnPitchCutoff) {
            if (turning != TURN_RIGHT) {
                turnRight(true);
                sparkLightsFragment.setSparkText("Turning Out");
            }
        } else if (isArmUp() && pitch_w >= turnPitchCutoff) {
            if (turning != TURN_LEFT) {
                turnLeft(true);
                sparkLightsFragment.setSparkText("Turning In");
            }
        } else {
                sparkLightsFragment.setSparkText("Not Turning");
        }

        //Log.i(TAG, "yaw=" + yaw_w + "; pitch=" + pitch_w + "; roll=" + roll_w + "; bearing=" + bearing_w);
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
        sparkLightsFragment.setStatusText("Myo Rssi Detected!");
    }

    public void turnEnded() {
        if (turning != TURN_OFF) {
            Log.d(TAG, "Turning Ended");
            turnOff();
            turning = TURN_OFF;
        }
    }

    public void setColor(int _r, int _g, int _b) {
        r = _r % 255;
        g = _g % 255;
        b = _b % 255;

        long colorChangeWait = 1000;
        if (System.currentTimeMillis() - lastColorChange > colorChangeWait) {
            mHandler.post(colorChangeRunnable);
        } else {
            mHandler.removeCallbacks(colorChangeRunnable);
            mHandler.postDelayed(colorChangeRunnable, colorChangeWait);
        }
    }

    public void setBearing(Float bearing) {
        this.bearing = bearing;
        this.bearing_w = (int) ((bearing / 360) * 20);
    }

    public boolean isArmOutStraight() {
        int adjustedYawDiff = Math.abs(((25 - bearing_w) % 20) - yaw_w);
        return adjustedYawDiff <= turnYawWindow || 19 - adjustedYawDiff <= turnYawWindow - 1;
    }

    public boolean isArmUp() {
        int adjustedYawDiff = Math.abs(((30 - bearing_w) % 20) - yaw_w);
        return adjustedYawDiff <= turnYawWindow || 19 - adjustedYawDiff <= turnYawWindow - 1;
    }

    public boolean isArmDown() {
        int adjustedYawDiff = (bearing_w + yaw_w) % 20;
        return adjustedYawDiff <= turnYawWindow || 19 - adjustedYawDiff <= turnYawWindow - 1;
    }
}
