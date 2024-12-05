package com.advento.lucart;

import static java.security.AccessController.getContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PendingProductsActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "product_approval_channel";

    private ProductAdapter productAdapter;
    private List<Product> pendingProducts;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pending_products);

        createNotificationChannel();

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
        productAdapter = new ProductAdapter(this, pendingProducts, true);
        recyclerView.setAdapter(productAdapter);

        ProductAdapter adapter = new ProductAdapter(PendingProductsActivity.this, pendingProducts, product -> {
            Intent intent = new Intent(PendingProductsActivity.this, MyProductOverview.class);
            intent.putExtra("productId", product.getProductId());
            intent.putExtra("productName", product.getProductName());
            intent.putExtra("productPrice", product.getProductPrice());
            intent.putExtra("productDescription", product.getProductDescription());
            intent.putExtra("productCategory", product.getProductCategory());
            intent.putExtra("productImage", product.getProductImage());
            startActivity(intent);
        });

        fetchPendingProducts();

        fabApprove.setOnClickListener(v -> approveAllProducts());
    }

    private void createNotificationChannel() {
        CharSequence name = "Product Approval Notifications";
        String description = "Notifications for approved products";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

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
                            productAdapter.notifyDataSetChanged();
                            sendApprovalNotificationToUser(product.getBusinessId(), product.getProductName());

                            addNotificationForUser(product.getBusinessId(), product.getProductName());
                        } else {
                            Toast.makeText(PendingProductsActivity.this, "Failed to approve product: " + product.getProductName(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void addNotificationForUser(String userId, String productName) {

        SimpleDateFormat sdf = new SimpleDateFormat("EEE MM-dd HH:mm", Locale.getDefault());
        String formattedTimestamp = sdf.format(new Date(System.currentTimeMillis()));

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", "Product Approved");
        notificationData.put("message", "Your product \"" + productName + "\" has been approved.");
        notificationData.put("status", "approved");
        notificationData.put("userId", userId);
        notificationData.put("timestamp", formattedTimestamp);

        firestore.collection("business")
                .document(userId)
                .collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    // Notify success
                    Toast.makeText(this, "Notification added for " + productName, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(this, "Failed to add notification for " + productName, Toast.LENGTH_SHORT).show();
                });
    }


    private void sendApprovalNotificationToUser(String userId, String productName) {
        firestore.collection("business")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userDeviceToken = documentSnapshot.getString("deviceToken");

                        if (userDeviceToken != null) {
                            sendApprovalNotification(productName);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PendingProductsActivity.this, "Failed to retrieve user information", Toast.LENGTH_SHORT).show()
                );
    }

    private void sendApprovalNotification(String productName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check)
                .setContentTitle("Product Approved")
                .setContentText("Your product \"" + productName + "\" has been approved.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
