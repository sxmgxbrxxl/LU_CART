package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityEmailVerificationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerification extends AppCompatActivity {

    ActivityEmailVerificationBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityEmailVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get email and password from the first activity
        String userEmail = getIntent().getStringExtra("EMAIL");
        String userPassword = getIntent().getStringExtra("PASSWORD");

        // Register user with email and password
        if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPassword)) {
            registerUser(userEmail, userPassword);
        } else {
            Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
        }

        binding.btnVerify.setOnClickListener(v -> checkEmailVerification());
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            // Send verification email
                            sendVerificationEmail();
                        }
                    } else {
                        showCustomDialog();
                    }
                });
    }

    private void sendVerificationEmail() {
        currentUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkEmailVerification() {
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        // User's email is verified, allow them to proceed
                        Intent intent = new Intent(EmailVerification.this, CreateAccountEmail.class);
                        startActivity(intent);
                        finish(); // Close the current activity
                    } else {
                        // Email is not verified
                        Toast.makeText(this, "Email not verified yet. Please check your email.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to reload user information.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showCustomDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_layout, null);

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Access elements inside the dialog
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button dialogButton = dialogView.findViewById(R.id.dialog_button);

        // Set the message dynamically if needed
        dialogMessage.setText("Registration failed: The given email address is already used in another account");

        // Set button action
        dialogButton.setOnClickListener(v -> {
            startActivity(new Intent(EmailVerification.this, MainActivity.class));
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
