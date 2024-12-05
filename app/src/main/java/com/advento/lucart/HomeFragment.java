package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.advento.lucart.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ShopAdapter shopAdapter;
    private List<Shop> shopList;
    private FirebaseFirestore db;
    private List<Product> allProducts;
    private androidx.appcompat.widget.SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        db = FirebaseFirestore.getInstance();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerViewProducts.setLayoutManager(gridLayoutManager);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvShops.setLayoutManager(layoutManager);

        shopList = new ArrayList<>();
        productList = new ArrayList<>();
        allProducts = new ArrayList<>();

        productAdapter = new ProductAdapter(getContext(), productList, product -> {
            Intent intent = new Intent(getContext(), ProductOverview.class);
            intent.putExtra("productId", product.getProductId());
            intent.putExtra("productName", product.getProductName());
            intent.putExtra("productPrice", product.getProductPrice());
            intent.putExtra("productDescription", product.getProductDescription());
            intent.putExtra("productCategory", product.getProductCategory());
            intent.putExtra("productImage", product.getProductImage());
            startActivity(intent);
        });
        binding.recyclerViewProducts.setAdapter(productAdapter);

        shopAdapter = new ShopAdapter(getContext(), shopList, shop -> {
            Intent intent = new Intent(getContext(), ShopOverview.class);
            intent.putExtra("businessId", shop.getBusinessId());
            intent.putExtra("businessName", shop.getBusinessName());
            intent.putExtra("businessImage", shop.getPhotoUrl());
            startActivity(intent);
        });
        binding.rvShops.setAdapter(shopAdapter);

        loadGreetings();
        loadShops();
        loadApprovedProducts();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            loadApprovedProducts();
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        binding.ivSearch.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Search.class));
        });

        binding.btnFood.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Foods.class));
        });

        binding.btnClothes.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Clothes.class));
        });

        binding.btnElectronics.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Electronics.class));
        });

        binding.btnAccessories.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Accessories.class));
        });

        binding.btnOthers.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Others.class));
        });

        binding.tvSeeAll.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Search.class));
        });

        binding.ivBell.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Notifications.class));
        });

        return binding.getRoot();
    }

    private void loadGreetings() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String firstName = task.getResult().getString("firstName");
                            String greetingMessage = (firstName != null && !firstName.isEmpty()) ?
                                    "Hello, " + firstName + "!" :
                                    "Hello, User!";

                            if (isAdded()) {
                                applyTypewriterEffect(greetingMessage, binding.tvGreetings, () -> loadQuestion());
                            }
                        } else {
                            if (isAdded()) {
                                applyTypewriterEffect("Hello, User!", binding.tvGreetings, () -> loadQuestion());
                            }
                        }
                    });
        } else {
            if (isAdded()) {
                applyTypewriterEffect("Hello, User!", binding.tvGreetings, () -> loadQuestion());
            }
        }
    }

    private Handler handler = new Handler();
    private Runnable typeWriterRunnable;

    private void applyTypewriterEffect(String message, TextView textView, Runnable onComplete) {
        final String finalMessage = message;
        final int[] index = {0};

        handler.removeCallbacks(typeWriterRunnable);

        typeWriterRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < finalMessage.length()) {
                    if (isAdded()) {
                        textView.append(String.valueOf(finalMessage.charAt(index[0])));
                        index[0]++;
                    }
                    handler.postDelayed(this, 70);
                } else {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        };

        handler.post(typeWriterRunnable);
    }

    private void loadQuestion() {
        String question = "What do you want today?";
        if (isAdded()) {
            applyTypewriterEffect(question, binding.tvSubtitle, null);
        }
    }

    private void loadShops() {
        CollectionReference shopsRef = db.collection("business");

        shopsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                shopList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Shop shop = doc.toObject(Shop.class);
                    shopList.add(shop);
                }
                shopAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadApprovedProducts() {
        CollectionReference productsRef = db.collection("products");

        productsRef.whereEqualTo("status", "approved")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        productList.clear();
                        allProducts.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            productList.add(product);
                            allProducts.add(product);
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                });
    }
}
