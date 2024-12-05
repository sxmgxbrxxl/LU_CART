package com.advento.lucart;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityCreatePersonalAccountBinding;
import com.advento.lucart.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreatePersonalAccount extends AppCompatActivity {

    private ActivityCreatePersonalAccountBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private Uri photoUri;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityCreatePersonalAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.fabPhoto.setOnClickListener(v -> showDialog());
        binding.btnSave.setOnClickListener(v -> saveUserInfo());
        binding.btnOpenCalendar.setOnClickListener(v -> showDatePicker());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            binding.btnOpenCalendar.setText(date);
        }, year, month, day);

        datePickerDialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
        String birthday = binding.btnOpenCalendar.getText().toString().trim();
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            showToast("Please fill in all fields.");
            return;
        }

        if (photoUri == null) {
            showToast("Please choose a photo.");
            return;
        }

        Dialog progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.dialog_loading);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        progressDialog.show();

        uploadPhotoToFirebase(photoUri, userId, downloadUrl -> {
            String photoUrl = downloadUrl.toString();
            User user = new User(firstName, lastName, email, password, birthday, phoneNumber, photoUrl);

            // Save user data to Firestore
            DocumentReference userDoc = firestore.collection("users").document(userId);
            userDoc.set(user).addOnCompleteListener(task -> {

                progressDialog.dismiss();

                if (task.isSuccessful()) {
                    showToast("User information saved successfully");
                    startActivity(new Intent(CreatePersonalAccount.this, Home.class));
                } else {
                    Log.e("SaveUserInfo", "Failed to save user information: " + task.getException());
                    showToast("Failed to save user information");
                }
            });
        });
    }

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
            return File.createTempFile(imageFileName, ".jpg", storageDir);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Glide.with(this)
                        .load(photoUri)
                        .circleCrop()
                        .into(binding.ivDisplayPhoto);
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                Uri selectedImageUri = data.getData();
                photoUri = selectedImageUri;
                Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop()
                        .into(binding.ivDisplayPhoto);
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
