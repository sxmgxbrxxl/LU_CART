package com.advento.lucart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DescriptionPagerAdapter extends RecyclerView.Adapter<DescriptionPagerAdapter.DescriptionViewHolder> {

    private final Context context;
    private final List<String> descriptions;

    public DescriptionPagerAdapter(Context context, List<String> descriptions) {
        this.context = context;
        this.descriptions = descriptions;
    }

    @NonNull
    @Override
    public DescriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_description, parent, false);
        return new DescriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DescriptionViewHolder holder, int position) {
        String description = descriptions.get(position);
        holder.textViewDescription.setText(description);
    }

    @Override
    public int getItemCount() {
        return descriptions.size();
    }

    static class DescriptionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDescription;

        public DescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
