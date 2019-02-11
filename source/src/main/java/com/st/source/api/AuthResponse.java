package com.st.source.api;

public class AuthResponse {
    private boolean success;
    private String transactionId;

    public AuthResponse(boolean success, String transactionId) {
        this.success = success;
        this.transactionId = transactionId;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
