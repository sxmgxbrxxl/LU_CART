package com.advento.lucart;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.advento.lucart.databinding.FragmentBusinessHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BusinessHomeFragment extends Fragment {

    private FragmentBusinessHomeBinding binding;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBusinessHomeBinding.inflate(inflater, container, false);

        db = FirebaseFirestore.getInstance();

        loadGreetings();

        return binding.getRoot();
    }

    private void loadGreetings() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("business").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String businessName = task.getResult().getString("businessName");
                            String greetingMessage = (businessName != null && !businessName.isEmpty()) ?
                                    "Hello, " + businessName + "!" :
                                    "Hello, User!";

                            if (isAdded()) {
                                applyTypewriterEffect(greetingMessage, binding.tvGreetings, () -> loadSubtitle());
                            }
                        } else {
                            if (isAdded()) {
                                applyTypewriterEffect("Hello, Seller!", binding.tvGreetings, () -> loadSubtitle());
                            }
                        }
                    });
        } else {
            if (isAdded()) {
                applyTypewriterEffect("Hello, Seller!", binding.tvGreetings, () -> loadSubtitle());
            }
        }
    }

    private Handler handler = new Handler();
    private Runnable typeWriterRunnable;

    private void applyTypewriterEffect(String message, TextView textView, Runnable onComplete) {
        final String finalMessage = message;
        final int[] index = {0};

        handler.removeCallbacks(typeWriterRunnable);

        typeWriterRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < finalMessage.length()) {
                    if (isAdded()) {
                        textView.append(String.valueOf(finalMessage.charAt(index[0])));
                        index[0]++;
                    }
                    handler.postDelayed(this, 70);
                } else {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        };

        handler.post(typeWriterRunnable);
    }

    private void loadSubtitle() {
        String question = "Here's your shop performance";
        if (isAdded()) {
            applyTypewriterEffect(question, binding.tvSubtitle, null);
        }
    }
}