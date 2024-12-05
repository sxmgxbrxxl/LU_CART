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

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private final Context context;
    private final List<Shop> shopList;
    private final OnShopClickListener shopClickListener;

    public ShopAdapter(Context context, List<Shop> shopList, OnShopClickListener listener) {
        this.context = context;
        this.shopList = shopList;
        this.shopClickListener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.shop_icon_layout, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Shop shop = shopList.get(position);
        holder.tvShopName.setText(shop.getBusinessName());
        Glide.with(context).load(shop.getPhotoUrl()).circleCrop().into(holder.ivShopImage);

        holder.itemView.setOnClickListener(v -> {
            if (shopClickListener != null) {
                shopClickListener.onShopClick(shop);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView tvShopName;
        ImageView ivShopImage;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvShop);
            ivShopImage = itemView.findViewById(R.id.ivShop);
        }
    }

    public interface OnShopClickListener {
        void onShopClick(Shop shop);
    }
}
