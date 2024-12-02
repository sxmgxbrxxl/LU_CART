package com.advento.lucart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class fragment_to_receive extends Fragment {

    public fragment_to_receive() {
        // Required empty public constructor
    }

    public static fragment_to_receive newInstance() {
        return new fragment_to_receive();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_to_receive, container, false);
    }
}
