package com.st.proof.api;

import com.st.proof.Decoder;

public class Transaction {
    private AuthRequest transactionId;

    public Transaction(Decoder.CardData cardData) {
        this.transactionId = new AuthRequest(cardData.getCardNumber(), cardData.getExpirationDate(), cardData.getCvcNumber());
    }

    public AuthRequest getTransactionId() {
        return transactionId;
    }
}
