package com.example.tpeea.projeteb03;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TimeDivFragment extends Fragment {

    private Button plusButton;
    private Button minusButton;
    private TextView text;
    private MainActivity activity;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timediv, container, false);
        text=view.findViewById(R.id.timeDivText);
        minusButton=view.findViewById(R.id.boutonMoins);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //faire les bails
            }
        });

        return view;
    }



}
