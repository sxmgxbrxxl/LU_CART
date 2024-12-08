package com.advento.lucart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class fragment_seller_toreceive extends Fragment implements TransactionsAdapter.OnMoveToReceiveListener {

    private FirebaseFirestore db;
    private final List<Transaction> transactionsList = new ArrayList<>();
    private TransactionsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_seller_toreceive, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        RecyclerView recyclerView = rootView.findViewById(R.id.rvSellerTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionsAdapter(getContext(), transactionsList, this); // Pass 'this' as listener
        recyclerView.setAdapter(adapter);

        // Fetch transactions
        fetchTransactions();

        return rootView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchTransactions() {
        String currentBusinessId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        db.collection("transactions")
                .whereEqualTo("businessId", currentBusinessId)
                .whereEqualTo("status", "To Receive")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactionsList.clear(); // Clear old data
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        transaction.setTransactionId(doc.getId());
                        transactionsList.add(transaction);
                    }
                    adapter.notifyDataSetChanged(); // Notify adapter to refresh RecyclerView
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load transactions", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMoveToReceiveClicked(Transaction transaction) {
        String newStatus = "To Receive".equals(transaction.getStatus()) ? "Completed" : "To Receive";

        db.collection("transactions")
                .document(transaction.getTransactionId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    // Refresh data after update
                    fetchTransactions();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show());
    }
}
