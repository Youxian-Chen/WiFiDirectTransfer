package com.example.youxian.wifidirecttransfer;

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Youxian on 11/11/15.
 */
public class TransferFragment extends ListFragment {

    private static final String TAG = TransferFragment.class.getName();

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private IntentFilter mIntentFilter;
    private BroadcastReceiver mReceiver;
    private List<WifiP2pDevice> mPeerList = new ArrayList<>();

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                startDiscoverPeers();
                mHandler.postDelayed(this, 8000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWifiP2p();
        getActivity().registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(mRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }

    private void initWifiP2p(){
        Log.d(TAG, "initWifiP2p");
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mWifiP2pManager = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(getActivity(), getActivity().getMainLooper(), null);
        mReceiver = new WiFiDirectReceiver(mWifiP2pManager, mChannel, (MainActivity)getActivity());
        deletePersistentGroups();
    }

    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mWifiP2pManager, mChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transfer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.setListAdapter(new WiFiP2pDeviceAdapter(this.getActivity(),
                android.R.layout.simple_list_item_2, android.R.id.text1,
                mPeerList));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        connectP2p((WifiP2pDevice) l.getItemAtPosition(position));
    }

    private void startDiscoverPeers() {
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "discover success");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "discover failure");
            }
        });
    }

    public void stopDiscover(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void connectP2p(WifiP2pDevice device){
        WifiP2pConfig config = new WifiP2pConfig();
        config.groupOwnerIntent = 0;
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Succeed connecting");
            }

            @Override
            public void onFailure(int errorCode) {
                Log.d(TAG, "Failed connecting");
            }
        });
    }

    public void updateList(WifiP2pDeviceList peers){
        mPeerList.clear();
        mPeerList.addAll(peers.getDeviceList());
        ((WiFiP2pDeviceAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private class WiFiP2pDeviceAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> mDeviceList;
        public WiFiP2pDeviceAdapter(Context context, int resource,
                                    int textViewResourceId, List<WifiP2pDevice> items) {
            super(context, resource, textViewResourceId, items);
            mDeviceList =items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_2, null);
            }
            WifiP2pDevice device = mDeviceList.get(position);
            if (device != null) {
                TextView nameText = (TextView) v.findViewById(android.R.id.text1);
                if (nameText != null) {
                    nameText.setText(device.deviceName);
                }
                TextView statusText = (TextView) v.findViewById(android.R.id.text2);
                statusText.setText(getDeviceStatus(device.status));
            }
            return v;
        }
    }
    public static String getDeviceStatus(int statusCode) {
        switch (statusCode) {
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

}