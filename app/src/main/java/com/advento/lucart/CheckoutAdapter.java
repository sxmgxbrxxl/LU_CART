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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {
    private final Context context;
    private final List<CartItem> checkoutItems;

    public CheckoutAdapter(Context context, List<CartItem> checkoutItems) {
        this.context = context;
        this.checkoutItems = checkoutItems;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem item = checkoutItems.get(position);

        // Load product image
        Glide.with(context)
                .load(item.getImageUrl()) // Assuming CartItem has a getImageUrl() method
                .into(holder.ivProductImage);

        // Set product details
        holder.tvProductName.setText(item.getName());
        holder.tvProductPrice.setText(formatCurrency(item.getPrice()));
        holder.tvProductQuantity.setText("Quantity: " + item.getQuantity());
        holder.tvProductTotal.setText("Total: " + formatCurrency(item.getPrice() * item.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return checkoutItems.size();
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductQuantity, tvProductTotal;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvProductTotal = itemView.findViewById(R.id.tvProductTotal);
        }
    }

    private String formatCurrency(double amount) {
        NumberFormat formatPHP = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        return formatPHP.format(amount);
    }
}
