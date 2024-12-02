package com.advento.lucart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellerToShipFragment extends Fragment {

    private static final String TAG = "SellerToShipFragment"; // For consistent logging
    private TransactionAdapter transactionAdapter;
    private final List<Transaction> transactionList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for SellerToShipFragment
        View view = inflater.inflate(R.layout.fragment_seller_to_ship, container, false);

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.rvSellerToShipOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Set up the adapter
        transactionAdapter = new TransactionAdapter(getActivity(), transactionList);
        recyclerView.setAdapter(transactionAdapter);

        // Fetch seller-specific transactions
        fetchSellerToShipTransactions();

        return view;
    }

    private void fetchSellerToShipTransactions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(getActivity(), "User not logged in.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: User not logged in.");
            return;
        }

        Log.d(TAG, "Fetching transactions for seller ID: " + currentUserId);

        db.collection("transactions")
                .whereEqualTo("status", "To Ship")
                .whereEqualTo("sellerId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactionList.clear();

                    // Log total number of documents retrieved
                    Log.d(TAG, "Total documents retrieved: " + queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            // Log full document data for debugging
                            Log.d(TAG, "Document ID: " + snapshot.getId());
                            Log.d(TAG, "Document Data: " + snapshot.getData());

                            try {
                                Transaction transaction = snapshot.toObject(Transaction.class);
                                if (transaction != null) {
                                    // Additional debug logging
                                    Log.d(TAG, "Seller ID in transaction: " + transaction.getSellerId());
                                    Log.d(TAG, "Status in transaction: " + transaction.getStatus());
                                    Log.d(TAG, "Cart Items count: " + (transaction.getCartItems() != null ? transaction.getCartItems().size() : "NULL"));

                                    transactionList.add(transaction);
                                } else {
                                    Log.w(TAG, "Null transaction for document: " + snapshot.getId());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to Transaction", e);
                            }
                        }

                        // Log the final list size
                        Log.d(TAG, "Final transactions list size: " + transactionList.size());

                        transactionAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "No transactions found for 'To Ship' status.");
                        Toast.makeText(getActivity(), "No orders to ship.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching transactions: ", e);
                    Toast.makeText(getActivity(), "Error fetching orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
