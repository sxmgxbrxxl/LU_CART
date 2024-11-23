package com.advento.lucart;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

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
        RecyclerView recyclerView = binding.recyclerViewProducts;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

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
            intent.putExtra("productCategory", product.getCategory());
            intent.putExtra("productImage", product.getProductImage());
            startActivity(intent);
        });
        recyclerView.setAdapter(productAdapter);

        binding.rvShops.setAdapter(shopAdapter);

        loadGreetings();
//        loadShops(); DI KO PA MAAYOS YUNG ADAPTER
        loadApprovedProducts();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            loadApprovedProducts();
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        binding.ivSearch.setOnClickListener(v -> {

        });

        binding.ivBell.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), Notifications.class);
            startActivity(intent);
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
    
    private void showDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_filter);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }

        CheckBox chkFoods = dialog.findViewById(R.id.chkFoods);
        CheckBox chkClothes = dialog.findViewById(R.id.chkClothes);
        CheckBox chkAccessories = dialog.findViewById(R.id.chkAccessories);
        CheckBox chkElectronics = dialog.findViewById(R.id.chkElectronics);

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnApply = dialog.findViewById(R.id.btnApply);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            List<String> selectedCategories = new ArrayList<>();
            if (chkFoods.isChecked()) selectedCategories.add("Foods");
            if (chkClothes.isChecked()) selectedCategories.add("Clothes");
            if (chkAccessories.isChecked()) selectedCategories.add("Accessories");
            if (chkElectronics.isChecked()) selectedCategories.add("Electronics");

            applyFilters(selectedCategories);
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
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

    private void applyFilters(List<String> selectedCategories) {
        CollectionReference productsRef = db.collection("products");

        productsRef.whereEqualTo("status", "approved")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        productList.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            if (selectedCategories.contains(product.getCategory())) {
                                productList.add(product);
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                });
        Toast.makeText(requireContext(), "Filters applied: " + selectedCategories, Toast.LENGTH_SHORT).show();
    }
}
