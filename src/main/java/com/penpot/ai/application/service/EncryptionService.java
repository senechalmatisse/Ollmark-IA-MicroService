package com.penpot.ai.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Service for encrypting and decrypting data using AES/GCM encryption.
 * This service provides secure encryption and decryption operations
 * for sensitive data in the application.
 */
@Service
public class EncryptionService {

    private static final String ALGORITHM    = "AES/GCM/NoPadding";
    private static final int    IV_LENGTH    = 12;   
    private static final int    TAG_LENGTH   = 128; 

    private final SecretKey secretKey;

    /**
     * Constructs an EncryptionService with the provided master key.
     *
     * @param masterKeyHex the master key in hexadecimal format (must be 256 bits / 32 bytes)
     * @throws IllegalStateException if the master key is not 256 bits
     */
    public EncryptionService(@Value("${crypto.master-key}") String masterKeyHex) {
        byte[] key = HexFormat.of().parseHex(masterKeyHex);
        if (key.length != 32)
            throw new IllegalStateException("La master key doit faire 256 bits (32 bytes)");
        this.secretKey = new SecretKeySpec(key, "AES");
    }

    /**
     * Encrypts the given plaintext using AES/GCM encryption.
     *
     * @param plaintext the text to encrypt
     * @return the encrypted data encoded in Base64
     * @throws Exception if encryption fails
     */
    public String encrypt(String plaintext) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        
        byte[] payload = new byte[IV_LENGTH + ciphertext.length];
        System.arraycopy(iv, 0, payload, 0, IV_LENGTH);
        System.arraycopy(ciphertext, 0, payload, IV_LENGTH, ciphertext.length);

        return Base64.getEncoder().encodeToString(payload);
    }

    /**
     * Decrypts the given Base64 encoded encrypted data.
     *
     * @param base64Payload the encrypted data encoded in Base64
     * @return the decrypted plaintext
     * @throws Exception if decryption fails
     */
    public String decrypt(String base64Payload) throws Exception {
        byte[] payload = Base64.getDecoder().decode(base64Payload);

        byte[] iv  = Arrays.copyOfRange(payload, 0, IV_LENGTH);
        byte[] enc = Arrays.copyOfRange(payload, IV_LENGTH, payload.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));

        return new String(cipher.doFinal(enc), StandardCharsets.UTF_8);
    }
}