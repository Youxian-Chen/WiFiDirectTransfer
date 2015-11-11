package com.example.youxian.wifidirecttransfer;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Youxian on 11/11/15.
 */
public class SelectFileFragment extends Fragment {

    private static final String TAG = SelectFileFragment.class.getName();

    private FileManager mFileManager;
    private LocalMusicAdapter mLocalMusicAdapter = new LocalMusicAdapter();
    private ListView mList;
    private List<Music> mMusics;
    private List<Music> mSelectedMusics;
    private Listener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileManager = FileManager.getInstance();
        mSelectedMusics = new ArrayList<>();
        mMusics = mFileManager.getLocalMusicList(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selectfiles, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View cancelButton = view.findViewById(R.id.cancel_text_fragment);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onCancelKeyClick();
            }
        });
        View okButton = view.findViewById(R.id.submit_text_fragment);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onOkKeyClick();
            }
        });
        mList = (ListView) view.findViewById(R.id.files_list_fragment);
        mList.setDivider(null);
        mList.setAdapter(mLocalMusicAdapter);
    }

    public List<Music> getSelectedMusics(){
        for (Music music: mMusics){
            if (music.getSelected() && !mSelectedMusics.contains(music))
                mSelectedMusics.add(music);
        }
        return mSelectedMusics;
    }


    private class LocalMusicAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMusics.size();
        }

        @Override
        public Object getItem(int position) {
            return mMusics.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mMusics.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Music music = mMusics.get(position);
            if (convertView == null){
                convertView = View.inflate(parent.getContext(), R.layout.file_list_item, null);
                ViewHolder tag = new ViewHolder();
                tag.title = (TextView) convertView.findViewById(R.id.title_master_item);
                tag.selected = (CheckBox) convertView.findViewById(R.id.checkbox_master_item);
                convertView.setTag(tag);
            }
            final ViewHolder tag = (ViewHolder) convertView.getTag();

            if (music != null){
                tag.title.setText(music.getTitle());
                tag.selected.setChecked(music.getSelected());
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tag.selected.performClick();
                        music.setSelected(!music.getSelected());
                    }
                });
            }
            return convertView;
        }
    }

    private static class ViewHolder{
        TextView title;
        CheckBox selected;
    }

    public void setListener(Listener listener){
        mListener = listener;
    }

    public interface Listener{
        void onCancelKeyClick();
        void onOkKeyClick();
    }
}
