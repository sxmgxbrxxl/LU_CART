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

    private Context context;
    private List<Shop> shopList;
    private ShopClickListener clickListener;

    public interface ShopClickListener {
        void onShopClick(Shop shop);
    }

    public ShopAdapter(Context context, List<Shop> shopList, ShopClickListener clickListener) {
        this.context = context;
        this.shopList = shopList;
        this.clickListener = clickListener;
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
        holder.tvShopName.setText(shop.getName());
        Glide.with(context).load(shop.getImageUrl()).into(holder.ivShopImage);

        holder.itemView.setOnClickListener(v -> clickListener.onShopClick(shop));
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        ImageView ivShopImage;
        TextView tvShopName;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivShopImage = itemView.findViewById(R.id.ivShop);
            tvShopName = itemView.findViewById(R.id.tvShop);
        }
    }
}
