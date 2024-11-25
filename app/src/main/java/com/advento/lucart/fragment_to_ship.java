package com.advento.lucart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class fragment_to_ship extends Fragment {

    private TransactionAdapter transactionAdapter;  // Adapter for the RecyclerView
    private final List<Transaction> transactionList = new ArrayList<>();  // List to store fetched transactions

    public fragment_to_ship() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_to_ship, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvToShipOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize the adapter
        transactionAdapter = new TransactionAdapter(getActivity(), transactionList);
        recyclerView.setAdapter(transactionAdapter);

        // Fetch transactions with "To Ship" status from Firestore
        fetchToShipTransactions();

        return view;
    }

    private void fetchToShipTransactions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("transactions")
                .whereEqualTo("status", "To Ship") // Firestore query for "To Ship" status
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            Transaction transaction = snapshot.toObject(Transaction.class);
                            transactionList.add(transaction); // Add the transaction to the list
                        }
                        // Notify the adapter that data has changed
                        transactionAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), "No orders to ship.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error fetching orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
