package com.example.youxian.wifidirecttransfer;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Youxian on 11/11/15.
 */
public class ReceiveFragment extends Fragment {

    private static final String TAG = ReceiveFragment.class.getName();

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private IntentFilter mIntentFilter;
    private BroadcastReceiver mReceiver;

    private Listener mListener;
    private TextView mDeviceName;
    private WifiP2pDevice mDevice;
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
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mWifiP2pManager = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(getActivity(), getActivity().getMainLooper(), null);
        mReceiver = new WiFiDirectReceiver(mWifiP2pManager, mChannel, (MainActivity)getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receive, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDeviceName = (TextView) view.findViewById(R.id.device_text_receive);
    }

    public void setDevice(WifiP2pDevice device){
        mDevice = device;
        mDeviceName.setText(mDevice.deviceName);
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

    public void disconnect(){
        mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "disconnect success");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "disconnect failed");
            }
        });
    }

    public void setListener(Listener listener){
        mListener = listener;
    }

    public interface Listener{
        void onRegisterClick();
    }


}