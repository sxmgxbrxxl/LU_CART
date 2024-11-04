package com.advento.lucart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyCartFragment extends Fragment implements CartAdapter.CartItemClickListener {
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;
    private String userId;
    private ProgressBar progressBar;
    private TextView tvTotalPrice;
    private TextView tvEmptyCart;
    private double totalPrice = 0.0;

    public MyCartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_cart, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.rvCart);
        progressBar = view.findViewById(R.id.progressBar);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        tvEmptyCart = view.findViewById(R.id.tvEmptyCart);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Setup RecyclerView
        cartItems = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(getContext(), cartItems, this);
        recyclerView.setAdapter(adapter);

        // Load cart items
        loadCartItems();

        return view;
    }

    private void loadCartItems() {
        showLoading(true);

        db.collection("carts")
                .document(userId)
                .collection("items")
                .addSnapshotListener((value, error) -> {
                    showLoading(false);

                    // Check if the fragment is still attached before showing a Toast
                    if (error != null) {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Error loading cart items", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    cartItems.clear();
                    totalPrice = 0.0;

                    if (value != null && !value.isEmpty()) {
                        for (QueryDocumentSnapshot document : value) {
                            CartItem item = document.toObject(CartItem.class);
                            cartItems.add(item);
                            totalPrice += (item.getPrice() * item.getQuantity());
                        }
                        updateUI(false);
                    } else {
                        updateUI(true);
                    }

                    updateTotalPrice();
                    adapter.notifyDataSetChanged();
                });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void updateUI(boolean isEmpty) {
        if (tvEmptyCart != null) {
            tvEmptyCart.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (tvTotalPrice != null) {
            tvTotalPrice.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void updateTotalPrice() {
        NumberFormat formatPHP = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        String formattedTotal = formatPHP.format(totalPrice);
        tvTotalPrice.setText("Total: " + formattedTotal);
    }

    @Override
    public void onDeleteItem(CartItem item, int position) {
        cartItems.remove(position);
        adapter.notifyItemRemoved(position);
        totalPrice -= (item.getPrice() * item.getQuantity());
        updateTotalPrice();

        if (cartItems.isEmpty()) {
            updateUI(true);
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int position, double newTotalPrice) {
        totalPrice = 0.0;
        for (CartItem cartItem : cartItems) {
            totalPrice += (cartItem.getPrice() * cartItem.getQuantity());
        }
        updateTotalPrice();
    }
}