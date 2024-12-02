package com.advento.lucart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class fragment_cancelled extends Fragment {
    private RecyclerView recyclerViewCancelled;
    private TransactionAdapter cancelAdapter;

    public fragment_cancelled() {
        // Required empty public constructor
    }

    // Add the newInstance method
    public static fragment_cancelled newInstance() {
        return new fragment_cancelled();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cancelled, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewCancelled = view.findViewById(R.id.recyclerViewCancelled);

        // Get cancel list from SharedTransactionData
        List<Transaction> cancelList = SharedTransactionData.getInstance().getCancelList();

        // Set up RecyclerView if the cancel list is not empty
        if (!cancelList.isEmpty()) {
            cancelAdapter = new TransactionAdapter(requireContext(), cancelList);
            recyclerViewCancelled.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerViewCancelled.setAdapter(cancelAdapter);
        }
    }
}
