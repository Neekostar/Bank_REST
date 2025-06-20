package org.example.neekostar.bank.util;

import jakarta.annotation.PostConstruct;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CardEncryptor {

    private final String encryptionKey;
    private static SecretKeySpec secretKey;

    @Autowired
    public CardEncryptor(@Value("${card.encryption.key}") String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    @PostConstruct
    public void init() {
        if (encryptionKey == null || encryptionKey.length() != 16) {
            throw new IllegalStateException("encryptionKey must be 16 symbols (AES-128)");
        }
        this.secretKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    }

    public String encrypt(String cardNumber) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(cardNumber.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Card encryption error", e);
        }
    }

    public static String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
        } catch (Exception e) {
            throw new RuntimeException("Card decryption error", e);
        }
    }

    public static String maskCardNumber(String decrypted) {
        return "**** **** **** " + decrypted.substring(12);
    }
}
