package com.advento.lucart;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyProfile extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    private ActivityMyProfileBinding binding;
    private FirebaseDatabase db;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private boolean isEditing = false;
    private Uri selectedImageUri;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            db = FirebaseDatabase.getInstance("https://lu-cart-firebase-default-rtdb.asia-southeast1.firebasedatabase.app/");
            reference = db.getReference("users").child(user.getUid());
        } else {
            Toast.makeText(this, "User not signed in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchUserData();

        binding.ivEdit.setOnClickListener(v -> toggleEditMode());
        binding.fabPhoto.setOnClickListener(v -> showDialog());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
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
                                    .into(binding.ivDisplayPhoto);
                        }
                    }
                } else {
                    Toast.makeText(MyProfile.this, "No user data found.", Toast.LENGTH_SHORT).show();
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
            binding.ivEdit.setImageResource(R.drawable.ic_edit);
        } else {
            binding.ivEdit.setImageResource(R.drawable.ic_check);
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

    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_photo);

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

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            uploadImageToFirebase(photoUri);
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            uploadImageToFirebase(selectedImageUri);
        }
    }

    private void uploadImageToFirebase(Uri selectedImageUri) {
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
