package com.advento.lucart;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final List<Product> productList;
    private final Set<String> selectedItems = new HashSet<>();
    private final Context context;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
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
        holder.priceTextView.setText(product.getProductPrice());
        holder.descriptionTextView.setText(product.getProductDescription());
        holder.categoryTextView.setText(product.getCategory());

        // Load the product image using Glide
        Glide.with(context)
                .load(product.getProductImage())
                .into(holder.imageView);

        // Set click listener to open ProductOverview with product details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductOverview.class);
            intent.putExtra("productId", product.getProductId());
            intent.putExtra("productImage", product.getProductImage());
            intent.putExtra("productName", product.getProductName());
            intent.putExtra("productPrice", product.getProductPrice());
            intent.putExtra("productDescription", product.getProductDescription());
            intent.putExtra("productCategory", product.getCategory());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView, descriptionTextView, categoryTextView;
        ImageView imageView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
