package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityAdminDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboard extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnPendingProducts.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, PendingProductsActivity.class));
        });

        // Set click listener for View Statistics button
        binding.btnViewStatistics.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, ViewStatisticsActivity.class));
        });

        binding.btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Make sure to sign out the user
            startActivity(new Intent(AdminDashboard.this, SplashScreen.class));
        });
    }
}
