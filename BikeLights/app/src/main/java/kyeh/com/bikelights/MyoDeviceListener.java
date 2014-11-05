package kyeh.com.bikelights;

import android.content.Context;
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

    private static final int TURN_YAW_WINDOW = 2;  // Within two divs away from desired yaw value
    private static final int TURN_PITCH_CUTOFF = 17;  // >= Turn inwards, < Turn outwards
    int roll_w, pitch_w, yaw_w, bearing_w;

    private Context mContext;
    private TurnEventListener turnEventListener;
    private SparkLightsFragment sparkLightsFragment;
    private Arm mArm;

    public MyoDeviceListener(Context context) {
        mContext = context;
    }

    public void registerTurnEventListener(TurnEventListener tel) { turnEventListener = tel; }
    public void setSparkFragment(SparkLightsFragment fragment) { sparkLightsFragment = fragment; }

    public void turnRight() {
        SparkClient.turnRight(mContext);
        if (turnEventListener != null) {
            turnEventListener.onTurn(SparkClient.TURN_RIGHT);
        }
    }

    public void turnLeft() {
        SparkClient.turnLeft(mContext);
        if (turnEventListener != null) {
            turnEventListener.onTurn(SparkClient.TURN_LEFT);
        }
    }

    public void turnOff() {
        SparkClient.turnOff(mContext);
        if (turnEventListener != null) {
            turnEventListener.onTurn(SparkClient.TURN_OFF);
        }
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

        // Swap Y and Z axes
        double yaw = Quaternion.yaw(quaternion);
        double pitch = Quaternion.roll(quaternion);
        double roll = Quaternion.pitch(quaternion);

        // Convert the floating point angles in radians to a scale from 0 to 19.
        roll_w = (int)((roll + (float)Math.PI)/(Math.PI * 2.0f) * 20);
        pitch_w = (int)((pitch + (float)Math.PI/2.0f)/Math.PI * 20);
        yaw_w = (int)((yaw + (float)Math.PI)/(Math.PI * 2.0f) * 20);

        if ((isArmOutStraight() || isArmDown()) && pitch_w < TURN_PITCH_CUTOFF) {
            if (SparkClient.turning != SparkClient.TURN_RIGHT) {
                turnRight();
                sparkLightsFragment.setSparkText("Turning Out");
            }
        } else if (isArmUp() && pitch_w >= TURN_PITCH_CUTOFF) {
            if (SparkClient.turning != SparkClient.TURN_LEFT) {
                turnLeft();
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

    public void setBearing(Float bearing) {
        this.bearing_w = (int) ((bearing / 360) * 20);
    }

    public boolean isArmOutStraight() {
        int adjustedYawDiff = Math.abs(((25 - bearing_w) % 20) - yaw_w);
        return adjustedYawDiff <= TURN_YAW_WINDOW || 19 - adjustedYawDiff <= TURN_YAW_WINDOW - 1;
    }

    public boolean isArmUp() {
        int adjustedYawDiff = Math.abs(((30 - bearing_w) % 20) - yaw_w);
        return adjustedYawDiff <= TURN_YAW_WINDOW || 19 - adjustedYawDiff <= TURN_YAW_WINDOW - 1;
    }

    public boolean isArmDown() {
        int adjustedYawDiff = (bearing_w + yaw_w) % 20;
        return adjustedYawDiff <= TURN_YAW_WINDOW || 19 - adjustedYawDiff <= TURN_YAW_WINDOW - 1;
    }
}
