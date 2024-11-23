package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityRolesBinding;

public class Roles extends AppCompatActivity {

    ActivityRolesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityRolesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String userEmail = getIntent().getStringExtra("EMAIL");
        String userPassword = getIntent().getStringExtra("PASSWORD");

        binding.btnSell.setOnClickListener(v -> {
            Intent intent = new Intent(Roles.this, CreateBusinessAccount.class);
            intent.putExtra("EMAIL", userEmail);
            intent.putExtra("PASSWORD", userPassword);
            startActivity(intent);
        });

        binding.btnShop.setOnClickListener(v -> {
            Intent intent = new Intent(Roles.this, CreatePersonalAccount.class);
            intent.putExtra("EMAIL", userEmail);
            intent.putExtra("PASSWORD", userPassword);
            startActivity(intent);
        });
    }
}