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
    private String[] divsTable = {"2", "5", "10", "20", "50", "100", "200", "500", "1000", "2000", "5000", "10000", "20000", "50000", "100000", "200000", "500000"};
    private int index = 0;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("index", index);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (savedInstanceState != null) {
            savedInstanceState.getInt("index", index);
        }
        View view = inflater.inflate(R.layout.fragment_timediv, container, false);
        text = view.findViewById(R.id.timeDivText);
        text.setText(divsTable[index] + "ms/div");
        plusButton = view.findViewById(R.id.boutonPlus);
        minusButton = view.findViewById(R.id.boutonMoins);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("moins");
            }
        });


        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("plus");
            }
        });

        return view;
    }


}
