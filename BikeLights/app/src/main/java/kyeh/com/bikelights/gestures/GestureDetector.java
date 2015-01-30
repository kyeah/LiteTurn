package kyeh.com.bikelights.gestures;

import android.util.Log;

import com.thalmic.myo.Quaternion;

import kyeh.com.bikelights.Turn;

/**
 * Created by kyeh on 1/29/15.
 */
public class GestureDetector {

    private static final String TAG = "GestureDetector";

    private static final int TURN_YAW_WINDOW = 5;     // Within two divs away from desired yaw value
    private static final int TURN_PITCH_CUTOFF = 34;  // >= Turn inwards, < Turn outwards
    private int roll_w, pitch_w, yaw_w, bearing_w;

    OnGestureListener onGestureListener;
    boolean lefty = true;
    boolean activated = false;

    public void onResume() {}
    public void onEnd() {}

    public boolean isActivated() { return activated; }
    public void setBearing(Float bearing) {
        this.bearing_w = (int) ((bearing / 360) * 40);
    }

    private boolean isArmOutStraight() {
        int adjustedYawDiff = Math.abs(((50 - bearing_w) % 40) - yaw_w);
        if (lefty) {
            adjustedYawDiff = (adjustedYawDiff + 20) % 40;
        }
        return adjustedYawDiff <= TURN_YAW_WINDOW || 38 - adjustedYawDiff <= TURN_YAW_WINDOW - 2;
    }

    private boolean isArmUp() {
        int adjustedYawDiff = Math.abs(((60 - bearing_w) % 40) - yaw_w);
        return adjustedYawDiff <= TURN_YAW_WINDOW || 38 - adjustedYawDiff <= TURN_YAW_WINDOW - 2;
    }

    private boolean isArmDown() {
        int adjustedYawDiff = (bearing_w + yaw_w) % 40;
        return adjustedYawDiff <= TURN_YAW_WINDOW || 38 - adjustedYawDiff <= TURN_YAW_WINDOW - 2;
    }

    public void onOrientationData(Quaternion quaternion) {
        // Swap Y and Z axes
        double yaw = Quaternion.yaw(quaternion);
        double pitch = Quaternion.roll(quaternion);
        double roll = Quaternion.pitch(quaternion);

        // Convert the floating point angles in radians to a scale from 0 to 19.
        roll_w = (int) ((roll + (float) Math.PI) / (Math.PI * 2.0f) * 40);
        pitch_w = (int) ((pitch + (float) Math.PI / 2.0f) / Math.PI * 40);
        yaw_w = (int) ((yaw + (float) Math.PI) / (Math.PI * 2.0f) * 40);

        int outTurn = (lefty ? Turn.TURN_LEFT : Turn.TURN_RIGHT);
        int inTurn = (lefty ? Turn.TURN_RIGHT : Turn.TURN_LEFT);

        if ((/*isArmOutStraight() ||*/ isArmDown()) && pitch_w < TURN_PITCH_CUTOFF) {
            onGestureListener.onGesture(outTurn);
        } else if (isArmUp() && pitch_w >= TURN_PITCH_CUTOFF) {
            onGestureListener.onGesture(inTurn);
        } else {
            onGestureListener.onGesture(Turn.TURN_OFF);
        }

        Log.i(TAG, "yaw=" + yaw_w + "; pitch=" + pitch_w + "; roll=" + roll_w + "; bearing=" + bearing_w);

    }

    public interface OnGestureListener {
        public void onGesture(int gestureStatus);
    }
}