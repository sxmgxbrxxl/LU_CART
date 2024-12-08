package com.advento.lucart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class fragment_to_receive extends Fragment {

    private TransactionAdapter adapter; // Use the customer-specific adapter
    private List<Transaction> toReceiveTransactions;
    private FirebaseFirestore db;

    public fragment_to_receive() {
        // Required empty public constructor
    }

    public static fragment_to_receive newInstance() {
        return new fragment_to_receive();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_to_receive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvToReceiveOrders = view.findViewById(R.id.rvToShipOrders);
        rvToReceiveOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        toReceiveTransactions = new ArrayList<>();
        adapter = new TransactionAdapter(requireContext(), toReceiveTransactions); // Initialize the customer adapter
        rvToReceiveOrders.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchToReceiveTransactions();
    }

    private void fetchToReceiveTransactions() {
        db.collection("transactions")
                .whereEqualTo("status", "To Receive") // Fetch transactions with "To Receive" status
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    toReceiveTransactions.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        transaction.setTransactionId(doc.getId());
                        toReceiveTransactions.add(transaction);
                    }
                    adapter.notifyDataSetChanged(); // Notify adapter of data changes
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load transactions", Toast.LENGTH_SHORT).show()
                );
    }
}
