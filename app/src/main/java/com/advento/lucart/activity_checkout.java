package com.advento.lucart;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class activity_checkout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize views
        RecyclerView rvCheckoutItems = findViewById(R.id.rvCheckoutItems);
        EditText etLocation = findViewById(R.id.etLocation);
        Spinner spPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        TextView tvTotalPrice = findViewById(R.id.tvTotalPrice);
        Button btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        // Retrieve cart items and total price from the Intent
        List<CartItem> cartItems = getCartItemsFromIntent();
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);

        // Set up RecyclerView
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        CheckoutAdapter adapter = new CheckoutAdapter(this, cartItems);
        rvCheckoutItems.setAdapter(adapter);

        // Display total price
        tvTotalPrice.setText(String.format("Total: PHP %.2f", totalPrice));

        // Set up spinner for payment methods
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Cash on Delivery", "Credit Card", "E-Wallet"});
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPaymentMethod.setAdapter(paymentAdapter);

        // Handle Place Order button click
        btnPlaceOrder.setOnClickListener(v -> {
            String location = etLocation.getText().toString().trim();
            String paymentMethod = spPaymentMethod.getSelectedItem().toString();

            if (location.isEmpty()) {
                Toast.makeText(this, "Please enter a delivery location", Toast.LENGTH_SHORT).show();
            } else {
                // Complete the order
                placeOrder(cartItems, totalPrice, location, paymentMethod);
            }
        });
    }

    private List<CartItem> getCartItemsFromIntent() {
        List<CartItem> cartItems = new ArrayList<>();
        Serializable extraCartItems = getIntent().getSerializableExtra("cartItems");
        if (extraCartItems instanceof ArrayList<?>) {
            try {
                // Safely cast to List<CartItem>
                cartItems = (List<CartItem>) extraCartItems;
            } catch (ClassCastException e) {
                Toast.makeText(this, "Failed to load cart items", Toast.LENGTH_SHORT).show();
            }
        }
        return cartItems;
    }

    private void placeOrder(List<CartItem> cartItems, double totalPrice, String location, String paymentMethod) {
        // Get the current user ID (assuming you have Firebase Authentication set up)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a new transaction object with the status set to "To Ship"
        Transaction transaction = new Transaction(cartItems, userId, "To Ship", location, paymentMethod);

        // Add the transaction to Firestore in the "transactions" collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(documentReference -> {
                    // Show success message
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();

                    // Optionally, go to another screen or update the UI
                    // You can navigate to the user's transaction page or refresh the current page
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to place the order. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }



}
