package com.st.flow;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface Encoder {

    Data encode(CardData card) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException;

    class Data {
        private String token;
        private String secretKey;

        public Data(String token, String secretKey) {
            this.token = token;
            this.secretKey = secretKey;
        }

        public String getToken() {
            return token;
        }

        public String getSecretKey() {
            return secretKey;
        }
    }

    class CardData {
        private String cardNumber;
        private String expirationDate;
        private String cvcNumber;

        public CardData(String cardNumber, String expirationDate, String cvcNumber) {
            this.cardNumber = cardNumber;
            this.expirationDate = expirationDate;
            this.cvcNumber = cvcNumber;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public String getExpirationDate() {
            return expirationDate;
        }

        public String getCvcNumber() {
            return cvcNumber;
        }
    }
}
