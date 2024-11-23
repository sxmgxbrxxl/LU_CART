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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private final Context context;
    private final OnProductClickListener productClickListener;
    private final boolean isPendingList;

    // Constructor for using OnProductClickListener
    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.productClickListener = listener;
        this.isPendingList = false; // default value if not provided
    }

    // Overloaded constructor that accepts a boolean instead of a listener
    public ProductAdapter(Context context, List<Product> productList, boolean isPendingList) {
        this.context = context;
        this.productList = productList;
        this.productClickListener = null; // No listener in this case
        this.isPendingList = isPendingList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_tile, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.nameTextView.setText(product.getProductName());
        holder.priceTextView.setText("₱" + product.getProductPrice());

        // Load the product image using Glide
        Glide.with(context)
                .load(product.getProductImage())
                .into(holder.imageView);

        // Set click listener only if productClickListener is provided
        if (productClickListener != null) {
            holder.itemView.setOnClickListener(v -> productClickListener.onProductClick(product));
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView, categoryTextView;
        ImageView imageView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}
