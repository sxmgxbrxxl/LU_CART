package com.advento.lucart;

import java.util.ArrayList;
import java.util.List;

public class SharedTransactionData {
    private static SharedTransactionData instance;
    private final List<Transaction> cancelList;

    // Private constructor for singleton pattern
    private SharedTransactionData() {
        cancelList = new ArrayList<>();
    }

    // Get the single instance
    public static synchronized SharedTransactionData getInstance() {
        if (instance == null) {
            instance = new SharedTransactionData();
        }
        return instance;
    }

    // Add transaction to cancel list
    public void addToCancelList(Transaction transaction) {
        cancelList.add(transaction);
    }

    // Get the cancel list
    public List<Transaction> getCancelList() {
        return cancelList;
    }

    // Clear the cancel list (optional, based on your use case)
    public void clearCancelList() {
        cancelList.clear();
    }
}
