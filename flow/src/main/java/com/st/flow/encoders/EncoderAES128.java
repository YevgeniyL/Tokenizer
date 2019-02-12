package com.st.flow.encoders;

import com.st.flow.Encoder;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncoderAES128 implements Encoder {
    private final SecretKey secretKey = generateSecretKey();

    public EncoderAES128() throws NoSuchAlgorithmException {
    }

    private static byte[] encode(byte[] rawMessage, SecretKey secretKey) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(rawMessage);
    }

    private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    @Override
    public Data encode(CardData card) throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {
        String token = card.getCardNumber() + ";" + card.getExpirationDate() + ";" + card.getCvcNumber();
        byte[] tokenInAes = encode(token.getBytes(StandardCharsets.UTF_8), secretKey);
        return new Data(
                Base64.getEncoder().withoutPadding().encodeToString(tokenInAes),
                Base64.getEncoder().withoutPadding().encodeToString(secretKey.getEncoded()));
    }
}
