package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.advento.lucart.databinding.FragmentProfileBinding;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private FragmentProfileBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Check if user is signed in
        if (user == null) {
            // Handle the case where the user is not signed in (e.g., redirect to login)
            Log.e("ProfileFragment", "User not signed in");
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

        if (binding == null) {
            Log.e("ProfileFragment", "Binding is null");
            return null;  // Return null if binding could not be initialized
        }

        // Set up click listeners with checks for fragment attachment
        binding.ivSettings.setOnClickListener(v -> {
            if (isAdded()) {
                startActivity(new Intent(getActivity(), Settings.class));
            }
        });
        binding.btnMyProfile.setOnClickListener(v -> {
            if (isAdded()) {
                startActivity(new Intent(getActivity(), MyProfile.class));
            }
        });
        binding.btnChangeAccount.setOnClickListener(v -> {
            if (isAdded()) {
                startActivity(new Intent(getActivity(), Favorites.class));
            }
        });
        binding.btnTransactions.setOnClickListener(v -> {
            if (isAdded()) {
                startActivity(new Intent(getActivity(), Transactions.class));
            }
        });
        binding.btnSignOut.setOnClickListener(v -> signOut());

        // Load user data after the view is created
        loadData();

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
        // Check if the fragment is still attached to the activity and binding is not null
        if (isAdded() && binding != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String email = documentSnapshot.getString("email");
                            String photoUrl = documentSnapshot.getString("photoUrl");

                            // Ensure the fragment is still added before updating UI elements
                            if (isAdded() && binding != null) {
                                binding.tvFirstName.setText(firstName);
                                binding.tvLastName.setText(lastName);
                                binding.tvEmailAddress.setText(email);

                                if (photoUrl != null && !photoUrl.isEmpty()) {
                                    Glide.with(requireContext())
                                            .load(photoUrl)
                                            .circleCrop()
                                            .into(binding.ivDisplayPhoto);
                                } else {
                                    binding.ivDisplayPhoto.setImageResource(R.drawable.ic_photo_placeholder);
                                }
                            }
                        } else {
                            Log.e("ProfileFragment", "No data found for the user.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileFragment", "Error fetching user data: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("ProfileFragment", "Fragment is not added, aborting data load.");
        }
    }

    private void signOut() {
        auth.signOut();

        googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            LoginManager.getInstance().logOut();

            Intent intent = new Intent(requireActivity(), SplashScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}
