package com.advento.lucart;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.content.pm.PackageManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.advento.lucart.databinding.ActivityCreateAccountEmailBinding;
import com.advento.lucart.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateAccountEmail extends AppCompatActivity {

    private ActivityCreateAccountEmailBinding binding;
    private FirebaseAuth mAuth;
    private Uri photoUri;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityCreateAccountEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnSave.setOnClickListener(v -> saveUserInfo());
        binding.circularImageView.setOnClickListener(v -> showImagePickerOptions());
    }

    private void saveUserInfo() {
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String email = mAuth.getCurrentUser().getEmail(); // Get the user's email
        String userId = mAuth.getCurrentUser().getUid(); // Get the user's ID

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || photoUri == null) {
            showToast("Please fill in all fields and choose a photo.");
            return;
        }

        // Assuming you have a method to upload the photo and get the URL
        uploadPhotoAndSaveToDatabase(photoUri, userId, firstName, lastName, email);
    }

    private void uploadPhotoAndSaveToDatabase(Uri photoUri, String userId, String firstName, String lastName, String email) {
        if (photoUri == null) {
            Log.e("UploadPhoto", "Photo URI is null");
            showToast("Photo URI is null");
            return;
        }

        Log.d("UploadPhoto", "Uploading photo from URI: " + photoUri.toString());

        // Create a reference to Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_photos/" + userId + ".jpg");
        Log.d("UploadPhoto", "Uploading photo to: " + storageReference.getPath());

        storageReference.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();
                        Log.d("UploadPhoto", "Photo uploaded successfully: " + photoUrl);
                        saveUserToDatabase(userId, firstName, lastName, email, photoUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("UploadPhoto", "Failed to upload photo: " + e.getMessage());
                    showToast("Failed to upload photo: " + e.getMessage());
                });
    }

    private void saveUserToDatabase(String userId, String firstName, String lastName, String email, String photoUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        User user = new User(firstName, lastName, email, photoUrl); // Create a User object

        databaseReference.setValue(user)
                .addOnSuccessListener(aVoid -> {
                    showToast("User info saved successfully");
                    // You can navigate to another activity here if needed
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to save user info: " + e.getMessage());
                });
    }


    private void showImagePickerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Open Camera
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                }
            } else if (which == 1) {
                // Open Gallery
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, GALLERY_REQUEST_CODE);
                }
            } else if (which == 2) {
                // Remove Photo
                removePhoto();
            }
        });
        builder.show();
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
                showToast("Failed to create image file");
            }
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File file = File.createTempFile(imageFileName, ".jpg", storageDir);
            Log.d("CreateImageFile", "File created: " + file.getAbsolutePath()); // Log the path
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    private void removePhoto() {
        Glide.with(this)
                .load(R.drawable.ic_placeholder) // Load the default image
                .into(binding.circularImageView);
        photoUri = null; // Clear the photo URI
        showToast("Photo removed");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int i = 0; i < permissions.length; i++) {
            Log.d("PermissionResult", "Permission: " + permissions[i] + ", Result: " + grantResults[i]);
        }

        if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                showToast("Gallery permission denied");
            }
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                showToast("Camera permission denied");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Glide.with(this)
                        .load(photoUri)
                        .circleCrop() // This applies the circular crop transformation
                        .into(binding.circularImageView);
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                Uri selectedImageUri = data.getData();
                photoUri = selectedImageUri;
                // Use Glide to load the selected image with circular crop
                Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop() // This applies the circular crop transformation
                        .into(binding.circularImageView);
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Exit?")
                .setMessage("Are you sure you want to go back? Your progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
