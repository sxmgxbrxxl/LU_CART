package com.advento.lucart;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.advento.lucart.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    private FragmentHomeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        binding.btnSell.setOnClickListener( v-> addToHome());

        return binding.getRoot();
    }

        private void addToHome() {
        // Get the container layout where custom views will be added
        LinearLayout containerLayout = binding.containerLayout; // Assume this is defined in FragmentSellBinding

        // Inflate the custom layout and add it dynamically
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.product_tile, containerLayout, false);

        // Set fixed size of 600dp for the custom view
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(700, 480);
        layoutParams.setMargins(0,0,0,0);  // Set margins (left, top, right, bottom)
        customView.setLayoutParams(layoutParams);

        // Center the custom view in the container layout
        containerLayout.setGravity(Gravity.CENTER);

        // Add the custom view to the container layout
        containerLayout.addView(customView);
    }
}