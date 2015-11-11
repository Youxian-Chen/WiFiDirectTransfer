package com.example.youxian.wifidirecttransfer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Youxian on 11/11/15.
 */
public class FileManager {
    private static final String TAG = FileManager.class.getName();

    private static FileManager mFileManager;

    private FileManager(){

    }

    public static FileManager getInstance(){
        if (mFileManager == null) {
            mFileManager = new FileManager();
        }
        return mFileManager;
    }

    public List<Music> getLocalMusicList(Context context) {
        List<Music> musics = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            // query failed, handle error.
            Log.w(TAG, "getAllMusics cursor is null");
        } else if (!cursor.moveToFirst()) {
            // no media on the device
            Log.w(TAG, "getAllMusics no media on device");
            cursor.close();
        } else {
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int isMusicColumn = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
            int pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                int isMusic = cursor.getInt(isMusicColumn);
                if (isMusic == 1) {
                    Music music = new Music();
                    music.setId(cursor.getLong(idColumn));
                    music.setTitle(cursor.getString(titleColumn));
                    music.setAlbum(cursor.getString(albumColumn));
                    music.setArtist(cursor.getString(artistColumn));
                    music.setPath(cursor.getString(pathColumn));
                    musics.add(music);
                    // ...process entry...
                    Log.d(TAG, "Music is " + music);

                }

            } while (cursor.moveToNext());

            cursor.close();
        }


        return musics;
    }
}