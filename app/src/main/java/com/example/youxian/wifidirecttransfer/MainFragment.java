package com.example.youxian.wifidirecttransfer;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Youxian on 11/11/15.
 */
public class MainFragment extends Fragment {

    private Listener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button transButton = (Button) view.findViewById(R.id.transfer_button_main);
        transButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onTransferClick();
            }
        });
        Button receiveButton = (Button) view.findViewById(R.id.receive_button_main);
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onReceiveClick();
            }
        });
    }

    public void setListener(Listener listener){
        mListener = listener;
    }

    public interface Listener{
        void onTransferClick();
        void onReceiveClick();
    }
}
