package com.advento.lucart;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PendingProductsActivity extends AppCompatActivity {

    private ProductAdapter productAdapter;
    private List<Product> pendingProducts;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_pending_products);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestore = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.recycler_view_pending_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabApprove = findViewById(R.id.fab_approve_all);

        pendingProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(this, pendingProducts);
        recyclerView.setAdapter(productAdapter);

        fetchPendingProducts();

        // Set the click listener for the approve button
        fabApprove.setOnClickListener(v -> approveAllProducts());
    }

    // get the users uploaded product in firestore to display in the recycler view
    private void fetchPendingProducts() {
        firestore.collection("products")
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pendingProducts.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            pendingProducts.add(product);
                        }
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error fetching products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to approve all pending products
    private void approveAllProducts() {
        if (pendingProducts.isEmpty()) {
            Toast.makeText(this, "No products to approve", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Product product : new ArrayList<>(pendingProducts)) {
            product.setStatus("approved");

            firestore.collection("products")
                    .document(product.getProductId())
                    .set(product)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PendingProductsActivity.this, "Approved: " + product.getProductName(), Toast.LENGTH_SHORT).show();
                            pendingProducts.remove(product);
                            productAdapter.notifyDataSetChanged(); // Refresh list
                        } else {
                            Toast.makeText(PendingProductsActivity.this, "Failed to approve product: " + product.getProductName(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
