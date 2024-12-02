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

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {

    private Context context;
    private List<CartItem> cartItems;

    // Constructor to pass context and list of CartItems
    public CartItemAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each cart item
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        // Bind data to the view holder
        CartItem cartItem = cartItems.get(position);

        holder.tvProductName.setText(cartItem.getName());
        holder.tvProductPrice.setText(String.format("PHP %.2f", cartItem.getPrice()));
        holder.tvProductQuantity.setText(String.format("Quantity: %d", cartItem.getQuantity()));

        // Load the product image using Glide
        Glide.with(context)
                .load(cartItem.getImageUrl()) // Replace with the actual image URL from CartItem
                .placeholder(R.drawable.ic_placeholder_image) // Placeholder image
                .error(R.drawable.ic_placeholder_image) // Fallback image
                .into(holder.ivProductImage);
    }

    @Override
    public int getItemCount() {
        return cartItems.size(); // Return the number of items in the list
    }

    // ViewHolder class to hold references to the views
    public static class CartItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductQuantity;

        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage); // Product image
            tvProductName = itemView.findViewById(R.id.tvProductName); // Product name
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice); // Product price
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity); // Product quantity
        }
    }
}
