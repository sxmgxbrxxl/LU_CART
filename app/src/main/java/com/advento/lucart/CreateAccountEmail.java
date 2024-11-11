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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityCreateAccountEmailBinding;
import com.advento.lucart.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;
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
    private FirebaseDatabase db;
    private DatabaseReference reference;
    private Uri photoUri;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityCreateAccountEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        binding.btnSave.setOnClickListener(v -> saveUserInfo());
        binding.circularImageView.setOnClickListener(v -> showImagePickerOptions());
    }

    // Add this method in your class
    private void uploadPhotoToFirebase(Uri photoUri, String userId, OnSuccessListener<Uri> onSuccessListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference photoRef = storageRef.child("profile_photos/" + userId + ".jpg");

        photoRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(onSuccessListener))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseStorage", "Photo upload failed", e);
                    showToast("Photo upload failed");
                });
    }

    private void saveUserInfo() {
        if (mAuth.getCurrentUser() == null) {
            showToast("User not authenticated");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String email = getIntent().getStringExtra("EMAIL");
        String password = getIntent().getStringExtra("PASSWORD");
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String birthday = binding.etBirthday.getText().toString().trim();
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            showToast("Please fill in all fields.");
            return;
        }

        if (photoUri == null) {
            showToast("Please choose a photo.");
            return;
        }

        // Upload photo to Firebase Storage and get the URL
        uploadPhotoToFirebase(photoUri, userId, downloadUrl -> {
            String photoUrl = downloadUrl.toString();
            User user = new User(firstName, lastName, email, password, birthday, phoneNumber, photoUrl);

            db = FirebaseDatabase.getInstance("https://lu-cart-firebase-default-rtdb.asia-southeast1.firebasedatabase.app");
            reference = db.getReference("users");

            reference.child(userId).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showToast("User information saved successfully");
                    startActivity(new Intent(CreateAccountEmail.this, Home.class));
                } else {
                    Log.e("SaveUserInfo", "Failed to save user information: " + task.getException());
                    showToast("Failed to save user information");
                }
            });
        });
    }

    private void showImagePickerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                }
            } else if (which == 1) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, GALLERY_REQUEST_CODE);
                }
            } else if (which == 2) {
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
