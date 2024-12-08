package com.advento.lucart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class fragment_completed extends Fragment {

    private TransactionAdapter adapter; // Use the customer-specific adapter
    private List<Transaction> completedTransactions;
    private FirebaseFirestore db;

    public fragment_completed() {
        // Required empty public constructor
    }

    public static fragment_completed newInstance() {
        return new fragment_completed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_completed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvCompletedOrders = view.findViewById(R.id.rvToShipOrders); // Ensure the ID is correct
        rvCompletedOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        completedTransactions = new ArrayList<>();
        adapter = new TransactionAdapter(requireContext(), completedTransactions); // Use customer adapter
        rvCompletedOrders.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchCompletedTransactions();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchCompletedTransactions() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID

        db.collection("transactions")
                .whereEqualTo("status", "Completed") // Filter by "Completed" status
                .whereEqualTo("userId", currentUserId) // Fetch only transactions for the current user
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    completedTransactions.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        transaction.setTransactionId(doc.getId());
                        completedTransactions.add(transaction);
                        Log.d("FirestoreData", "Transaction: " + transaction.toString());
                    }
                    adapter.notifyDataSetChanged(); // Notify adapter of data updates
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load transactions", Toast.LENGTH_SHORT).show()
                );
    }
}
