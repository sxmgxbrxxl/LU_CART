package com.advento.lucart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class fragment_completed extends Fragment {

    public fragment_completed() {
        // Required empty public constructor
    }

    public static fragment_completed newInstance() {
        return new fragment_completed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_completed, container, false);
    }
}
