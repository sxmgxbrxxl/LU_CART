package com.advento.lucart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityMyProfileBinding;
import com.advento.lucart.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyProfile extends AppCompatActivity {

    private ActivityMyProfileBinding binding;
    private FirebaseDatabase db;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private boolean isEditing = false;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Check if user is signed in
        if (user != null) {
            db = FirebaseDatabase.getInstance("https://lu-cart-firebase-default-rtdb.asia-southeast1.firebasedatabase.app/");
            reference = db.getReference("users").child(user.getUid());
        } else {

        }

        fetchUserData();

        // Set up the edit button click listener
        binding.ivEdit.setOnClickListener(v -> toggleEditMode());

        // Set up the change photo button click listener
        binding.btnChangePhoto.setOnClickListener(v -> openImagePicker());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void fetchUserData() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        binding.etFirstName.setText(user.getFirstName());
                        binding.etLastName.setText(user.getLastName());
                        binding.etEmailAddress.setText(user.getEmail());
                        binding.etBirthday.setText(user.getBirthday());
                        binding.etPhoneNumber.setText(user.getPhoneNumber());
                        binding.etPassword.setText(user.getPassword());

                        if (user.getPhotoUrl() != null) {
                            Glide.with(MyProfile.this)
                                    .load(user.getPhotoUrl())
                                    .circleCrop()
                                    .into(binding.ivDisplayPhoto); // Assuming you have an ImageView for the profile photo
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MyProfile.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEditMode() {
        if (isEditing) {
            saveUserData();
            binding.ivEdit.setImageResource(R.drawable.ic_edit); // Change to edit icon
        } else {
            binding.ivEdit.setImageResource(R.drawable.ic_check); // Change to save icon
        }
        isEditing = !isEditing;

        setFieldsEditable(isEditing);
    }

    private void setFieldsEditable(boolean editable) {
        binding.etFirstName.setEnabled(editable);
        binding.etLastName.setEnabled(editable);
        binding.etEmailAddress.setEnabled(editable);
        binding.etBirthday.setEnabled(editable);
        binding.etPhoneNumber.setEnabled(editable);
        binding.etPassword.setEnabled(editable);
    }

    private void saveUserData() {
        String firstName = binding.etFirstName.getText().toString();
        String lastName = binding.etLastName.getText().toString();
        String email = binding.etEmailAddress.getText().toString();
        String password = binding.etPassword.getText().toString();
        String birthday = binding.etBirthday.getText().toString();
        String phoneNumber = binding.etPhoneNumber.getText().toString();

        // If the photo URL has been changed, use the new URL; otherwise, retain the old one.
        String photoUrl = selectedImageUri != null ? selectedImageUri.toString() : null;

        User updatedUser = new User(firstName, lastName, email, password, birthday, phoneNumber, photoUrl);

        reference.setValue(updatedUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MyProfile.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MyProfile.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_photos")
                    .child(mAuth.getCurrentUser().getUid() + ".jpg");

            storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String photoUrl = uri.toString();
                                updateUserPhotoUrl(photoUrl);
                            }))
                    .addOnFailureListener(e -> Toast.makeText(MyProfile.this, "Failed to upload photo.", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateUserPhotoUrl(String photoUrl) {
        reference.child("photoUrl").setValue(photoUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MyProfile.this, "Photo updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MyProfile.this, "Failed to update photo.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}