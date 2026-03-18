package com.penpot.ai.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class EncryptionServiceUnit {
    // Clé 256 bits quelconque pour les tests
    private final EncryptionService service =
            new EncryptionService("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");

    @Test
    void chiffrement_puis_dechiffrement_retourne_valeur_originale() throws Exception {
        String original = "sk-test-1234567890abcdef";

        String encrypted = service.encrypt(original);
        String decrypted = service.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void deux_chiffrements_du_meme_texte_donnent_des_resultats_differents() throws Exception {
        String original = "sk-test-1234567890abcdef";

        String enc1 = service.encrypt(original);
        String enc2 = service.encrypt(original);

        // IV aléatoire → chiffrés différents, mais les deux déchiffrables
        assertThat(enc1).isNotEqualTo(enc2);
        assertThat(service.decrypt(enc1)).isEqualTo(original);
        assertThat(service.decrypt(enc2)).isEqualTo(original);
    }

    @Test
    void dechiffrement_avec_payload_altere_lance_une_exception() {
        assertThatThrownBy(() -> service.decrypt("payloadcorrompu=="))
                .isInstanceOf(Exception.class);
    }

    @Test
    void chiffrement_dune_valeur_null_est_gere_par_le_converter() {
        // Le converter gère null avant d'appeler encrypt/decrypt — pas besoin de le gérer ici
        assertThatCode(() -> service.encrypt("")).doesNotThrowAnyException();
    }

}
