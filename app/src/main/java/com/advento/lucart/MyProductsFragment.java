package com.advento.lucart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.advento.lucart.databinding.FragmentMyProductsBinding;

public class MyProductsFragment extends Fragment {

    private FragmentMyProductsBinding binding;

    public MyProductsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentMyProductsBinding.inflate(inflater, container, false);
//
//        // Set up UI actions and event listeners
//        binding.btnSell.setOnClickListener(v -> addToHome());

        return binding.getRoot();
    }

//    private void addToHome() {
//        // Get the container layout where custom views will be added
//        LinearLayout containerLayout = binding.containerLayout; // Assume this is defined in FragmentSellBinding
//
//        // Inflate the custom layout and add it dynamically
//        View customView = LayoutInflater.from(getContext()).inflate(R.layout.product_tile, containerLayout, false);
//
//        // Set fixed size of 600dp for the custom view
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(600, 600);
//        layoutParams.setMargins(20, 20, 20, 20);  // Set margins (left, top, right, bottom)
//        customView.setLayoutParams(layoutParams);
//
//        // Center the custom view in the container layout
//        containerLayout.setGravity(Gravity.CENTER);
//
//        // Add the custom view to the container layout
//        containerLayout.addView(customView);
//    }
}
