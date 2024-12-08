package com.advento.lucart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {

    private final Context context;
    private final List<Transaction> transactionsList;
    private final OnMoveToReceiveListener listener;

    // Constructor with listener
    public TransactionsAdapter(Context context, List<Transaction> transactionsList, OnMoveToReceiveListener listener) {
        this.context = context;
        this.transactionsList = transactionsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transactions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionsList.get(position);

        holder.tvStatus.setText(transaction.getStatus());
        holder.tvLocation.setText(transaction.getDeliveryLocation());
        holder.tvPaymentMethod.setText(transaction.getPaymentMethod());

        FirebaseFirestore.getInstance()
                .collection("transactions")
                .document(transaction.getTransactionId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double shippingFee = documentSnapshot.getDouble("shippingFee");
                        transaction.setShippingFee(shippingFee);
                        holder.tvShippingFee.setText(String.format("Shipping: PHP %.2f", shippingFee));
                    } else {
                        Toast.makeText(context, "Failed to fetch shipping fee", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to fetch shipping fee", Toast.LENGTH_SHORT).show());

        // Calculate and display the total price
        double totalPrice = 0;
        for (CartItem product : transaction.getCartItems()) {
            totalPrice += product.getPrice() * product.getQuantity();
        }
        // Add shipping fee if available
        double shippingFee = transaction.getShippingFee(); // Assuming getShippingFee() exists
        totalPrice += shippingFee;

        holder.tvPrice.setText(String.format("Total: PHP %.2f", totalPrice, shippingFee));


        // Set up the nested RecyclerView for cart items
        CartItemAdapter cartItemAdapter = new CartItemAdapter(context, transaction.getCartItems());
        holder.rvCartItems.setLayoutManager(new LinearLayoutManager(context)); // Vertical list
        holder.rvCartItems.setAdapter(cartItemAdapter);

        // Set button text dynamically based on status
        if ("To Ship".equals(transaction.getStatus())) {
            holder.btnMoveToReceive.setText("Move to Receive");
            holder.btnMoveToReceive.setVisibility(View.VISIBLE);
        } else if ("To Receive".equals(transaction.getStatus())) {
            holder.btnMoveToReceive.setText("Completed");
            holder.btnMoveToReceive.setVisibility(View.VISIBLE);
        } else {
            holder.btnMoveToReceive.setVisibility(View.GONE);
        }

        // Handle button clicks
        holder.btnMoveToReceive.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMoveToReceiveClicked(transaction);
            }
        });

    }

    // Method to update the status of a transaction in Firestore
    private void updateTransactionStatus(Transaction transaction, String newStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("transactions")
                .document(transaction.getTransactionId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Notify the user about the successful update
                    Toast.makeText(context, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    // Optionally, you can refresh the data or re-fetch it to update the UI
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }

    public interface OnMoveToReceiveListener {
        void onMoveToReceiveClicked(Transaction transaction);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvPrice, tvQuantity, tvStatus, tvLocation, tvPaymentMethod, tvShippingFee;
        RecyclerView rvCartItems;
        Button btnMoveToReceive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPaymentMethod = itemView.findViewById(R.id.tvModeOfPayment);
            tvShippingFee = itemView.findViewById(R.id.tvShippingFee);
            rvCartItems = itemView.findViewById(R.id.rvCartItems);
            btnMoveToReceive = itemView.findViewById(R.id.btnMoveToReceive);
        }
    }
}

