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

public class EmailLogin extends AppCompatActivity {

    ActivityEmailLoginBinding binding;

    private FirebaseAuth auth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(EmailLogin.this, Home.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityEmailLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.cbShow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // Hide password
                binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // Move cursor to the end of the text
            binding.etPassword.setSelection(binding.etPassword.getText().length());
        });

        binding.btnSave.setOnClickListener(v -> {

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
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(EmailLogin.this, "Authentication Successful",
                                        Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(EmailLogin.this, Home.class));
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = auth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(EmailLogin.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}