package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.advento.lucart.databinding.FragmentBusinessProfileBinding;
import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BusinessProfileFragment extends Fragment {

    private FragmentBusinessProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private GoogleSignInClient googleSignInClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Initialize Google Sign-In client
        initializeGoogleSignInClient();

        // Check if user is signed in
        if (user == null) {
            Log.e("ProfileFragment", "User not signed in");
            // Handle the case where the user is not signed in (e.g., redirect to login)
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBusinessProfileBinding.inflate(inflater, container, false);


        binding.btnMyProfile.setOnClickListener(v -> {
//            startActivity(new Intent(getContext(), MyBusinessProfile.class)); // PLEASE WAG MONG KALIMUTAN TO SAM
        });
        binding.btnSignOut.setOnClickListener(v -> signOut());

        binding.ivSettings.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Settings.class));
        });

        // Load user data after the fragment's view has been created
        loadData();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Move the loadData call here instead of onCreate
        // Now we are sure the view is ready and attached to the fragment
        loadData();
    }

    private void loadData() {
        if (!isAdded()) {
            // If the fragment is not attached, skip the operation to avoid crashes
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        db.collection("business").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String businessName = documentSnapshot.getString("businessName");
                        String email = documentSnapshot.getString("email");
                        String photoUrl = documentSnapshot.getString("photoUrl");

                        if (isAdded()) {  // Make sure the fragment is still attached before accessing UI
                            binding.tvFirstName.setText(businessName);
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
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initializeGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void signOut() {
        if (!isAdded()) return;

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
