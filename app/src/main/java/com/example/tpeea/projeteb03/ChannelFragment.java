package com.example.tpeea.projeteb03;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

//TODO pouvoir faire un fragment par channel

public class ChannelFragment extends Fragment {

    private Switch chSwitch;
    private MainActivity activity;


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("switchState",chSwitch.isChecked());
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(savedInstanceState!=null){

        }

        View view = inflater.inflate(R.layout.ch_fragment, container, false);
        chSwitch=view.findViewById(R.id.chSwitch);
        chSwitch.setChecked(false);
        chSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(activity.getmBluetoothManager().getBluetoothState()==activity.getmBluetoothManager().STATE_CONNECTED) {
                    if (isChecked){
                        activity.getmBluetoothManager().write(activity.getmFrameProcessor().toFrame(activity.getmOscilloManager().setChannel(1, true)));
                        Toast.makeText(activity,"ch1 ouvert",Toast.LENGTH_SHORT).show();
                    } else {
                        activity.getmBluetoothManager().write(activity.getmFrameProcessor().toFrame(activity.getmOscilloManager().setChannel(1, false)));
                        Toast.makeText(activity,"ch1 ferme",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        return view;


    }

}
