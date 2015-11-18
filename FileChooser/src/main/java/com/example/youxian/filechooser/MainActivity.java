package com.example.youxian.filechooser;

import android.app.Activity;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    private TextView mPathText;
    private Button mCancelButton;
    private Button mOkButton;
    private ListView mList;

    private FileListAdapter mFileListAdapter;
    private List<File> mFiles;
    private File currentFile;
    private File rootFile;

    private List<File> mSelectFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_chooser_main);
        mFiles = new ArrayList<>();
        mSelectFiles = new ArrayList<>();
        initView();
    }

    @Override
    public void onBackPressed() {
        if (currentFile.equals(rootFile)) {
            this.finish();
        } else {
            showDirectory(currentFile.getParentFile());
        }
    }

    private void initView() {
        mPathText = (TextView) findViewById(R.id.path_main);

        mCancelButton = (Button) findViewById(R.id.cancel_button_main);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mOkButton = (Button) findViewById(R.id.ok_button_main);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "select files: " + getSelectedFiles().size());
            }
        });

        scanFile();
        mList = (ListView) findViewById(R.id.files_list_main);
        mFileListAdapter = new FileListAdapter(mFiles);
        mList.setAdapter(mFileListAdapter);
    }

    private void scanFile() {
        rootFile = Environment.getExternalStorageDirectory();
        File[] files = rootFile.listFiles();
        for (File file: files) {
            mFiles.add(file);
        }
        currentFile = rootFile;
        showPath(rootFile.getPath());
    }

    private void showPath(String path) {
        mPathText.setText(path);
    }

    private void showDirectory(File file) {
        mFiles.clear();
        Collections.addAll(mFiles, file.listFiles());
        for (File f : mFiles) {
            Log.d(TAG, "file: " + f.getName());
        }
        mFileListAdapter.notifyDataSetChanged();
        currentFile = file;
        showPath(currentFile.getPath());
    }

    private boolean checkSelectedFiles(File file) {
        if (mSelectFiles.contains(file))
            return true;
        return false;
    }

    public List<File> getSelectedFiles() {
        return mSelectFiles;
    }

    private class FileListAdapter extends BaseAdapter {

        private List<File> mListFiles;
        private LayoutInflater mInflater;
        public FileListAdapter(List<File> files) {
            mListFiles = files;
            mInflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mListFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return mListFiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final File file = mListFiles.get(position);
            Log.d(TAG, "file: " + file.getName() + " is folder: " + file.isDirectory());
            if (!file.isDirectory()) {
                convertView = mInflater.inflate(R.layout.listrow_file, null);
                TextView titleFile = (TextView) convertView.findViewById(R.id.title_file_item);
                final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_file_item);
                if (checkSelectedFiles(file))
                    checkBox.setChecked(true);
                titleFile.setText(file.getName());
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkBox.performClick();
                        if (!mSelectFiles.contains(file)) {
                            mSelectFiles.add(file);
                        } else mSelectFiles.remove(file);
                    }
                });
            } else {
                convertView = mInflater.inflate(R.layout.listrow_folder, null);
                TextView titleFolder = (TextView) convertView.findViewById(R.id.title_folder_item);
                titleFolder.setText(file.getName());
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick");
                        showDirectory(file);
                    }
                });
            }

            return convertView;
        }
    }
}
