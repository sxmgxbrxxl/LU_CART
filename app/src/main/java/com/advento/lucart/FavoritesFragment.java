package com.advento.lucart;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.advento.lucart.databinding.FragmentFavoritesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements FavoritesAdapter.OnDeleteListener {

    private FragmentFavoritesBinding binding;
    private FavoritesAdapter adapter;
    private List<FavoriteItem> favoriteItemList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        favoriteItemList = new ArrayList<>();
        adapter = new FavoritesAdapter(favoriteItemList, requireContext(), this);

        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFavorites.setAdapter(adapter);

        loadFavorites();
        binding.ivEdit.setOnClickListener(v -> toggleEditMode());

        return binding.getRoot();
    }


    private void loadFavorites() {
        if (mAuth.getCurrentUser() == null || binding == null) {
            // Ensure that binding is not null before accessing any views
            Toast.makeText(getContext(), "No user is logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userId = mAuth.getCurrentUser().getUid();
        db.collection("favorites")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding == null) return;  // Check if binding is null before accessing views

                    favoriteItemList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        binding.tvEmptyFavorites.setVisibility(View.VISIBLE);
                        binding.rvFavorites.setVisibility(View.GONE);
                    } else {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            FavoriteItem item = doc.toObject(FavoriteItem.class);
                            favoriteItemList.add(item);
                        }
                        binding.tvEmptyFavorites.setVisibility(View.GONE);
                        binding.rvFavorites.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                    showEdit();
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;  // Check if binding is null before accessing views

                    Toast.makeText(getContext(), "Failed to load favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.tvEmptyFavorites.setVisibility(View.VISIBLE);
                    binding.rvFavorites.setVisibility(View.GONE);
                });
    }

    private void showEdit() {
        // Show or hide the edit button based on whether favorites exist
        if (binding != null) {  // Check if binding is not null before updating the UI
            binding.ivEdit.setVisibility(favoriteItemList.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode; // Toggle edit mode

        // Update the edit button icon
        if (binding != null) {  // Check if binding is not null
            binding.ivEdit.setImageResource(isEditMode ? R.drawable.ic_check : R.drawable.ic_edit);
        }

        // Notify adapter to show/hide delete buttons
        adapter.setEditMode(isEditMode);
    }

    public void onDeleteItem(FavoriteItem item, int position) {
        // Remove item from the local list and update the adapter
        favoriteItemList.remove(position);
        adapter.notifyItemRemoved(position);

        // Delete item from Firestore
        db.collection("favorites")
                .document(userId)
                .collection("items")
                .document(item.getProductId()) // Assuming getProductId() gives the unique ID for the product
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (favoriteItemList.isEmpty()) {
                        if (binding != null) {  // Check if binding is not null
                            binding.tvEmptyFavorites.setVisibility(View.VISIBLE);
                            binding.rvFavorites.setVisibility(View.GONE);
                        }
                    }
                    showEdit();
                })
                .addOnFailureListener(e -> {
                    // Handle failure case
                    Toast.makeText(getContext(), "Failed to delete item from favorites", Toast.LENGTH_SHORT).show();
                    favoriteItemList.add(position, item);
                    adapter.notifyItemInserted(position);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}
