package com.example.youxian.wifidirecttransfer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener{

    private static final String TAG = MainActivity.class.getName();
    private ProgressDialog mProgressDialog;
    private IntentFilter mIntentFilter = new IntentFilter();
    private BroadcastReceiver mReceiver;


    private MainFragment mMainFragment;
    private SelectFileFragment  mSelectFileFragment;
    private ReceiveFragment mReceiveFragment;
    private TransferFragment mTransferFragment;

    private List<Music> mMusic;
    private TransferFilesTask mTransferFilesTask;

    private static final int File_Chooser = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mReceiver = new MainReceiver(this);
        mIntentFilter.addAction(MainReceiver.ACTION_TRANSFER_DONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == File_Chooser) {
            if (resultCode == RESULT_OK) {
                if (data.getAction().equals(Intent.ACTION_SEND_MULTIPLE)) {

                }
            }
            Log.d(TAG, "onActivityResult");
        }
    }

    private void initView(){
        replaceFragment(getMainFragment(), false);
    }

    private void replaceFragment(Fragment fragment, boolean addBackStack){
        Log.d(TAG, "replaceFragment: " + fragment);
        FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
        transaction.replace(R.id.container_layout_main, fragment, "transaction");
        if (addBackStack)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    private MainFragment getMainFragment(){
        if (mMainFragment == null) {
            mMainFragment = new MainFragment();
            mMainFragment.setListener(new MainFragment.Listener() {
                @Override
                public void onTransferClick() {
                    //replaceFragment(getSelectFileFragment(), true);
                    Intent fileChooserIntent = new Intent(MainActivity.this,
                            com.example.youxian.filechooser.MainActivity.class);
                    startActivityForResult(fileChooserIntent, File_Chooser);
                }

                @Override
                public void onReceiveClick() {
                    replaceFragment(getReceiveFragment(), true);
                }
            });
        }
        return mMainFragment;
    }

    public SelectFileFragment getSelectFileFragment(){
        if (mSelectFileFragment == null) {
            mSelectFileFragment = new SelectFileFragment();
            mSelectFileFragment.setListener(new SelectFileFragment.Listener() {
                @Override
                public void onCancelKeyClick() {
                    getFragmentManager().popBackStackImmediate();
                }

                @Override
                public void onOkKeyClick() {
                    mMusic = mSelectFileFragment.getSelectedMusics();
                    replaceFragment(getTransferFragment(), true);
                    mTransferFragment.setFiles(mMusic);
                }
            });
        }
        return mSelectFileFragment;
    }

    public TransferFragment getTransferFragment(){
        if (mTransferFragment == null) {
            mTransferFragment = new TransferFragment();
        }
        return mTransferFragment;
    }

    public ReceiveFragment getReceiveFragment(){
        if (mReceiveFragment == null) {
            mReceiveFragment = new ReceiveFragment();
        }
        return mReceiveFragment;
    }

    public void stopDiscover(){
        getReceiveFragment().stopDiscover();
        getTransferFragment().stopDiscover();
    }

    public void transferDone(){
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        getReceiveFragment().disconnect();
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.d(TAG, "Connect success");
        Log.d(TAG, info.groupOwnerAddress.getHostAddress());
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Transfer files...");
        mProgressDialog.setCancelable(false);
        if (info.isGroupOwner) {
            Intent receiveIntent = new Intent(this, ServerService.class);
            receiveIntent.setAction(ServerService.ACTION_RECEIVE);
            this.startService(receiveIntent);
        } else {
            if (mTransferFilesTask == null) {
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setProgress(0);
                new TransferFilesTask(mSelectFileFragment.getSelectedMusics(),
                        info.groupOwnerAddress.getHostAddress(), "1234").execute("");
            } else mTransferFilesTask = null;
        }
        mProgressDialog.show();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        if (mTransferFragment != null)
            mTransferFragment.updateList(peers);
    }

    private class TransferFilesTask extends AsyncTask<String, Integer, Long> {
        private String mAddress;
        private String mPort;
        private List<Music> mMusics = new ArrayList<>();
        private static final int SOCKET_TIMEOUT = 5000;
        public TransferFilesTask(List<Music> musics, String address, String port){
            mMusics = musics;
            mAddress = address;
            mPort = port;
        }
        @Override
        protected Long doInBackground(String... params) {
            while (!isCancelled()) {
                Socket socket = new Socket();

                try {
                    Log.d(TAG, "Opening client socket - ");
                    socket.bind(null);
                    Log.d(TAG, mAddress + " port: " + mPort);
                    socket.connect((new InetSocketAddress(mAddress, Integer.parseInt(mPort))), SOCKET_TIMEOUT);

                    Log.d(TAG, "Client socket - " + socket.isConnected());

                    BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                    DataOutputStream dos = new DataOutputStream(bos);

                    dos.writeInt(mMusics.size());
                    Log.d(TAG, mMusics.size()+"");
                    int added = 100 / mMusics.size() + 1;
                    int status = 0;
                    for (Music music : mMusics){
                        File file = new File(music.getPath());
                        Log.d(TAG, music.getPath());
                        long length = file.length();
                        dos.writeLong(length);
                        String name = file.getName();
                        dos.writeUTF(name);
                        FileInputStream fis = new FileInputStream(file);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        int theByte;
                        for (long i = 0; i < length; i++){
                            theByte = bis.read();
                            bos.write(theByte);
                        }
                        /*
                        while((theByte = bis.read()) != -1){
                            bos.write(theByte);
                        }*/
                        bis.close();
                        status = status + added;
                        mProgressDialog.setProgress(status);
                        Log.d(TAG, "Client: Data written");
                    }
                    dos.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                Log.d(TAG, "Client: stop service");
                mProgressDialog.dismiss();
                this.cancel(true);
            }
            return null;
        }

    }
}
