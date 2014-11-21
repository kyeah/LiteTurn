package kyeh.com.bikelights;

import android.content.Context;
import android.os.Handler;

/**
 * This class is a static instantiation that interfaces with the Spark Core and drives the turn light LEDs.
 *
 * Created by kyeh on 11/5/14.
 */
public class SparkClient {

    // Turn Configuration
    public static final int TURN_OFF = 0;
    public static final int TURN_LEFT = 1;
    public static final int TURN_RIGHT = 2;
    public static int turning = TURN_OFF;

    // Turn Signal Color Configuration
    private static final long COLOR_CHANGE_WAIT = 1000;
    private static int r = 255;
    private static int g, b;
    private static long lastColorChange;

    // Delay Configurations
    private static final long HOLD_DURATION = 300;
    private static Handler colorHandler = new Handler();
    private static Handler turnHandler = new Handler();
    private static TurnEventListener turnEventListener;

    private SparkClient() { }
    public static void registerTurnEventListener(TurnEventListener tel) { turnEventListener = tel; }

    public static void makeRequest(final Context context, final String addUrl, final String otherParams) {
        new SparkAsyncTask(context).execute(addUrl, otherParams);
    }

    public static void turnRight(final Context context) {
        if (turning != TURN_RIGHT) {
            turning = TURN_RIGHT;
            Runnable turnRunnable = new Runnable() {
                @Override
                public void run() {
                    if (turnEventListener != null) {
                        turnEventListener.onTurn(SparkClient.TURN_RIGHT);
                    }
                    makeRequest(context, "on", "RIGHT");
                }
            };
            turnHandler.removeCallbacksAndMessages(null);
            turnHandler.postDelayed(turnRunnable, HOLD_DURATION);
            //makeRequest(context, "on", "RIGHT");
        }
    }

    public static void turnLeft(final Context context) {
        if (turning != TURN_LEFT) {
            turning = TURN_LEFT;
            Runnable turnRunnable = new Runnable() {
                @Override
                public void run() {
                    if (turnEventListener != null) {
                        turnEventListener.onTurn(SparkClient.TURN_LEFT);
                    }
                    makeRequest(context, "on", "LEFT");
                }
            };
            turnHandler.removeCallbacksAndMessages(null);
            turnHandler.postDelayed(turnRunnable, HOLD_DURATION);
            //makeRequest(context, "on", "RIGHT");
        }
    }

    public static void turnOff(final Context context) {
        turning = TURN_OFF;
        if (turnEventListener != null) {
            turnEventListener.onTurn(SparkClient.TURN_OFF);
        }
        makeRequest(context, "off", "");
    }

    public static void setColor(final Context context, int _r, int _g, int _b) {
        r = _r % 255;
        g = _g % 255;
        b = _b % 255;

        Runnable colorChangeRunnable = new Runnable() {
            @Override
            public void run() {
                makeRequest(context, "setColor", String.format("%03d %03d %03d", r, g, b));
                lastColorChange = System.currentTimeMillis();
            }
        };

        if (System.currentTimeMillis() - lastColorChange > COLOR_CHANGE_WAIT) {
            colorHandler.post(colorChangeRunnable);
        } else {
            colorHandler.removeCallbacksAndMessages(null);
            colorHandler.postDelayed(colorChangeRunnable, COLOR_CHANGE_WAIT);
        }
    }

    public static void cancelPendingTurns() {
        turnHandler.removeCallbacksAndMessages(null);
        turning = TURN_OFF;
    }
}
