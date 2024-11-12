package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private List<Product> allProducts;  // To store all products for filtering
    private androidx.appcompat.widget.SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firestore and RecyclerView
        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize product list and adapter
        productList = new ArrayList<>();
        allProducts = new ArrayList<>();

        // Setting up adapter with item click functionality
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

        // Initialize SearchView
        searchView = view.findViewById(R.id.searchView);
        setupSearchFunction();

        // Load approved products from Firestore
        loadApprovedProducts();

        // Set up filter button click to show filter dialog
        Button filterButton = view.findViewById(R.id.btnFilter);
        filterButton.setOnClickListener(v -> showFilterDialog());

        ImageButton ivBell = view.findViewById(R.id.ivBell);
        ivBell.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), Notifications.class);
            startActivity(intent);
        });

        return view;
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
                        productList.clear();  // Clear the list to avoid duplicate entries
                        allProducts.clear();  // Clear the backup list as well

                        // Loop through each document and convert to Product object
                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            productList.add(product);  // Add product to display list
                            allProducts.add(product);  // Add product to backup list
                        }
                        productAdapter.notifyDataSetChanged();  // Notify adapter of data change
                    }
                });
    }

    private void setupSearchFunction() {
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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

    private void showFilterDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Categories");

        // Inflate the custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        // Get references to checkboxes and buttons in the dialog layout
        CheckBox chkFoods = dialogView.findViewById(R.id.chkFoods);
        CheckBox chkClothes = dialogView.findViewById(R.id.chkClothes);
        CheckBox chkAccessories = dialogView.findViewById(R.id.chkAccessories);
        CheckBox chkElectronics = dialogView.findViewById(R.id.chkElectronics);

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnApply = dialogView.findViewById(R.id.btnApply);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set up button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            // Filter logic for selected categories
            List<String> selectedCategories = new ArrayList<>();
            if (chkFoods.isChecked()) selectedCategories.add("Foods");
            if (chkClothes.isChecked()) selectedCategories.add("Clothes");
            if (chkAccessories.isChecked()) selectedCategories.add("Accessories");
            if (chkElectronics.isChecked()) selectedCategories.add("Electronics");

            applyFilters(selectedCategories);
            dialog.dismiss();
        });
    }

    private void applyFilters(List<String> selectedCategories) {
        // Reference to the products collection
        CollectionReference productsRef = db.collection("products");

        // Query for approved products in selected categories
        productsRef.whereEqualTo("status", "approved")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle the error
                        return;
                    }
                    if (value != null) {
                        productList.clear(); // Clear the list for filtered results

                        // Loop through each document, filter by category
                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            if (selectedCategories.contains(product.getCategory())) {
                                productList.add(product); // Add filtered product to list
                            }
                        }
                        productAdapter.notifyDataSetChanged(); // Notify adapter of data change
                    }
                });
        Toast.makeText(requireContext(), "Filters applied: " + selectedCategories, Toast.LENGTH_SHORT).show();
    }
}