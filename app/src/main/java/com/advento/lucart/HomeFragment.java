package com.advento.lucart;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.advento.lucart.databinding.FragmentHomeBinding;
import com.advento.lucart.databinding.FragmentProfileBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentHomeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1000, 600);
        layoutParams.setMargins(10, 2,10,2);  // Set margins (left, top, right, bottom)
        customView.setLayoutParams(layoutParams);

        // Center the custom view in the container layout
        containerLayout.setGravity(Gravity.CENTER);

        // Add the custom view to the container layout
        containerLayout.addView(customView);
    }
}