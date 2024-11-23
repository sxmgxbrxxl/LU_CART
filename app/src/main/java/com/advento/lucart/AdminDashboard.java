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
import com.google.firebase.auth.FirebaseUser;

public class AdminDashboard extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private FirebaseAuth auth;
    private static final String ADMIN1_UID = "JlmXtJLguLOSoe7HCJ3JOXBVnJ52";
    private static final String ADMIN2_UID = "kVfHiX6EI5bnRYAawNwS3Dw4Bf72";

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

        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            if (currentUser.getUid().equals(ADMIN1_UID)) {
                binding.tvWelcome.setText("Welcome, Mr. Tubelliza!");
            } else if (currentUser.getUid().equals(ADMIN2_UID)) {
                binding.tvWelcome.setText("Welcome, Mr. Advento!");
            } else {
                binding.tvWelcome.setText("Welcome, Administrator!");
            }
        }

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
