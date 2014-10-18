/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.background;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TriggerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start the BackgroundService to receive and handle Myo events.
        startService(new Intent(this, BackgroundService.class));

        // Close this activity since BackgroundService will run in the background.
        finish();
    }
}
