package com.advento.lucart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private ProductAdapter productAdapter;
    private List<Product> approvedProducts;
    private String currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentMyProductsBinding binding = FragmentMyProductsBinding.inflate(inflater, container, false);

        // Initialize user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.ivAddProduct.setOnClickListener(v -> showAddProductDialog());

        RecyclerView recyclerView = binding.rvApprovedProducts;
        approvedProducts = new ArrayList<>(); // This will be fetched from the database
        productAdapter = new ProductAdapter(getContext(), approvedProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(productAdapter);

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchApprovedProducts();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Fetch approved products from Firestore on fragment creation
        fetchApprovedProducts();

        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showAddProductDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_addproduct_layout, null);

        imageViewProduct = dialogView.findViewById(R.id.ivProductImage);
        Button buttonUploadPhoto = dialogView.findViewById(R.id.buttonUploadPhoto);
        EditText editTextProductName = dialogView.findViewById(R.id.editTextProductName);
        EditText editTextProductPrice = dialogView.findViewById(R.id.editTextProductPrice);
        EditText editTextProductDescription = dialogView.findViewById(R.id.editTextProductDescription);
        EditText editTextStockNumber = dialogView.findViewById(R.id.editTextStockNumber);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Product")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    // Fetching user-entered data from dialog fields
                    String productName = editTextProductName.getText().toString();
                    String productPrice = editTextProductPrice.getText().toString();
                    String productDescription = editTextProductDescription.getText().toString();
                    String stockNumber = editTextStockNumber.getText().toString();
                    String status = "pending"; // Default status for admin approval

                    if (imageUri != null) {
                        // Define the Firebase Storage path for product images
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("product_images/" + System.currentTimeMillis() + ".jpg");

                        // Upload the image to Firebase Storage
                        storageRef.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    // Get the download URL for the uploaded image
                                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        // Create product object with the image download URL
                                        Product newProduct = new Product(productName, productPrice, productDescription, uri.toString(), "", status, currentUserId);

                                        // Save to Firestore
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        db.collection("products").add(newProduct)
                                                .addOnSuccessListener(documentReference -> {
                                                    // Save document ID as productId in Firestore
                                                    db.collection("products").document(documentReference.getId())
                                                            .update("productId", documentReference.getId())
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(getContext(), "Product added to pending list", Toast.LENGTH_SHORT).show();
                                                                fetchApprovedProducts(); // Refresh list if needed
                                                            });
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error adding product", Toast.LENGTH_SHORT).show());
                                    });
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        buttonUploadPhoto.setOnClickListener(v -> openImagePicker());
        builder.create().show();
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

    // Method to fetch approved products from Firestore
    @SuppressLint("NotifyDataSetChanged")
    private void fetchApprovedProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereEqualTo("status", "approved") // Fetch only approved products for homepage
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        approvedProducts.clear(); // Clear the current list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            approvedProducts.add(product); // Add each approved product to the list
                        }
                        productAdapter.notifyDataSetChanged(); // Notify adapter to refresh the list
                    } else {
                        Toast.makeText(getContext(), "Error fetching products", Toast.LENGTH_SHORT).show();
                    }
                });

        // Fetch products specific to the current user for "My Products"
        fetchUserProducts();
    }

    // Method to fetch approved products by the current user
    @SuppressLint("NotifyDataSetChanged")
    private void fetchUserProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereEqualTo("userId", currentUserId) // Fetch products uploaded by the current user
                .whereEqualTo("status", "approved") // Ensure they are approved
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        approvedProducts.clear(); // Clear the current list for My Products
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            approvedProducts.add(product); // Add each product by the current user to the list
                        }
                        productAdapter.notifyDataSetChanged(); // Notify adapter to refresh the list
                    } else {
                        Toast.makeText(getContext(), "Error fetching user products", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
