package com.advento.lucart;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityHomeBinding;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.bumptech.glide.Glide;

public class Home extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private GoogleSignInClient googleSignInClient;

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Google Sign-In client
        initializeGoogleSignInClient();

        // Check if data from Google or Facebook is passed through intent
//        handleGoogleIntent();
//        handleFacebookIntent();

//        binding.btnLogout.setOnClickListener(v -> signOut());
    }

    private void initializeGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

//    private void handleGoogleIntent() {
//        Intent intent = getIntent();
//        String firstName = intent.getStringExtra("FIRST_NAME");
//        String email = intent.getStringExtra("EMAIL");
//        String photoUrl = intent.getStringExtra("PHOTO_URL");
//
//        // Set data if available from Google
//        if (firstName != null) binding.tvFirstName.setText(firstName);
//        if (email != null) binding.tvEmailAddress.setText(email);
//        if (photoUrl != null) Glide.with(this).load(photoUrl).into(binding.ivDisplayPhoto);
//
//        // Redirect to login if the user is not authenticated
//        if (user == null) {
//            startActivity(new Intent(Home.this, MainActivity.class));
//            finish();
//        }
//    }

//    private void handleFacebookIntent() {
//        Intent intent = getIntent();
//        String name = intent.getStringExtra("NAME");
//        String email = intent.getStringExtra("EMAIL");
//        String photoUrl = intent.getStringExtra("PHOTO_URL");
//
//        // Set data if available from Facebook
//        if (name != null) binding.tvFirstName.setText(name);
//        if (email != null) binding.tvEmailAddress.setText(email);
//        if (photoUrl != null) Glide.with(this).load(photoUrl).into(binding.ivDisplayPhoto);
//
//        // Redirect to login if the user is not authenticated
//        if (user == null) {
//            startActivity(new Intent(Home.this, MainActivity.class));
//            finish();
//        }
//    }

    private void signOut() {
        // Firebase sign out
        auth.signOut();

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Facebook sign out
                LoginManager.getInstance().logOut();

                // Redirect to MainActivity
                Intent intent = new Intent(Home.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
