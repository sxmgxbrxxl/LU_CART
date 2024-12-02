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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class fragment_to_ship extends Fragment {

    private static final String ARG_USER_TYPE = "userType"; // Either "buyer" or "seller"
    private String userType; // Holds the role passed to the fragment

    private TransactionAdapter transactionAdapter;
    private final List<Transaction> transactionList = new ArrayList<>();

    public fragment_to_ship() {
        // Required empty public constructor
    }

    public static fragment_to_ship newInstance(String param) {
        fragment_to_ship fragment = new fragment_to_ship();
        Bundle args = new Bundle();
        args.putString("param_key", param);
        fragment.setArguments(args);
        return fragment;
    }

    // Overloaded no-argument version
    public static fragment_to_ship newInstance() {
        return new fragment_to_ship();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userType = getArguments().getString(ARG_USER_TYPE); // Retrieve the user type
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_ship, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvToShipOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        transactionAdapter = new TransactionAdapter(getActivity(), transactionList);
        recyclerView.setAdapter(transactionAdapter);

        fetchToShipTransactions();

        return view;
    }

    private void fetchToShipTransactions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) {
            Toast.makeText(getActivity(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("transactions")
                .whereEqualTo("status", "To Ship") // Filter by "To Ship" status
                .whereEqualTo("userId", currentUserId) // Match the current user's ID as the buyer
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactionList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Transaction transaction = snapshot.toObject(Transaction.class);
                        if (transaction != null) {
                            transactionList.add(transaction); // Add the transaction to the list
                        }
                    }
                    transactionAdapter.notifyDataSetChanged(); // Notify the adapter
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error fetching orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
