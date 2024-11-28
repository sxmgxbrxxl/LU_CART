package com.advento.lucart;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<FavoriteItem> favoriteItems;
    private Context context;
    private OnDeleteListener listener;
    private boolean isEditMode = false;

    public FavoritesAdapter(List<FavoriteItem> favoriteItems, Context context, OnDeleteListener listener) {
        this.favoriteItems = favoriteItems;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.favorite_item_layout, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteItem item = favoriteItems.get(position);
        holder.bind(item);

        holder.deleteButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return favoriteItems.size();
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        notifyDataSetChanged();  // Refresh all items to show/hide delete buttons
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textName, textPrice;
        private ImageView deleteButton;
        private Button overView;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivFavoriteItem);
            textName = itemView.findViewById(R.id.tvFavoriteItemName);
            textPrice = itemView.findViewById(R.id.tvFavoriteItemPrice);
            deleteButton = itemView.findViewById(R.id.ivDeleteFavorite);
            overView = itemView.findViewById(R.id.btnProductOverview);

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteItem(favoriteItems.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            overView.setOnClickListener(v -> {
                FavoriteItem item = favoriteItems.get(getAdapterPosition());
                Intent intent = new Intent(context, ProductOverview.class);
                intent.putExtra("productId", item.getProductId());
                intent.putExtra("productImage", item.getProductImage());
                intent.putExtra("productName", item.getProductName());
                intent.putExtra("productCategory", item.getProductCategory());
                intent.putExtra("productPrice", item.getProductPrice());
                intent.putExtra("productDescription", item.getProductDescription());
                context.startActivity(intent);
            });
        }

        public void bind(FavoriteItem item) {
            textName.setText(item.getProductName());
            textPrice.setText("₱ " + item.getProductPrice());
            Glide.with(itemView.getContext()).load(item.getProductImage()).into(imageView);
        }
    }

    public interface OnDeleteListener {
        void onDeleteItem(FavoriteItem item, int position);
    }
}
