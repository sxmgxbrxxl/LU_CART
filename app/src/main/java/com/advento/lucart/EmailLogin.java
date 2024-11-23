package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityEmailLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailLogin extends AppCompatActivity {

    ActivityEmailLoginBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    //ADMIN UID FIXED
    private static final String ADMIN1_UID = "JlmXtJLguLOSoe7HCJ3JOXBVnJ52";
    private static final String ADMIN2_UID = "kVfHiX6EI5bnRYAawNwS3Dw4Bf72";

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getUid().equals(ADMIN1_UID)||currentUser.getUid().equals(ADMIN2_UID)) {
                startActivity(new Intent(EmailLogin.this, AdminDashboard.class));
            } else {
                startActivity(new Intent(EmailLogin.this, Home.class));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityEmailLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(EmailLogin.this, ForgotPassword.class));
        });

        binding.btnLogin.setOnClickListener(v -> {
            String userEmail = binding.etEmail.getText().toString().trim();
            String userPassword = binding.etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(userEmail)) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return; // Exit if credentials are empty
            }

            if (TextUtils.isEmpty(userPassword)) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                return; // Exit if credentials are empty
            }

            auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String userUID = user.getUid();
                                checkAccountType(userUID);
                            }
                        } else {
                            Toast.makeText(EmailLogin.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void checkAccountType(String userUID) {
        db.collection("business").document(userUID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "Welcome, Business Account!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EmailLogin.this, BusinessHome.class));
                    } else {
                        db.collection("users").document(userUID).get()
                                .addOnSuccessListener(userSnapshot -> {
                                    if (userSnapshot.exists()) {
                                        // User account
                                        Toast.makeText(this, "Welcome, User Account!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(EmailLogin.this, Home.class));
                                    } else {
                                        Toast.makeText(this, "Account type not recognized.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error checking user account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking business account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
