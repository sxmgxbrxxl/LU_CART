package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.advento.lucart.databinding.FragmentProfileBinding;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase db;
    private DatabaseReference reference;
    private GoogleSignInClient googleSignInClient;
    private FragmentProfileBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Check if user is signed in
        if (user != null) {
            db = FirebaseDatabase.getInstance("https://lu-cart-firebase-default-rtdb.asia-southeast1.firebasedatabase.app/");
            reference = db.getReference("users").child(user.getUid());
        } else {
            // Handle the case where the user is not signed in (e.g., redirect to login)
            Log.e("ProfileFragment", "User not signed in");
            // You can redirect to a login screen or show a message
            return;
        }

        // Initialize Google Sign-In client
        initializeGoogleSignInClient();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Load user data after the view is created
        loadData();

        // Set up UI actions and event listeners
        binding.btnSignOut.setOnClickListener(v -> signOut());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up to prevent memory leaks
        binding = null;
    }

    private void initializeGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void loadData() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);

                    binding.tvFirstName.setText(firstName);
                    binding.tvLastName.setText(lastName);
                    binding.tvEmailAddress.setText(email);

                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(photoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_placeholder)
                                .into(binding.ivDisplayPhoto);
                    } else {
                        // Optionally set a default image or placeholder if no URL is found
                        binding.ivDisplayPhoto.setImageResource(R.drawable.ic_placeholder);
                    }
                } else {
                    Log.e("ProfileFragment", "No data found for the user.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log the error message
                Log.e("ProfileFragment", "Database error: " + error.getMessage());
            }
        });
    }

    private void signOut() {
        // Firebase sign out
        auth.signOut();

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            // Facebook sign out
            LoginManager.getInstance().logOut();

            // Redirect to SplashScreen
            Intent intent = new Intent(requireActivity(), SplashScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}
