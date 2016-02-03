/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xeonqi.com.ece150;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.xeonqi.com.ece150.DeviceListFragment.DeviceActionListener;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends Activity implements ChannelListener, DeviceActionListener, SensorEventListener {

    public static final String TAG = "HW3-Wifi-direct";
    private WifiP2pManager mManager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel mChannel;
    private BroadcastReceiver mReceiver = null;

//    private SensorManager sensorManager;
    static final int SHAKE_THRESHOLD = 18;
    static int conn_flag = 0;

    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    /* get Sensor Events */
        final DeviceListFragment listFragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
//        final DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

    /* get P2P Events */

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
//        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                // Code for when the discovery initiation is successful goes here.
//                // No services have actually been discovered yet, so this method
//                // can often be left blank.  Code for peer discovery goes in the
//                // onReceive method, detailed below.
//                Toast toast = Toast.makeText(getApplicationContext(), "Discover devices successfully.", Toast.LENGTH_LONG);
//                toast.show();
//            }
//            @Override
//            public void onFailure(int reasonCode) {
//                // Code for when the discovery initiation fails goes here.
//                // Alert the user that something went wrong.
//                Toast toast = Toast.makeText(getApplicationContext(), "Failed to discover devices.", Toast.LENGTH_LONG);
//                toast.show();
//            }
//        });

    }

    @Override
    public void onSensorChanged(SensorEvent se) {
//        float x = se.values[0];
//        float y = se.values[1];
//        float z = se.values[2];
//        mAccelLast = mAccelCurrent;
//        mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
//        float delta = mAccelCurrent - mAccelLast;
//        mAccel = mAccel * 0.9f + delta; // perform low-cut filter
//
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void connectChosen(){
        if (!isWifiP2pEnabled)  Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
        else {
            final DeviceListFragment listFragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
            listFragment.onInitiateDiscovery();
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (mAccel > 12) {
                Toast toast = Toast.makeText(getApplicationContext(), "Device has shaken.", Toast.LENGTH_LONG);
                toast.show();
                connectChosen();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        final DeviceListFragment listFragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
        final DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    /** register the BroadcastReceiver with the intent values to be matched */
        registerReceiver(mReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        /** register the BroadcastReceiver with the intent values to be matched */
        mSensorManager.unregisterListener(mSensorListener);
    }
    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        final DeviceListFragment listFragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
        final DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        if (listFragment != null) {
            listFragment.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        final DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        fragmentDetails.showDetails(device);

    }

    @Override
    public void connect(WifiP2pConfig config) {
		
       // TODO: this part may need some coding
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDirectActivity.this, "Connection succeeded.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connection failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
	   // TODO: this part may need some coding
        final DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        fragmentDetails.resetViews();
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(WiFiDirectActivity.this, "Disconnection failed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                fragmentDetails.getView().setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
	   // TODO: this part may need some coding
        if (mManager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost.", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            mManager.initialize(this, getMainLooper(), this);
        } else Toast.makeText(this, "Channel lost permanently, probably. Try Disable/Re-Enable P2P.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void cancelDisconnect() {

        // TODO: this part may need some coding
        if (mManager != null) {
            final DeviceListFragment listFragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
            if (listFragment.getDevice() == null
                    || listFragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (listFragment.getDevice().status == WifiP2pDevice.AVAILABLE || listFragment.getDevice().status == WifiP2pDevice.INVITED) {
                mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(WiFiDirectActivity.this, "Connection cancelled", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(WiFiDirectActivity.this, "Connection cancelling failed. Error Code: " + reasonCode, Toast.LENGTH_SHORT).show();
                }
                });
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (mManager != null && mChannel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.

                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
