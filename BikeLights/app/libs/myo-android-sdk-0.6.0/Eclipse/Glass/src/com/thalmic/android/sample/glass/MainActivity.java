/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.glass;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private MyoGlassService mService;
    private ExampleCardAdapter mCardAdapter;
    private CardScrollView mCardScroller;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyoGlassService.MBinder binder = ((MyoGlassService.MBinder)service);
            mService = binder.getService();

            // Let the service know that the activity is showing. Used by the service to trigger
            // the appropriate foreground or background events.
            mService.setActivityActive(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mCardAdapter = new ExampleCardAdapter(this);

        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mCardAdapter);
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openOptionsMenu();
            }
        });

        setContentView(mCardScroller);

        // Bind to the ConnectionService so that we can communicate with it directly.
        Intent intent = new Intent(this, MyoGlassService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        // Start the ConnectionService normally so it outlives the activity. This allows it to
        // listen for Myo pose events when the activity isn't running.
        startService(new Intent(this, MyoGlassService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mService != null) {
            mService.setActivityActive(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mService != null) {
            mService.setActivityActive(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_footnotes:
                mCardAdapter.toggleFootnotes();
                return true;
            case R.id.pair_menu_item:
                if (mService != null) {
                    mService.pairWithNewMyo();
                }
                return true;
            case R.id.stop_menu_item:
                stopService(new Intent(this, MyoGlassService.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class ExampleCardAdapter extends CardScrollAdapter {
        private Context mContext;
        private List<Card> mCards = new ArrayList<Card>();
        private boolean mShowingFootnotes = true;

        public ExampleCardAdapter(Context context) {
            mContext = context;
            createCards();
        }

        @Override
        public int getPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public int getViewTypeCount() {
            return Card.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position){
            return mCards.get(position).getItemViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).getView(convertView, parent);
        }

        public void toggleFootnotes() {
            mShowingFootnotes = !mShowingFootnotes;
            mCards.clear();
            createCards();
            notifyDataSetChanged();
        }

        private void createCards() {
            Card card = new Card(mContext);
            card.setText("One");
            if (mShowingFootnotes) {
                card.setFootnote("Wave right");
            }
            mCards.add(card);

            card = new Card(mContext);
            card.setText("Two");
            if (mShowingFootnotes) {
                card.setFootnote("Wave left/right");
            }
            mCards.add(card);

            card = new Card(mContext);
            card.setText("Three");
            if (mShowingFootnotes) {
                card.setFootnote("Wave left");
            }
            mCards.add(card);
        }
    }
}
