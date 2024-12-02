package com.advento.lucart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.advento.lucart.databinding.FragmentTransactionsBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        setupInsets(binding.getRoot());
        setupViewPagerAndTabs();

        // Initialize FAB and set its click listener
        binding.fabMoveToReceive.setOnClickListener(v -> moveToReceive());

        return binding.getRoot();
    }

    private void setupInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupViewPagerAndTabs() {
        List<Fragment> fragments = new ArrayList<>();

        String userType = "business"; // Replace this with dynamic logic to determine user type

        fragments.add(fragment_to_ship.newInstance()); // No-argument version
        fragments.add(fragment_to_receive.newInstance()); // Adjust parameters if needed
        fragments.add(fragment_completed.newInstance()); // Adjust parameters if needed


        PagerAdapter adapter = new PagerAdapter(requireActivity(), fragments);
        binding.viewPagerSeller.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayoutSeller, binding.viewPagerSeller, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("To Ship");
                    break;
                case 1:
                    tab.setText("To Receive");
                    break;
                case 2:
                    tab.setText("Completed");
                    break;
            }
        }).attach();
    }


    private void moveToReceive() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = "sellerId"; // Replace with actual logic for fetching seller ID

        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Seller ID is missing.", Toast.LENGTH_SHORT).show();
            return; // Exit early if seller ID is null
        }

        db.collection("transactions")
                .whereEqualTo("status", "To Ship")
                .whereEqualTo("sellerId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            if (snapshot.exists() && snapshot.get("status") != null) {
                                snapshot.getReference().update("status", "To Receive")
                                        .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Orders moved to 'To Receive'.", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "No orders to move.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error fetching orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
