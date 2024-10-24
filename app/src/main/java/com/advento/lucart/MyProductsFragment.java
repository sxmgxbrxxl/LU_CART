package com.advento.lucart;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.advento.lucart.databinding.FragmentMyProductsBinding;

public class MyProductsFragment extends Fragment {

    private FragmentMyProductsBinding binding;
    private Uri imageUri; // Store the selected image URI
    private ImageView imageViewProduct; // Reference to the ImageView in the dialog

    public MyProductsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentMyProductsBinding.inflate(inflater, container, false);

        binding.ivaddProduct.setOnClickListener(v -> showAddProductDialog());

        return binding.getRoot();
    }

    private void showAddProductDialog() {
        // Inflate the dialog view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_addproduct_layout, null);

        // Find views in the dialog
        imageViewProduct = dialogView.findViewById(R.id.ivProductImage); // Store the ImageView reference
        Button buttonUploadPhoto = dialogView.findViewById(R.id.buttonUploadPhoto);
        EditText editTextProductName = dialogView.findViewById(R.id.editTextProductName);
        EditText editTextProductPrice = dialogView.findViewById(R.id.editTextProductPrice);
        EditText editTextProductDescription = dialogView.findViewById(R.id.editTextProductDescription);
        EditText editTextStockNumber = dialogView.findViewById(R.id.editTextStockNumber);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Product")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    // Handle the add product action here
                    String productName = editTextProductName.getText().toString();
                    String productPrice = editTextProductPrice.getText().toString();
                    String productDescription = editTextProductDescription.getText().toString();
                    String stockNumber = editTextStockNumber.getText().toString();

                    if (imageUri != null) {
                        uploadProduct(productName, productPrice, productDescription, stockNumber, imageUri);
                    } else {
                        Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        AlertDialog alertDialog = builder.create();

        // Handle upload photo button click
        buttonUploadPhoto.setOnClickListener(v -> openImagePicker());

        alertDialog.show();
    }

    private static final int PICK_IMAGE_REQUEST = 1;

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData(); // Store the selected image URI
            // Update the ImageView in the dialog with the selected image
            if (imageViewProduct != null) {
                imageViewProduct.setImageURI(imageUri);
            }
        }
    }

    private void uploadProduct(String productName, String productPrice, String productDescription, String stockNumber, Uri imageUri) {
        // Add your code here to upload the product data and the image to the database.
        // For example, you can use Firebase Storage to upload the image and then store the product details in your database.

        // Placeholder for upload logic
        Toast.makeText(getContext(), "Uploading product...", Toast.LENGTH_SHORT).show();

        // After successful upload, show a success message
        Toast.makeText(getContext(), "Product was successfully added to pending list", Toast.LENGTH_SHORT).show();
    }
}
