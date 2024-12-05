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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private final List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each transaction
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Bind transaction details
        holder.tvStatus.setText(transaction.getStatus());
        holder.tvLocation.setText(transaction.getDeliveryLocation());
        holder.tvPaymentMethod.setText(transaction.getPaymentMethod());

        // Calculate and display the total price
        double totalPrice = 0;
        for (CartItem product : transaction.getCartItems()) {
            totalPrice += product.getPrice() * product.getQuantity();
        }
        holder.tvPrice.setText(String.format("Total: PHP %.2f", totalPrice));

        // Set up the nested RecyclerView for cart items
        CartItemAdapter cartItemAdapter = new CartItemAdapter(context, transaction.getCartItems());
        holder.rvCartItems.setLayoutManager(new LinearLayoutManager(context)); // Vertical list
        holder.rvCartItems.setAdapter(cartItemAdapter);

        // Dynamically set button text based on status
        if ("To Ship".equals(transaction.getStatus())) {
            holder.btnCancel.setText("Cancel");
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else if ("Cancelled".equals(transaction.getStatus())) {
            holder.btnCancel.setText("Delete");
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }

           // Handle cancel and delete button clicks
        holder.btnCancel.setOnClickListener(v -> {
            if ("To Ship".equals(transaction.getStatus())) {
                // Show confirmation dialog
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Cancel Transaction")
                        .setMessage("Do you want to cancel your transaction?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Move transaction to "Cancelled"
                            SharedTransactionData.getInstance().addToCancelList(transaction);
                            transaction.setStatus("Cancelled"); // Update status locally

                            // Update Firestore status to "Cancelled"
                            FirebaseFirestore.getInstance()
                                    .collection("transactions")
                                    .document(transaction.getTransactionId()) // Assuming the document ID is userId; adjust as needed
                                    .update("status", "Cancelled")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Transaction Cancelled.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update transaction status.", Toast.LENGTH_SHORT).show());

                            // Update UI
                            transactionList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, transactionList.size());
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Dismiss the dialog
                            dialog.dismiss();
                        })
                        .show();
            } else if ("Cancelled".equals(transaction.getStatus())) {
                // Delete transaction from Firestore
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Delete Transaction")
                        .setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Yes", (dialog, which) -> FirebaseFirestore.getInstance()
                                .collection("transactions")
                                .document(transaction.getUserId()) // Ensure unique Firestore document ID
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    transactionList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, transactionList.size());
                                    Toast.makeText(context, "Transaction deleted successfully.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete transaction.", Toast.LENGTH_SHORT).show()))
                        .setNegativeButton("No", (dialog, which) -> {
                            // Dismiss the dialog
                            dialog.dismiss();
                        })
                        .show();
            }
        });
    }

    private void addToCancelSection(Transaction transaction) {
        // Pass the transaction to the cancel list
        // Example: YourActivity.cancelList.add(transaction);
    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // ViewHolder for the RecyclerView items
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvPrice, tvQuantity, tvStatus, tvLocation, tvPaymentMethod;
        RecyclerView rvCartItems; // Add RecyclerView for cart items
        Button btnCancel;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPaymentMethod = itemView.findViewById(R.id.tvModeOfPayment);
            rvCartItems = itemView.findViewById(R.id.rvCartItems); // Initialize the RecyclerView
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }

}
