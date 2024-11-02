package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.advento.lucart.databinding.FragmentHomeBinding;
import com.advento.lucart.databinding.FragmentMyProductsBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Initialize Firestore and RecyclerView
        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = binding.recyclerViewProducts;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize product list and adapter
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(getContext(), productList);
        recyclerView.setAdapter(productAdapter);

        binding.ivBell.setOnClickListener(v -> openNotifications());

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadApprovedProducts();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Load approved products from Firestore
        loadApprovedProducts();

        return binding.getRoot();
    }

    private void openNotifications()  {
        startActivity(new Intent(getActivity(), Notifications.class));
    }

    private void loadApprovedProducts() {
        // Reference to the products collection
        CollectionReference productsRef = db.collection("products");

        // Query to get only approved products
        productsRef.whereEqualTo("status", "approved")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle the error
                        return;
                    }
                    if (value != null) {
                        productList.clear(); // Clear the list to avoid duplicate entries

                        // Loop through each document and convert to Product object
                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            productList.add(product); // Add product to list
                        }
                        productAdapter.notifyDataSetChanged(); // Notify adapter of data change
                    }
                });
    }
}
