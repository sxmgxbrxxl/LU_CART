package com.advento.lucart;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String password = charSequence.toString();
                int strength = PasswordStrength.getPasswordStrength(password);

                // Update UI based on strength
                switch (strength) {
                    case 0:
                        binding.tvStrength.setText("Very Weak");
                        binding.tvStrength.setTextColor(Color.RED);
                        break;
                    case 1:
                        binding.tvStrength.setText("Weak");
                        binding.tvStrength.setTextColor(Color.RED);
                        break;
                    case 2:
                        binding.tvStrength.setText("Fair");
                        binding.tvStrength.setTextColor(Color.CYAN);
                        break;
                    case 3:
                        binding.tvStrength.setText("Good");
                        binding.tvStrength.setTextColor(Color.GREEN);
                        break;
                    case 4:
                        binding.tvStrength.setText("Strong");
                        binding.tvStrength.setTextColor(Color.GREEN);
                        break;
                    case 5:
                        binding.tvStrength.setText("Very Strong");
                        binding.tvStrength.setTextColor(Color.GREEN);
                        break;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        binding.btnContinue.setOnClickListener(v -> {

            String userEmail = binding.etEmail.getText().toString().trim();
            String userPassword = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (userEmail.isEmpty()) {
                binding.etEmail.setError("Please enter your email.");
                binding.etEmail.requestFocus();
            } else if (userPassword.isEmpty()) {
                binding.etPassword.setError("Please enter your password.");
                binding.etPassword.requestFocus();
            } else if (userPassword.length() < 8) {
                binding.etPassword.setError("Password must be at least 8 characters.");
                binding.etPassword.requestFocus();
            } else if (confirmPassword.isEmpty()) {
                binding.etConfirmPassword.setError("Please confirm your password.");
                binding.etConfirmPassword.requestFocus();
            } else if (!userPassword.equals(confirmPassword)) {
                binding.etConfirmPassword.setError("Passwords do not match");
                binding.etConfirmPassword.requestFocus();
            } else if ("Very Strong".equals(binding.tvStrength.getText().toString())) {  // Corrected string comparison
                Intent intent = new Intent(EmailSignUp.this, EmailVerification.class);
                intent.putExtra("EMAIL", userEmail);
                intent.putExtra("PASSWORD", userPassword);
                startActivity(intent);
                finish();
            } else {
                // Handle other password strength cases or validation errors here
                Toast.makeText(EmailSignUp.this, "Password strength should be 'Very Strong'.", Toast.LENGTH_SHORT).show();
            }
        });
    }

        @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
