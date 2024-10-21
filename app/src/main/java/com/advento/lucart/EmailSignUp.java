package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityEmailSignupBinding;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

public class EmailSignUp extends AppCompatActivity {

    ActivityEmailSignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityEmailSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        binding.btnContinue.setOnClickListener(v -> {

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

            //// NOTE NEED EMAIL AND PASSWORD VALIDATIONS //// ADD TO TO-DO ////

            Intent intent = new Intent(EmailSignUp.this, EmailVerification.class); //CHANGE (FOR TEST ONLY)
            intent.putExtra("EMAIL", userEmail);
            intent.putExtra("PASSWORD", userPassword);
            startActivity(intent);
            finish();
        });

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}