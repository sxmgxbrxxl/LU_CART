package com.advento.lucart;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.advento.lucart.databinding.ActivityCheckoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Checkout extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private double shippingPrice;
    private double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        List<CartItem> cartItems = getCartItemsFromIntent();
        double subtotalPrice = getIntent().getDoubleExtra("subtotalPrice", 0.0);

        binding.rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        CheckoutAdapter adapter = new CheckoutAdapter(this, cartItems);
        binding.rvCheckoutItems.setAdapter(adapter);

        binding.spinnerPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPayment = parent.getItemAtPosition(position).toString();
                if (selectedPayment.equals("Cash on Delivery")) {
                    shippingPrice = 10.00;
                } else {
                    shippingPrice = 0.00;
                }
                totalPrice = subtotalPrice + shippingPrice;
                binding.tvShipping.setText(String.format("₱%.2f", shippingPrice));
                binding.tvTotal.setText(String.format("₱%.2f", totalPrice));
                binding.tvTotalPrice.setText(String.format("Total: ₱%.2f", totalPrice));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.tvSubtotal.setText(String.format("₱%.2f", subtotalPrice));
        binding.tvShipping.setText(String.format("₱%.2f", shippingPrice));
        binding.tvTotal.setText(String.format("₱%.2f", totalPrice));

        binding.tvTotalPrice.setText(String.format("Total: ₱%.2f", totalPrice));

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner_item,
                new String[]{"Cash on Delivery", "Credit Card", "E-Wallet"});
        binding.spinnerPaymentMethod.setAdapter(paymentAdapter);

        ArrayAdapter<String> landmarkAdapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner_item,
                new String[]{"Multipurpose Gym", "New Building", "LU Rooms", "Canteen", "Oreta", "Gym" });
        binding.spinnerLandmark.setAdapter(landmarkAdapter);

        binding.btnPlaceOrder.setOnClickListener(v -> {
            String location = binding.spinnerLandmark.getSelectedItem().toString() + ", " + binding.etLocation.getText().toString().trim();
            String paymentMethod = binding.spinnerPaymentMethod.getSelectedItem().toString();

            if (location.isEmpty()) {
                Toast.makeText(this, "Please enter a delivery location", Toast.LENGTH_SHORT).show();
            } else {
                placeOrder(cartItems, totalPrice, location, paymentMethod);
            }
        });

        setSupportActionBar(binding.tbCheckOut);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Drawable customBackButton = ContextCompat.getDrawable(this, R.drawable.ic_custom_back_activities);
        if (customBackButton != null) {
            customBackButton.setTint(ContextCompat.getColor(this, R.color.eleven_green));
            getSupportActionBar().setHomeAsUpIndicator(customBackButton);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private List<CartItem> getCartItemsFromIntent() {
        List<CartItem> cartItems = new ArrayList<>();
        Serializable extraCartItems = getIntent().getSerializableExtra("cartItems");
        if (extraCartItems instanceof ArrayList<?>) {
            try {
                cartItems = (List<CartItem>) extraCartItems;
            } catch (ClassCastException e) {
                Toast.makeText(this, "Failed to load cart items", Toast.LENGTH_SHORT).show();
            }
        }
        return cartItems;
    }

    private void placeOrder(List<CartItem> cartItems, double totalPrice, String location, String paymentMethod) {
        String buyerId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Group cart items by business
        Map<String, List<CartItem>> businessItemsMap = new HashMap<>();
        for (CartItem item : cartItems) {
            String businessId = item.getBusinessId();
            if (!businessItemsMap.containsKey(businessId)) {
                businessItemsMap.put(businessId, new ArrayList<>());
            }
            businessItemsMap.get(businessId).add(item);
        }

        // Place an order for each business
        for (Map.Entry<String, List<CartItem>> entry : businessItemsMap.entrySet()) {
            String businessId = entry.getKey();
            List<CartItem> itemsForBusiness = entry.getValue();

            // Create the Transaction object without a transactionId
            Transaction transaction = new Transaction(
                    itemsForBusiness,    // List of items for this business
                    buyerId,             // Buyer ID
                    businessId,          // Business ID
                    null,                  // No transactionId yet
                    "To Ship",           // Status
                    location,            // Delivery location
                    paymentMethod     // Payment method
            );

            Dialog progressDialog = new Dialog(this);
            progressDialog.setContentView(R.layout.dialog_loading);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Save each transaction to Firestore
            db.collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener(documentReference -> {
                        // Get the auto-generated document ID from Firestore and update the transactionId
                        String transactionId = documentReference.getId();

                        // Update the transactionId in Firestore
                        transaction.setTransactionId(transactionId);

                        // Now that the transactionId is set, save it again to Firestore
                        documentReference.set(transaction)
                                .addOnSuccessListener(aVoid -> {

                                    progressDialog.dismiss();
                                    startActivity(new Intent(this, FinishCheckout.class));
                                    finish();

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update transaction with ID", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors during the transaction creation
                        Toast.makeText(this, "Failed to place the order for business: " + businessId, Toast.LENGTH_SHORT).show();
                    });
        }

        // After processing all transactions, notify the user about the overall order
        Toast.makeText(this, "Your order is being processed!", Toast.LENGTH_LONG).show();
    }

}
