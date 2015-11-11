package com.example.youxian.wifidirecttransfer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Youxian on 11/11/15.
 */
public class ServerService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    private static final String TAG = ServerService.class.getName();
    public static final String ACTION_SYNC = "action_sync";
    public ServerService(String name) {
        super(name);
    }

    public ServerService(){
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (ACTION_SYNC.equals(action)){
            Log.d(TAG, action);
            String dirPath = android.os.Environment.getExternalStorageDirectory() + "/Music";
            File dir = new File(dirPath);
            dir.mkdirs();
            try {

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                Log.d(TAG, "server create");
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(1234));
                Socket client = serverSocket.accept();
                Log.d(TAG, "client accept");

                BufferedInputStream bis = new BufferedInputStream(client.getInputStream());
                DataInputStream dis = new DataInputStream(bis);

                int filesCount = dis.readInt();
                File[] files = new File[filesCount];
                for (int i = 0; i < filesCount; i++){
                    long fileLength = dis.readLong();
                    String fileName = dis.readUTF();

                    files[i] = new File(dirPath +"/"+ fileName);

                    FileOutputStream fos = new FileOutputStream(files[i]);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    for(int j = 0; j < fileLength; j++){
                        bos.write(bis.read());
                    }
                    Log.d(TAG, "get file" + fileName);
                    bos.close();
                    new MediaScannerWrapper(getApplicationContext(), dirPath + "/" + fileName, "music/*").scan();
                }
                dis.close();
                serverSocket.close();
                Log.d(TAG, "saved file and close server");
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());

            }
            stopSelf();
        }
    }

    private class MediaScannerWrapper implements MediaScannerConnection.MediaScannerConnectionClient{
        private MediaScannerConnection mConnection;
        private String mPath;
        private String mMimeType;
        public MediaScannerWrapper(Context context, String filePath, String mime){
            mPath = filePath;
            mMimeType = mime;
            mConnection = new MediaScannerConnection(context, this);
        }

        public void scan(){
            mConnection.connect();
        }
        @Override
        public void onMediaScannerConnected() {
            mConnection.scanFile(mPath, mMimeType);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {

        }
    }
}
