package com.advento.lucart;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityMyProfileBinding;
import com.advento.lucart.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MyProfile extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    private ActivityMyProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private DocumentReference userReference;
    private boolean isEditing = false;
    private Uri selectedImageUri;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Check if the user is logged in
        if (user == null) {
            Toast.makeText(this, "User not signed in.", Toast.LENGTH_SHORT).show();
            finish();
            return; // Stop further execution of the code if no user is logged in.
        }

        firestore = FirebaseFirestore.getInstance();
        userReference = firestore.collection("users").document(user.getUid());

        fetchUserData();

        binding.ivEdit.setOnClickListener(v -> toggleEditMode());
        binding.fabPhoto.setOnClickListener(v -> showDialog());

        setSupportActionBar(binding.tbMyProfile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        Drawable customBackButton = ContextCompat.getDrawable(this, R.drawable.ic_custom_back_activities);
        if (customBackButton != null) {
            customBackButton.setTint(ContextCompat.getColor(this, R.color.eleven_green));
            getSupportActionBar().setHomeAsUpIndicator(customBackButton);
        }

        setFieldsLocked();
    }

    private void setFieldsLocked() {
        binding.etFirstName.setClickable(false);
        binding.etLastName.setClickable(false);
        binding.etPhoneNumber.setClickable(false);

        binding.etFirstName.setFocusable(false);
        binding.etLastName.setFocusable(false);
        binding.etPhoneNumber.setFocusable(false);

        binding.fabPhoto.setVisibility(View.GONE);
    }

    private void fetchUserData() {
        userReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            binding.etFirstName.setText(user.getFirstName());
                            binding.etLastName.setText(user.getLastName());
                            binding.etEmailAddress.setText(user.getEmail());
                            binding.etPhoneNumber.setText(user.getPhoneNumber());

                            if (user.getPhotoUrl() != null) {
                                Glide.with(MyProfile.this)
                                        .load(user.getPhotoUrl())
                                        .circleCrop()
                                        .into(binding.ivDisplayPhoto);
                            }
                        }
                    } else {
                        Toast.makeText(MyProfile.this, "No user data found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MyProfile.this, "Failed to load data.", Toast.LENGTH_SHORT).show());
    }

    private void toggleEditMode() {
        if (isEditing) {
            saveUserData();
            binding.ivEdit.setImageResource(R.drawable.ic_edit);
        } else {
            binding.ivEdit.setImageResource(R.drawable.ic_check);
        }
        isEditing = !isEditing;
        setFieldsOpen(isEditing);
    }

    private void setFieldsOpen(boolean isEditing) {
        binding.etFirstName.setClickable(isEditing);
        binding.etLastName.setClickable(isEditing);
        binding.etPhoneNumber.setClickable(isEditing);

        binding.etFirstName.setFocusable(isEditing);
        binding.etLastName.setFocusable(isEditing);
        binding.etPhoneNumber.setFocusable(isEditing);

        binding.fabPhoto.setVisibility(View.VISIBLE);
    }

    private void saveUserData() {
        String firstName = binding.etFirstName.getText().toString();
        String lastName = binding.etLastName.getText().toString();
        String phoneNumber = binding.etPhoneNumber.getText().toString();
        String photoUrl = selectedImageUri != null ? selectedImageUri.toString() : null;

        Map<String, Object> updatedUserData = new HashMap<>();
        updatedUserData.put("firstName", firstName);
        updatedUserData.put("lastName", lastName);
        updatedUserData.put("phoneNumber", phoneNumber);
        if (photoUrl != null) {
            updatedUserData.put("photoUrl", photoUrl);
        }

        userReference.update(updatedUserData)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(MyProfile.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(MyProfile.this, "Failed to update profile.", Toast.LENGTH_SHORT).show());
    }

    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_photo);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }

        TextView takePhoto = dialog.findViewById(R.id.tvTakePhoto);
        TextView choosePhoto = dialog.findViewById(R.id.tvChoosePhoto);
        TextView removePhoto = dialog.findViewById(R.id.tvRemovePhoto);

        takePhoto.setOnClickListener(v -> {
            openCamera();
            dialog.dismiss();
        });
        choosePhoto.setOnClickListener(v -> {
            openImagePicker();
            dialog.dismiss();
        });
        removePhoto.setOnClickListener(v -> {
            removePhoto();
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "com.advento.lucart.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Failed to create image file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e("CreateImageFile", "Error creating image file", e);
            return null;
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void removePhoto() {
        Glide.with(this)
                .load(R.drawable.ic_photo_placeholder)
                .circleCrop()
                .into(binding.ivDisplayPhoto);
        selectedImageUri = null;
        Toast.makeText(this, "Photo removed.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                selectedImageUri = data.getData();
                try {
                    // Directly use the URI to load the image
                    Glide.with(this)
                            .load(selectedImageUri)
                            .circleCrop()
                            .into(binding.ivDisplayPhoto);

                    // Optionally, upload the image if needed
                    uploadImageToFirebase(selectedImageUri);
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading image.", Toast.LENGTH_SHORT).show();
                    Log.e("ImagePicker", "Error loading URI", e);
                }
            }
        }
    }

    private void uploadImageToFirebase(Uri selectedImageUri) {
        if (selectedImageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_photos")
                    .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid() + ".jpg");

            storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Use a local variable to store the URI for Glide and Firestore update
                            final Uri finalUri = uri;
                            Glide.with(MyProfile.this)
                                    .load(finalUri)
                                    .circleCrop()
                                    .into(binding.ivDisplayPhoto);
                        }).addOnFailureListener(e -> Toast.makeText(MyProfile.this, "Failed to get image URL.", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(MyProfile.this, "Failed to upload image.", Toast.LENGTH_SHORT).show());
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}

