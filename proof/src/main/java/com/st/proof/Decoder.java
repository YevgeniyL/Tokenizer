package com.st.proof;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface Decoder {

    CardData decode(Data data) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException;

    class Data {
        private String token;
        private String secretKeyBase64;

        public Data(String token, String secretKeyBase64) {
            this.token = token;
            this.secretKeyBase64 = secretKeyBase64;
        }

        public String getToken() {
            return token;
        }

        public String getSecretKeyBase64() {
            return secretKeyBase64;
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
