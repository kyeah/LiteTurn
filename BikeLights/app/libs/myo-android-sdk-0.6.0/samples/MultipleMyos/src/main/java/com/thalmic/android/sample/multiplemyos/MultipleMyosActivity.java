/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.multiplemyos;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;

import java.util.ArrayList;

// This sample illustrates how to pair with multiple Myo devices and distinguish between them.

public class MultipleMyosActivity extends Activity {
    private static final String TAG = "MultipleMyosActivity";

    // We store each Myo object that we pair with in this list, so that we can keep track of the order we've seen
    // each Myo and give it a unique short identifier (see onPair() and identifyMyo() below).
    private ArrayList<Myo> mKnownMyos = new ArrayList<Myo>();

    private MyoAdapter mAdapter;

    private DeviceListener mListener = new AbstractDeviceListener() {

        // Every time the SDK successfully pairs with a Myo armband, this function will be called.
        //
        // You can rely on the following rules:
        //  - onPair() will only be called once for each Myo device
        //  - no other events will occur involving a given Myo device before onPair() is called with it
        //
        // If you need to do some kind of per-Myo preparation before handling events, you can safely do it in onPair().
        @Override
        public void onPair(Myo myo, long timestamp) {

            // The object for a Myo is unique - in other words, it's safe to compare two Myo references to
            // see if they're referring to the same Myo.

            // Add the Myo object to our list of known Myo devices. This list is used to implement identifyMyo() below so
            // that we can give each Myo a nice short identifier.
            mKnownMyos.add(myo);

            // Now that we've added it to our list, get our short ID for it and print it out.
            Log.i(TAG, "Paired with " + myo.getMacAddress() + ", now known as Myo " + identifyMyo(myo) + ".");
        }

        @Override
        public void onConnect(Myo myo, long timestamp) {
            mAdapter.setMessage(myo, "Myo " + identifyMyo(myo) + " has connected.");
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            mAdapter.setMessage(myo, "Myo " + identifyMyo(myo) + " has disconnected.");
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            mAdapter.setMessage(myo, "Myo " + identifyMyo(myo) + " switched to pose " + pose.toString() + ".");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_myos);

        // First, we initialize the Hub singleton.
        Hub hub = Hub.getInstance();
        if (!hub.init(this)) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final int pairingCount = 2;

        Log.i(TAG, "Pairing with " + pairingCount + " Myo armbands.");

        // pairWithAdjacentMyos() pairs with Myo devices that are physically very near to the Bluetooth radio
        // until it has paired with the provided count.
        // DeviceListeners attached to the hub will receive onPair() events once pairing has completed.
        hub.pairWithAdjacentMyos(pairingCount);

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);

        // Attach an adapter to the ListView for showing the state of each Myo.
        mAdapter = new MyoAdapter(this, pairingCount);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will unpair all paired Myo devices,
            // and disconnect any that are connected.
            Hub.getInstance().shutdown();
        }
    }

    // This is a utility function implemented for this sample that maps a Myo to a unique ID starting at 1.
    // It does so by looking for the Myo object in mKnownMyos, which onPair() adds each Myo into as it is paired.
    private int identifyMyo(Myo myo) {
        return mKnownMyos.indexOf(myo) + 1;
    }

    private class MyoAdapter extends ArrayAdapter<String> {

        public MyoAdapter(Context context, int count) {
            super(context, android.R.layout.simple_list_item_1);

            // Initialize adapter with items for each expected Myo.
            for (int i = 0; i < count; i++) {
                add(getString(R.string.waiting_message));
            }
        }

        public void setMessage(Myo myo, String message) {
            // identifyMyo returns IDs starting at 1, but the adapter indices start at 0.
            int index = identifyMyo(myo) - 1;

            // Replace the message.
            remove(getItem(index));
            insert(message, index);
        }
    }
}
