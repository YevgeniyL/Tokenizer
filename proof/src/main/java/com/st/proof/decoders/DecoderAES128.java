package com.st.proof.decoders;

import com.st.proof.Decoder;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DecoderAES128 implements Decoder {

    @Override
    public CardData decode(Decoder.Data data) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        byte[] encryptedTokenBase64 = decryptData(Base64.getDecoder().decode(data.getToken()), new SecretKeySpec(Base64.getDecoder().decode(data.getSecretKeyBase64()), "AES"));
        String token = new String(encryptedTokenBase64, StandardCharsets.UTF_8);
        String[] cardData = token.split(";");
        return new CardData(cardData[0],cardData[1],cardData[2]);
    }

    private static byte[] decryptData(byte[] rawMessage, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(rawMessage);
    }
}
