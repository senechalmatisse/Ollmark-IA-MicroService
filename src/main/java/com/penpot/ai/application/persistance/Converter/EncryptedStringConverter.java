package com.penpot.ai.application.persistance.Converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.penpot.ai.application.service.EncryptionService;

import jakarta.persistence.*;

/**
 * Convertisseur JPA : chiffre avant INSERT/UPDATE,
 * déchiffre après SELECT.
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static EncryptionService encryptionService;

    /**
     * Sets the encryption service to be used for encryption/decryption operations.
     *
     * @param service the EncryptionService instance
     */
    @Autowired
    public void setEncryptionService(EncryptionService service) {
        EncryptedStringConverter.encryptionService = service;
    }

    /**
     * Converts the entity attribute value to the database column value by encrypting it.
     *
     * @param plaintext the plaintext value to encrypt
     * @return the encrypted value, or null if the input is null
     * @throws RuntimeException if encryption fails
     */
    @Override
    public String convertToDatabaseColumn(String plaintext){
        if (plaintext == null ) return null;
        try {
            return encryptionService.encrypt(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chiffrement de la clé API", e);
        }
    }

    /**
     * Converts the database column value to the entity attribute value by decrypting it.
     *
     * @param ciphertext the encrypted value to decrypt
     * @return the decrypted plaintext value, or null if the input is null
     * @throws RuntimeException if decryption fails
     */
    @Override
    public String convertToEntityAttribute(String ciphertext){
        if (ciphertext == null) return null;
        try {
            return encryptionService.decrypt(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du déchiffrement de la clé API", e);
        }
    }


}
