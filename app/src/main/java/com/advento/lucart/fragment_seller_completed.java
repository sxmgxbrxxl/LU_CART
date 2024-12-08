package com.advento.lucart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class fragment_seller_completed extends Fragment {

    private TransactionsAdapter adapter;
    private List<Transaction> completedTransactions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seller_completed, container, false);

        // Initialize RecyclerView and the list of completed transactions
        RecyclerView recyclerView = view.findViewById(R.id.rvSellerTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Vertical list

        completedTransactions = new ArrayList<>();
        adapter = new TransactionsAdapter(getContext(), completedTransactions, new TransactionsAdapter.OnMoveToReceiveListener() {
            @Override
            public void onMoveToReceiveClicked(Transaction transaction) {
                // Handle the Move to Receive button click if needed
            }
        });

        recyclerView.setAdapter(adapter);

        // Fetch completed transactions from Firestore
        fetchCompletedTransactions();

        return view;
    }

    private void fetchCompletedTransactions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query to get all completed transactions
        db.collection("transactions")
                .whereEqualTo("status", "Completed") // Fetch only completed transactions
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<Transaction> transactions = queryDocumentSnapshots.toObjects(Transaction.class);
                        completedTransactions.clear(); // Clear the list before adding new items
                        completedTransactions.addAll(transactions); // Add fetched transactions
                        adapter.notifyDataSetChanged(); // Notify the adapter to update the UI
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }
}
