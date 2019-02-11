package com.st.proof.api;

public class Transaction {
    private AuthRequest transactionId;

    public Transaction(AuthRequest authRequest) {
        this.transactionId = authRequest;
    }

    public AuthRequest getTransactionId() {
        return transactionId;
    }
}
