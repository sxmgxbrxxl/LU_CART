package com.advento.lucart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;

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

        // Check if the transaction has products
        if (transaction.getCartItems() != null && !transaction.getCartItems().isEmpty()) {
            CartItem firstProduct = transaction.getCartItems().get(0);

            // Bind product details
            holder.tvProductName.setText(firstProduct.getName());
            holder.tvPrice.setText(String.format("PHP %.2f", firstProduct.getPrice()));
            holder.tvQuantity.setText(String.format("Quantity: %d", firstProduct.getQuantity()));

            // Calculate total price
            double totalPrice = 0;
            for (CartItem product : transaction.getCartItems()) {
                totalPrice += product.getPrice() * product.getQuantity();
            }
            holder.tvPrice.setText(String.format("Total: PHP %.2f", totalPrice));

            // Load product image using Glide
            Glide.with(context)
                    .load(firstProduct.getImageUrl()) // Replace with actual image URL
                    .placeholder(R.drawable.ic_placeholder_image) // Placeholder image
                    .error(R.drawable.ic_placeholder_image) // Fallback image
                    .into(holder.ivProductImage);

            // Bind transaction details
            holder.tvStatus.setText(transaction.getStatus());
            holder.tvLocation.setText(transaction.getDeliveryLocation());
            holder.tvPaymentMethod.setText(transaction.getPaymentMethod());
        } else {
            // Handle no products
            holder.tvProductName.setText("No products available");
            holder.tvPrice.setText("N/A");
            holder.tvQuantity.setText("Quantity: N/A");
            holder.ivProductImage.setImageResource(R.drawable.ic_placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // ViewHolder for the RecyclerView items
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvPrice, tvQuantity, tvStatus, tvLocation, tvPaymentMethod;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPaymentMethod = itemView.findViewById(R.id.tvModeOfPayment);
        }
    }
}
