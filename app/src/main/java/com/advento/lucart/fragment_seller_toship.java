package com.advento.lucart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.advento.lucart.databinding.FragmentSellerToshipBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class fragment_seller_toship extends Fragment {

    private FragmentSellerToshipBinding binding;
    private FirebaseFirestore db;
    private List<Transaction> transactionsList = new ArrayList<>();
    private TransactionsAdapter adapter; // Adapter for RecyclerView

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSellerToshipBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        fetchTransactions();

        return binding.getRoot();
    }

    // Set up RecyclerView with adapter
    private void setupRecyclerView() {
        adapter = new TransactionsAdapter(getContext(), transactionsList, this::onMoveToReceiveClicked);
        binding.rvSellerTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSellerTransactions.setAdapter(adapter);
    }

    // Fetch transactions from Firestore
    private void fetchTransactions() {
        String currentBusinessId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        db.collection("transactions")
                .whereEqualTo("businessId", currentBusinessId)
                .whereEqualTo("status", "To Ship")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactionsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        transaction.setTransactionId(doc.getId());
                        transactionsList.add(transaction);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load transactions", Toast.LENGTH_SHORT).show());
    }

    // Handle move to 'To Receive' action
    private void onMoveToReceiveClicked(Transaction transaction) {
        db.collection("transactions")
                .document(transaction.getTransactionId())
                .update("status", "To Receive")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Transaction moved to 'To Receive'", Toast.LENGTH_SHORT).show();
                    fetchTransactions(); // Refresh the list
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update transaction", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
