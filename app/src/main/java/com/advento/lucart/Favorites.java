package com.advento.lucart;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.advento.lucart.databinding.ActivityFavoritesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Favorites extends AppCompatActivity implements FavoritesAdapter.OnDeleteListener {

    private ActivityFavoritesBinding binding;
    private FavoritesAdapter adapter;
    private List<FavoriteItem> favoriteItemList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Set up toolbar with ViewBinding
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize the favorite item list and adapter with listener for delete
        favoriteItemList = new ArrayList<>();
        adapter = new FavoritesAdapter(favoriteItemList, this, this);

        // Set up RecyclerView layout manager and adapter
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFavorites.setAdapter(adapter);

        // Load favorite items from Firestore
        loadFavorites();

        // Toggle edit mode when ivEdit is clicked
        binding.ivEdit.setOnClickListener(v -> toggleEditMode());
    }

    private void loadFavorites() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userId = mAuth.getCurrentUser().getUid();
        db.collection("favorites")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
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
                    Toast.makeText(Favorites.this, "Failed to load favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.tvEmptyFavorites.setVisibility(View.VISIBLE);
                    binding.rvFavorites.setVisibility(View.GONE);
                });
    }

    private void showEdit() {
        // Show or hide the edit button based on whether favorites exist
        binding.ivEdit.setVisibility(favoriteItemList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode; // Toggle edit mode

        // Update the edit button icon
        binding.ivEdit.setImageResource(isEditMode ? R.drawable.ic_check : R.drawable.ic_edit);

        // Notify adapter to show/hide delete buttons
        adapter.setEditMode(isEditMode);
    }

    @Override
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
                        binding.tvEmptyFavorites.setVisibility(View.VISIBLE);
                        binding.rvFavorites.setVisibility(View.GONE);
                    }
                    showEdit();
                })
                .addOnFailureListener(e -> {
                    // Handle failure case
                    Toast.makeText(this, "Failed to delete item from favorites", Toast.LENGTH_SHORT).show();
                    // Optionally, undo item removal if Firestore deletion fails
                    favoriteItemList.add(position, item);
                    adapter.notifyItemInserted(position);
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the ViewBinding reference to avoid memory leaks
        binding = null;
    }
}
