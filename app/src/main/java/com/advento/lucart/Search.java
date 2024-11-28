package com.advento.lucart;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.advento.lucart.databinding.ActivitySearchBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Search extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private List<Product> productList;
    private ProductAdapter productAdapter;
    private List<Product> allProducts;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.tbSearch);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Drawable customBackButton = ContextCompat.getDrawable(this, R.drawable.ic_custom_back_activities);
        if (customBackButton != null) {
            customBackButton.setTint(ContextCompat.getColor(this, R.color.eleven_green));
            getSupportActionBar().setHomeAsUpIndicator(customBackButton);
        }

        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();
        allProducts = new ArrayList<>();

        productAdapter = new ProductAdapter(this, productList, product -> {
            Intent intent = new Intent(this, ProductOverview.class);
            intent.putExtra("productId", product.getProductId());
            intent.putExtra("productName", product.getProductName());
            intent.putExtra("productPrice", product.getProductPrice());
            intent.putExtra("productDescription", product.getProductDescription());
            intent.putExtra("productCategory", product.getProductCategory());
            intent.putExtra("productImage", product.getProductImage());
            startActivity(intent);
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.rvBrowse.setLayoutManager(gridLayoutManager);
        binding.rvBrowse.setAdapter(productAdapter);

        loadApprovedProducts();
        setupSearchFunction();

        binding.ivCategory.setOnClickListener(v -> {
            showDialog();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupSearchFunction() {
        binding.svSearch.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText) && !newText.isEmpty()) {
                    filterProducts(newText);
                } else {
                    productList.clear();
                    productList.addAll(allProducts);
                    productAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productList.clear();
        productList.addAll(filteredList);
        productAdapter.notifyDataSetChanged();
    }

    private void showDialog() {
        Dialog dialog = new Dialog(this);
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
                            if (selectedCategories.contains(product.getProductCategory())) {
                                productList.add(product);
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                });
        Toast.makeText(this, "Filters applied: " + selectedCategories, Toast.LENGTH_SHORT).show();
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