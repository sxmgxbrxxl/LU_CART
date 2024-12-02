package com.advento.lucart;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.advento.lucart.databinding.FragmentMyProductsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MyProductsFragment extends Fragment {

    private Uri imageUri;
    private ImageView imageViewProduct;
    private List<Product> approvedProducts;
    private String currentUserId;
    private ProductAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentMyProductsBinding binding = FragmentMyProductsBinding.inflate(inflater, container, false);

        // Initialize user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.ivAddProduct.setOnClickListener(v -> showAddProductDialog());

        // Initialize RecyclerView and product list
        RecyclerView recyclerView = binding.rvApprovedProducts;
        approvedProducts = new ArrayList<>(); // This will be populated from the database

        // Initialize adapter with click listener to navigate to MyProductOverview
        adapter = new ProductAdapter(getContext(), approvedProducts, product -> {
            Intent intent = new Intent(getContext(), MyProductOverview.class);
            intent.putExtra("productId", product.getProductId());
            intent.putExtra("productName", product.getProductName());
            intent.putExtra("productPrice", product.getProductPrice());
            intent.putExtra("productDescription", product.getProductDescription());
            intent.putExtra("productCategory", product.getProductCategory());
            intent.putExtra("productImage", product.getProductImage());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter); // Set the adapter on the RecyclerView

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchApprovedProducts();
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        // Fetch approved products from Firestore on fragment creation
        fetchApprovedProducts();

        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showAddProductDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_addproduct_layout);

        imageViewProduct = dialog.findViewById(R.id.ivProductImage);
        Button buttonUploadPhoto = dialog.findViewById(R.id.buttonUploadPhoto);
        EditText editTextProductName = dialog.findViewById(R.id.editTextProductName);
        EditText editTextProductPrice = dialog.findViewById(R.id.editTextProductPrice);
        EditText editTextProductDescription = dialog.findViewById(R.id.editTextProductDescription);
        EditText editTextStockNumber = dialog.findViewById(R.id.editTextStockNumber);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);

        Button addButton = dialog.findViewById(R.id.btnAdd);
        Button cancelButton = dialog.findViewById(R.id.btnCancel);

        addButton.setOnClickListener(view -> {
            String productName = editTextProductName.getText().toString().trim();
            String productPrice = editTextProductPrice.getText().toString().trim();
            String productDescription = editTextProductDescription.getText().toString().trim();
            String stockNumber = editTextStockNumber.getText().toString().trim();
            String productCategory = spinnerCategory.getSelectedItem().toString();
            String status = "pending"; //Default for Admin

            if (productName.isEmpty() || productPrice.isEmpty() || productDescription.isEmpty() || stockNumber.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(productPrice);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid price", Toast.LENGTH_SHORT).show();
                return;
            }

            if (price > 9999) {
                Toast.makeText(getContext(), "Product price cannot exceed 9999", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                // Upload image and save product logic
                // ...
            } else {
                Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            }

            if (imageUri != null) {
                // Upload image to Firebase Storage
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("product_images/" + System.currentTimeMillis() + ".jpg");

                storageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Create new product with image URL
                            Product newProduct = new Product(productName,productCategory,productPrice, stockNumber, productDescription,
                                    uri.toString(), "", status, currentUserId);

                            // Save product to Firestore
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("products").add(newProduct)
                                    .addOnSuccessListener(documentReference -> db.collection("products")
                                            .document(documentReference.getId())
                                            .update("productId", documentReference.getId())
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Product added to pending list", Toast.LENGTH_SHORT).show();
                                                fetchUserProducts(); // Refresh the user’s products
                                            }))
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error adding product", Toast.LENGTH_SHORT).show());
                        }))
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });

        editTextProductPrice.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        buttonUploadPhoto.setOnClickListener(v -> openImagePicker());

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewProduct.setImageURI(imageUri);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchApprovedProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereEqualTo("status", "approved")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        approvedProducts.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            approvedProducts.add(product);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Error fetching products", Toast.LENGTH_SHORT).show();
                    }
                });

        fetchUserProducts();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchUserProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", "approved")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        approvedProducts.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            approvedProducts.add(product);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Error fetching products", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
