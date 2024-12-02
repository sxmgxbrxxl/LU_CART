package com.advento.lucart;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.advento.lucart.databinding.ActivityCheckoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Checkout extends AppCompatActivity {

    private ActivityCheckoutBinding binding;

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
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);

        binding.rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        CheckoutAdapter adapter = new CheckoutAdapter(this, cartItems);
        binding.rvCheckoutItems.setAdapter(adapter);

        binding.tvTotalPrice.setText(String.format("Total: PHP %.2f", totalPrice));

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner_item,
                new String[]{"Cash on Delivery", "Credit Card", "E-Wallet"});
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPaymentMethod.setAdapter(paymentAdapter);

        ArrayAdapter<String> landmarkAdapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner_item,
                new String[]{"New Building", "Oreta", "Gym", "Multipurpose Gym", "Canteen", "LU Rooms"});
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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

        // Iterate through each cart item
        for (CartItem item : cartItems) {
            // Ensure CartItem has a sellerId field
            String sellerId = item.getSellerId();

            // Create a transaction for each item
            Transaction transaction = new Transaction(
                    List.of(item),  // Wrap item in a List
                    buyerId,        // Buyer ID
                    sellerId,       // Seller ID
                    "To Ship",      // Status
                    location,       // Delivery location
                    paymentMethod   // Payment method
            );

            // Save each transaction to Firestore
            db.collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener(documentReference -> Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to place the order. Please try again.", Toast.LENGTH_SHORT).show());
        }
    }


}
