package com.example.youxian.wifidirecttransfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by Youxian on 11/11/15.
 */
public class WiFiDirectReceiver extends BroadcastReceiver {
    public static final String TAG = WiFiDirectReceiver.class.getName();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;

    public WiFiDirectReceiver(){
        super();
    }

    public WiFiDirectReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                              MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "P2P PEER CHANGED");
            if (mManager != null) {
                mManager.requestPeers(mChannel, mActivity.getTransferFragment());
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "P2P CONNECTION CHANGED");
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                mActivity.stopDiscover();
                Log.d("Receiver",
                        "Connected to p2p network. Requesting network details");
                mManager.requestConnectionInfo(mChannel, mActivity);
            } else {

            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "this device change");
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mActivity.getReceiveFragment().setDevice(device);
        }
    }
}