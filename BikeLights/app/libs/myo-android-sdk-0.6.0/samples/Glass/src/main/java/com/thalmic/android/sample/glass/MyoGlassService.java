/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.glass;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.XDirection;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//
// This sample demonstrates how to connect to a Myo and use it to trigger touch pad motion
// events that Google Glass recognizes as taps and swipes.
//
// Due to Android security restrictions, the motion events dispatched by this service will only be
// sent to activities in the same application.
//
public class MyoGlassService extends Service {
    private static final String TAG = "MyoGlassService";

    private static final String PREF_MAC_ADDRESS = "PREF_MAC_ADDRESS";

    private Hub mHub;
    private SharedPreferences mPrefs;
    private boolean mActivityActive;
    private MyoListener mListener = new MyoListener();

    // Return an interface to use to communicate with the service.
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new MBinder();

    // The Binder class clients will use to communicate with this service. We know clients in this
    // sample will always run in the same process as the service, so we don't need to deal with IPC.
    public class MBinder extends Binder {
        public MyoGlassService getService() {
            return MyoGlassService.this;
        }
    }

    // Set the active state of the activity.
    public void setActivityActive(boolean active) {
        mActivityActive = active;
    }

    // Unpair with the currently paired Myo, if any, and pair with a new one.
    public void pairWithNewMyo() {
        // Unpair with the previously paired Myo, if it exists.
        mHub.unpair(mPrefs.getString(PREF_MAC_ADDRESS, ""));

        // Clear the saved Myo mac address.
        mPrefs.edit().putString(PREF_MAC_ADDRESS, "").apply();

        // Begin looking for an adjacent Myo to pair with.
        mHub.pairWithAdjacentMyo();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // First, we initialize the Hub singleton with an application identifier.
        mHub = Hub.getInstance();
        if (!mHub.init(this, getPackageName())) {
            Log.e(TAG, "Could not initialize the Hub.");
            stopSelf();
            return;
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Register for DeviceListener callbacks.
        mHub.addListener(mListener);

        // If there is no connected Myo, try to pair with one.
        if (mHub.getConnectedDevices().isEmpty()) {
            String myoAddress = mPrefs.getString(PREF_MAC_ADDRESS, "");

            // If we have a saved Myo MAC address then connect to it, otherwise look for one nearby.
            if (TextUtils.isEmpty(myoAddress)) {
                mHub.pairWithAdjacentMyo();
            } else {
                mHub.pairByMacAddress(myoAddress);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Release any resources held by the Hub and MyoListener.
        mHub.shutdown();
        mListener.shutdown();
    }

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private class MyoListener extends AbstractDeviceListener {
        private static final long LAUNCH_HOLD_DURATION = 1000;

        private Handler mHandler = new Handler();
        private Instrumentation mInstrumentation = new Instrumentation();
        private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

        // The arm that Myo is on is unknown until the arm recognized event is received.
        private Arm mArm = Arm.UNKNOWN;

        private Runnable mLaunchRunnable = new Runnable() {
            @Override
            public void run() {
                // Start the immersion activity. FLAG_ACTIVITY_NEW_TASK is needed to start an
                // Activity from a Service.
                Intent intent = new Intent(MyoGlassService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };

        public void shutdown() {
            mExecutor.shutdown();
        }

        // onPair() is called whenever a Myo has been paired.
        @Override
        public void onPair(Myo myo, long timestamp) {
            // Store the MAC address of the paired Myo so we can automatically pair with it
            // the next time the app starts.
            mPrefs.edit().putString(PREF_MAC_ADDRESS, myo.getMacAddress()).apply();
        }

        // onArmRecognized() is called whenever Myo has recognized a setup gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmRecognized(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            // Save the arm the Myo is on so that we can use it in the pose events.
            mArm = arm;
        }

        // onArmLost() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmLost(Myo myo, long timestamp) {
            mArm = Arm.UNKNOWN;
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            Log.i(TAG, "pose: " + pose);

            if (!mActivityActive) {
                // Pose a delayed runnable when the THUMB_TO_PINKY pose is detected, and remove it
                // if the pose is released before the delay ends. This allows triggering actions
                // only if a pose is held for a certain time.
                if (pose == Pose.THUMB_TO_PINKY) {
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

                // Dispatch touch pad events for the standard navigation controls based on the
                // current pose.
                switch (pose) {
                    case FIST:
                        sendEvents(MotionEventGenerator.getTapEvents());
                        break;
                    case FINGERS_SPREAD:
                        sendEvents(MotionEventGenerator.getSwipeDownEvents());
                        break;
                    case WAVE_IN:
                        sendEvents(MotionEventGenerator.getSwipeRightEvents());
                        break;
                    case WAVE_OUT:
                        sendEvents(MotionEventGenerator.getSwipeLeftEvents());
                        break;
                }
            }
        }

        // Dispatch a list of events using Instrumentation. Due to Android security restrictions,
        // the events will only be sent to activities in the same application.
        private void sendEvents(final List<MotionEvent> events) {
            if (mExecutor.isShutdown()) {
                Log.w(TAG, "Executor shutdown. Can't send event.");
                return;
            }

            for (final MotionEvent event : events) {
                // Post the event dispatch to a background thread, as sendPointerSync can not be
                // called from the main thread.
                mExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mInstrumentation.sendPointerSync(event);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed sending motion event." , e);
                        }
                    }
                });
            }
        }
    }
}
