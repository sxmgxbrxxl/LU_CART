package com.advento.lucart;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.advento.lucart.databinding.ActivityBusinessNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BusinessNotifications extends AppCompatActivity {

    private ActivityBusinessNotificationsBinding binding;
    private NotificationAdapter notificationAdapter;
    private final List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityBusinessNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        setSupportActionBar(binding.tbNotifications);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Drawable customBackButton = ContextCompat.getDrawable(this, R.drawable.ic_custom_back_activities);
        if (customBackButton != null) {
            customBackButton.setTint(ContextCompat.getColor(this, R.color.eleven_green));
            getSupportActionBar().setHomeAsUpIndicator(customBackButton);
        }

        requestNotificationPermission();

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        loadNotifications();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Notification permission already granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Notifications are enabled by default on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(notificationList);
        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewNotifications.setAdapter(notificationAdapter);
    }

    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference notificationsRef = db.collection("business")
                .document(userId)
                .collection("notifications");

        notificationsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    notificationList.clear();
                    for (QueryDocumentSnapshot document : value) {
                        try {
                            Notification notification = document.toObject(Notification.class);

                            Log.d("Notification", "Notification Loaded: " + notification.toString());

                            Long timestamp = document.getLong("timestamp");
                            if (timestamp != null) {
                                notification.setTimestamp(timestamp);
                            }

                            notificationList.add(notification);
                        } catch (RuntimeException e) {
                            Toast.makeText(this, "Error loading a notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    notificationAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}