package com.example.youxian.wifidirecttransfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Youxian on 11/12/15.
 */
public class MainReceiver extends BroadcastReceiver {

    private static final String TAG = MainReceiver.class.getName();
    public static final String ACTION_TRANSFER_DONE = "action_transfer_done";
    private MainActivity mActivity;
    public MainReceiver(){
        super();
    }

    public MainReceiver(MainActivity activity){
        super();
        mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ACTION_TRANSFER_DONE)) {
            Log.d(TAG, "transfer done");
            mActivity.transferDone();
        }
    }
}
