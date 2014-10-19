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
    private static final long HOLD_DURATION = 300;

    private static final int TURN_OFF = 0;
    private static final int TURN_LEFT = 1;
    private static final int TURN_RIGHT = 2;

    private static final int turnOutPitchMin = 1;//7;
    private static final int turnOutPitchMax = 9;//16;
    private static final int turnOutYawMin = 1;
    private static final int turnOutYawMax = 4;
    private static final int turnInPitchMin = 10;//1;
    private static final int turnInPitchMax = 15;//6;
    private static final int turnInYawMin = 4;
    private static final int turnInYawMax = 9;

    private long lastYawChange, lastPitchChange;
    private int turning = TURN_OFF;

    private int r, g, b;
    private long lastColorChange;

    private boolean mLaunching;
    private Handler mHandler = new Handler();

    private Quaternion orientation;

    int roll_init, pitch_init, yaw_init;
    int roll_w, pitch_w, yaw_w;
    int yaw_base;

    Context mContext;
    SparkLightsFragment sparkLightsFragment;

    Arm mArm;

    private Runnable colorChangeRunnable = new Runnable() {

        @Override
        public void run() {
            makeRequest("setColor", String.format("%03d %03d %03d", r, g, b));
            lastColorChange = System.currentTimeMillis();
        }
    };

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
        lastYawChange = lastPitchChange = System.currentTimeMillis();
        roll_init = pitch_init = yaw_init = -1;
        r = 255;
        g = b = 0;
    }

    public void calibrateYaw() {
        yaw_base = yaw_w;
    }  // TODO: Check Calibration accuracy...

    private void makeRequest(String addUrl, String otherParams) {
        new SparkAsyncTask().execute(addUrl, otherParams);
    }

    public void turnRight() { makeRequest("on", "RIGHT"); turning = TURN_RIGHT; }

    public void turnLeft() { makeRequest("on", "LEFT"); turning = TURN_LEFT; }

    public void turnOff() { makeRequest("off", ""); turning = TURN_OFF; }

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
        //Toast.makeText(mContext, "Myo Orientation: " + quaternion, Toast.LENGTH_SHORT).show();
        //sparkLightsFragment.setStatusText("Myo Orientation: " + quaternion);
        //Log.i(TAG, "Myo Quaternion: " + quaternion);
        orientation = quaternion;
        // Calculate Euler angles (roll, pitch, and yaw) from the unit quaternion.
        double roll = Math.atan2(2.0f * (quaternion.w() * quaternion.x() + quaternion.y() * quaternion.z()),
                1.0f - 2.0f * (quaternion.x() * quaternion.x() + quaternion.y() * quaternion.y()));
        double pitch = Math.asin(Math.max(-1.0f, Math.min(1.0f, 2.0f * (quaternion.w() * quaternion.y() - quaternion.z() * quaternion.x()))));
        double yaw = Math.atan2(2.0f * (quaternion.w() * quaternion.z() + quaternion.x() * quaternion.y()),
                1.0f - 2.0f * (quaternion.y() * quaternion.y() + quaternion.z() * quaternion.z()));
        // Convert the floating point angles in radians to a scale from 0 to 18.
        int roll_w_2 = (int)((roll + (float)Math.PI)/(Math.PI * 2.0f) * 18);
        int pitch_w_2 = (int)((pitch + (float)Math.PI/2.0f)/Math.PI * 18);
        int yaw_w_2 = (int)((yaw + (float)Math.PI)/(Math.PI * 2.0f) * 18);

        long time = System.currentTimeMillis();

        if (pitch_init == -1) {
            pitch_init = pitch_w_2;
            yaw_init = yaw_w_2;
            roll_init = roll_w_2;
        }

        if (pitch_w != pitch_w_2) {
            lastPitchChange = time;
        }
        if (yaw_w != yaw_w_2) {
            lastYawChange = time;
        }

        roll_w = roll_w_2;
        pitch_w = pitch_w_2;
        yaw_w = yaw_w_2;

        //boolean checkTurnStatus = (time - lastPitchChange >= HOLD_DURATION) && (time - lastYawChange >= HOLD_DURATION);
        boolean checkTurnStatus = true;

        if (checkTurnStatus) {
            if (turnOutPitchMin <= pitch_w && pitch_w <= turnOutPitchMax &&
                    turnOutYawMin <= yaw_w - yaw_base && yaw_w - yaw_base <= turnOutYawMax) {
                // Send Spark Commands
                if (turning != TURN_RIGHT) {
                    turnRight();
                    sparkLightsFragment.setSparkText("Turning Out");
                }
            } else if (turnInPitchMin <= pitch_w && pitch_w <= turnInPitchMax &&
                turnInYawMin <= yaw_w - yaw_base && yaw_w - yaw_base <= turnInYawMax) {
                if (turning != TURN_LEFT) {
                    turnLeft();
                    sparkLightsFragment.setSparkText("Turning In");
                }
            } else {
                sparkLightsFragment.setSparkText("Not Turning");
                /*if (turning != TURN_OFF) {
                    turnOff();
                }*/
            }
        } else {
            if (turning != TURN_OFF) {
                sparkLightsFragment.setSparkText("Not Turning");
                turnOff();
                turning = TURN_OFF;
            }
        }

        //Log.i(TAG, "yaw=" + yaw_w + "; pitch=" + pitch_w + "; roll=" + roll_w);
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
}
