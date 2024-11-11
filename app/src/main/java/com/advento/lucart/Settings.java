package com.advento.lucart;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivitySettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.FirebaseDatabase;

public class Settings extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseDatabase realtimeDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase services
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        realtimeDb = FirebaseDatabase.getInstance("https://lu-cart-firebase-default-rtdb.asia-southeast1.firebasedatabase.app");

        // Set up window insets and padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        // Set up the toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Night mode toggle
        binding.scNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int color = ContextCompat.getColor(this, R.color.sub_green);
            if (isChecked) {
                binding.scNightMode.getTrackDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            } else {
                binding.scNightMode.getThumbDrawable().clearColorFilter();
                binding.scNightMode.getTrackDrawable().clearColorFilter();
            }
        });

        // Notifications toggle
        binding.scNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int color = ContextCompat.getColor(this, R.color.sub_green);
            if (isChecked) {
                binding.scNotifications.getTrackDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            } else {
                binding.scNotifications.getThumbDrawable().clearColorFilter();
                binding.scNotifications.getTrackDrawable().clearColorFilter();
            }
        });

        // Delete account action
        binding.cvDeleteAccount.setOnClickListener(v -> showConfirmationDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(Settings.this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action is irreversible.")
                .setPositiveButton("Yes", (dialog, which) -> deleteUserAccount())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(Settings.this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        Log.d("DeleteUser", "User ID: " + userId);

        // Delete Firestore User Data
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> deleteUserCart(userId, user))
                .addOnFailureListener(e -> handleError("Firestore user data", e));

    }

    private void deleteUserCart(String userId, FirebaseUser user) {
        db.collection("carts").document(userId).delete()
                .addOnSuccessListener(aVoid -> deleteRealtimeDbUser(userId, user))
                .addOnFailureListener(e -> handleError("Firestore cart data", e));
    }

    private void deleteRealtimeDbUser(String userId, FirebaseUser user) {
        realtimeDb.getReference("users").child(userId).removeValue()
                .addOnSuccessListener(aVoid -> deleteProfilePhoto(userId, user))
                .addOnFailureListener(e -> handleError("Realtime Database user data", e));
    }

    private void deleteProfilePhoto(String userId, FirebaseUser user) {
        StorageReference profilePicRef = storage.getReference().child("profile_photos/" + userId + ".jpg");
        profilePicRef.delete()
                .addOnSuccessListener(aVoid -> deleteUserProductImages(userId, user))
                .addOnFailureListener(e -> {
                    handleError("Profile photo", e);
                    deleteUserProductImages(userId, user);  // Continue with product images even if profile photo deletion fails
                });
    }

    private void deleteUserProductImages(String userId, FirebaseUser user) {
        StorageReference userProductsRef = storage.getReference().child("product_images/" + userId + ".jpg");
        userProductsRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                fileRef.delete().addOnSuccessListener(aVoid -> Log.d("DeleteUser", "Deleted product image: " + fileRef.getPath()))
                        .addOnFailureListener(e -> handleError("Product image", e));
            }
            deleteUserAuthentication(user);
        }).addOnFailureListener(e -> {
            handleError("Listing product images", e);
            deleteUserAuthentication(user);
        });
    }

    // Handle user deletion from Authentication
    private void deleteUserAuthentication(FirebaseUser user) {
        user.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Settings.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.this, SplashScreen.class));
                    finish();
                })
                .addOnFailureListener(e -> handleError("Authentication user", e));
    }

    private void handleError(String dataType, Exception e) {
        Log.e("DeleteUser", "Failed to delete " + dataType + ": " + e.getMessage());
        Toast.makeText(Settings.this, "Failed to delete " + dataType + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
