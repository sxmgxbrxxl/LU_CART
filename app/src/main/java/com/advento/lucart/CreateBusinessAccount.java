package com.advento.lucart;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityCreateBusinessAccountBinding;
import com.advento.lucart.models.Business;
import com.advento.lucart.models.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateBusinessAccount extends AppCompatActivity {

    ActivityCreateBusinessAccountBinding binding;
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

        binding = ActivityCreateBusinessAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        binding.fabPhoto.setOnClickListener(v -> showDialog());
        binding.btnSave.setOnClickListener(v -> saveBusinessAccount());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void saveBusinessAccount() {
        if (mAuth.getCurrentUser() == null) {
            showToast("User not authenticated");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String email = getIntent().getStringExtra("EMAIL");
        String password = getIntent().getStringExtra("PASSWORD");
        String businessName = binding.etBusinessName.getText().toString().trim();

        if (TextUtils.isEmpty(businessName)) {
            showToast("Please fill in all fields.");
            return;
        }

        if (photoUri == null) {
            showToast("Please choose a photo.");
            return;
        }

        uploadPhotoToFirebase(photoUri, userId, downloadUrl -> {
            String photoUrl = downloadUrl.toString();
            Business business = new Business(businessName, email, password, photoUrl);

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("business")
                    .document(userId)
                    .set(business)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Business information saved successfully");
                            startActivity(new Intent(CreateBusinessAccount.this, BusinessHome.class));
                        } else {
                            Log.e("BusinessUserInfo", "Failed to save business information: " + task.getException());
                            showToast("Failed to save business information");
                        }
                    });
        });
    }


    private void uploadPhotoToFirebase(Uri photoUri, String userId, OnSuccessListener<Uri> onSuccessListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference photoRef = storageRef.child("business_profile_photos/" + userId + ".jpg");

        photoRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(onSuccessListener))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseStorage", "Photo upload failed", e);
                    showToast("Photo upload failed");
                });
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
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void removePhoto() {
        Glide.with(this)
                .load(R.drawable.ic_photo_placeholder)
                .into(binding.ivDisplayPhoto);
        photoUri = null;
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
                openImagePicker();
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
                        .into(binding.ivDisplayPhoto);
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                Uri selectedImageUri = data.getData();
                photoUri = selectedImageUri;
                // Use Glide to load the selected image with circular crop
                Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop() // This applies the circular crop transformation
                        .into(binding.ivDisplayPhoto);
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}