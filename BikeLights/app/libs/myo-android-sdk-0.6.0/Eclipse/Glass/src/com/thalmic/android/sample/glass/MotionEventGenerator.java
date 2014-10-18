/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.glass;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates sequences of MotionEvent events to simulate gestures on the Google Glass touch pad.
 */
public class MotionEventGenerator {

    /**
     * Create a sequence of events to simulate a swipe down on a touch pad.
     */
    public static List<MotionEvent> getSwipeDownEvents() {
        return getSwipeEvents(600f, 0f, 600f, 140f);
    }

    /**
     * Create a sequence of events to simulate a swipe to the left on a touch pad.
     */
    public static List<MotionEvent> getSwipeLeftEvents() {
        return getSwipeEvents(580f, 100f, 680f, 100f);
    }

    /**
     * Create a sequence of events to simulate a swipe to the right on a touch pad.
     */
    public static List<MotionEvent> getSwipeRightEvents() {
        return getSwipeEvents(680f, 100f, 580f, 100f);
    }

    /**
     * Create a sequence of events to simulate a tap on a touch pad.
     */
    public static List<MotionEvent> getTapEvents() {
        List<MotionEvent> events = new ArrayList<MotionEvent>();

        long downTime = SystemClock.uptimeMillis();
        events.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, 100f, 100f));
        events.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_UP, 100f, 100f));

        return events;
    }

    /**
     * Create a sequence of events to simulate a swipe on a touch pad.
     *
     * @param startX The x coordinate of the down event in the swipe.
     * @param startY The y coordinate of the down event in the swipe.
     * @param endX The x coordinate of the up event in the swipe.
     * @param endY The y coordinate of the up event in the swipe.
     */
    private static List<MotionEvent> getSwipeEvents(float startX, float startY, float endX, float endY) {
        List<MotionEvent> events = new ArrayList<MotionEvent>();

        long upTime = SystemClock.uptimeMillis();
        long downTime = upTime - 50;
        events.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, startX, startY));

        long moveTime = (downTime + upTime) / 2;
        float moveX = (endX + startX) / 2;
        float moveY = (endY + startY) / 2;
        events.add(getMotionEvent(downTime, moveTime, MotionEvent.ACTION_MOVE, moveX, moveY));

        events.add(getMotionEvent(downTime, upTime, MotionEvent.ACTION_UP, endX, endY));

        return events;
    }

    /**
     * Create a new touch pad MotionEvent with the given basic values.
     *
     * @param downTime The time (in ms) when the user originally pressed down to start
     * a stream of position events. This must be obtained from {@link SystemClock#uptimeMillis()}.
     * @param eventTime The time (in ms) when this specific event was generated.  This
     * must be obtained from {@link SystemClock#uptimeMillis()}.
     * @param action The kind of action being performed, such as {@link android.view.MotionEvent#ACTION_DOWN}.
     * @param x The X coordinate of this event.
     * @param y The Y coordinate of this event.
     * MotionEvent.
     */
    private static MotionEvent getMotionEvent(long downTime, long eventTime, int action, float x, float y) {
        // We need to set the source to not be SOURCE_UNKNOWN, the value used in the simpler
        // MotionEvent.obtain methods, or else a security exception is thrown when submitting the
        // events using Instrumentation. Unfortunately, that means setting the rest of the values
        // of the MotionEvent as well. The values used were read from real Glass touch events.
        int source = InputDevice.SOURCE_TOUCHPAD;

        MotionEvent.PointerProperties property = new MotionEvent.PointerProperties();
        property.id = 0;
        property.toolType = MotionEvent.TOOL_TYPE_FINGER;
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[]{property};

        MotionEvent.PointerCoords coordinate = new MotionEvent.PointerCoords();
        coordinate.pressure = 0.2f;
        coordinate.size = 0.2f;
        coordinate.toolMajor = 3f;
        coordinate.toolMinor = 1f;
        coordinate.touchMajor = 3f;
        coordinate.touchMinor = 3f;
        coordinate.orientation = 0f;
        coordinate.x = x;
        coordinate.y = y;
        MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[]{coordinate};

        int pointerCount = 1;
        int metaState = 0;
        int buttonState = 0;
        float xPrecision = 1f;
        float yPrecision = 1f;
        int deviceId = 1;
        int edgeFlags = 0;
        int flags = 0;

        return MotionEvent.obtain(downTime, eventTime, action, pointerCount, properties, coords,
                metaState, buttonState, xPrecision, yPrecision, deviceId, edgeFlags, source, flags);
    }
}
