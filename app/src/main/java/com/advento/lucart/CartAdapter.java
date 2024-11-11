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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final List<CartItem> cartItems;
    private final Context context;
    private final FirebaseFirestore db;
    private final String userId;
    private final CartItemClickListener listener;
    private boolean isEditMode = false;

    // Interface for click events
    public interface CartItemClickListener {
        void onDeleteItem(CartItem item, int position);
        void onQuantityChanged(CartItem item, int position, double newTotalPrice);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, CartItemClickListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_layout, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        // Format price to PHP
        NumberFormat formatPHP = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        String formattedPrice = formatPHP.format(item.getPrice());

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(formattedPrice);
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Load image using Glide (simplified)
        Glide.with(context)
                .load(item.getImageUrl())
                .into(holder.ivImage);

        // Handle quantity changes
        holder.btnIncrease.setOnClickListener(v -> updateQuantity(item, position, 1));
        holder.btnDecrease.setOnClickListener(v -> updateQuantity(item, position, -1));

        // Set visibility of ivDelete based on isEditMode
        holder.ivDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        // Handle delete button click
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteItem(item, holder.getAdapterPosition());
            }
        });
    }

    public void onDeleteItem(CartItem item, int position) {
        // Remove item from Firestore
        db.collection("carts")
                .document(userId)
                .collection("items")
                .document(item.getProductId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove item from local list and notify adapter
                    cartItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartItems.size());
                    Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateQuantity(CartItem item, int position, int delta) {
        int newQuantity = item.getQuantity() + delta;
        if (newQuantity > 0) {
            item.setQuantity(newQuantity);
            double newTotalPrice = item.getPrice() * newQuantity;

            // Update Firestore
            db.collection("carts")
                    .document(userId)
                    .collection("items")
                    .document(item.getProductId())
                    .update("quantity", newQuantity)
                    .addOnSuccessListener(aVoid -> {
                        notifyItemChanged(position);
                        if (listener != null) {
                            listener.onQuantityChanged(item, position, newTotalPrice);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show();
                        // Revert the quantity change
                        item.setQuantity(item.getQuantity() - delta);
                        notifyItemChanged(position);
                    });
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        notifyDataSetChanged();  // Refresh all items to show/hide delete buttons
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivDelete;
        TextView tvName, tvPrice, tvQuantity;
        Button btnIncrease, btnDecrease;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivCartItem);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }
}