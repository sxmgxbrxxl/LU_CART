package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard2);

        // Initialize ImageButtons
        ImageButton btnApproveProducts = findViewById(R.id.btn_approve_products);
        ImageButton btnViewStatistics = findViewById(R.id.btn_view_statistics);

        // Set click listener for Approve Products button
        btnApproveProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, ApproveProductsActivity.class);
            startActivity(intent);
        });

        // Set click listener for View Statistics button
        btnViewStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, ViewStatisticsActivity.class);
            startActivity(intent);
        });


        Button logoutButton = findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Make sure to sign out the user
            Intent intent = new Intent(AdminDashboard.this, EmailLogin.class);
            startActivity(intent);
            finish(); // Optionally call finish() to remove AdminDashboard from the back stack
        });

    }


}
