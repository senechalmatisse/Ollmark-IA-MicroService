package com.penpot.ai.application.persistance.Repositories;

import com.penpot.ai.application.persistance.Entity.AiModelConfig;
import com.penpot.ai.application.persistance.Entity.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.penpot.ai.application.service.EncryptionService;
import com.penpot.ai.application.persistance.Converter.EncryptedStringConverter;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({EncryptionService.class, EncryptedStringConverter.class})
class AiModelConfigRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired AiModelConfigRepository repository;

    @Test
    void la_cle_api_est_chiffree_en_base_et_dechiffree_en_lecture() {
       
        Project project = em.persist(new Project("Test Project"));
        em.flush();

        AiModelConfig config = new AiModelConfig(project, "gpt-4o", "openai");
        config.setModelApiKey("sk-test-1234567890abcdef");

        // ---  sauvegarde ---
        AiModelConfig saved = repository.save(config);
        em.flush();
        em.clear(); 

        // ---  lecture ---
        AiModelConfig loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getModelApiKey()).isEqualTo("sk-test-1234567890abcdef");
    }

    @Test
    void la_valeur_brute_en_base_est_differente_du_plaintext() {
        Project project = em.persist(new Project( "Test Project"));
        em.flush();

        AiModelConfig config = new AiModelConfig(project, "claude-3", "anthropic");
        config.setModelApiKey("sk-ant-supersecret");
        repository.save(config);
        em.flush();

        // Lecture directe SQL — doit retourner le chiffré, pas le plaintext
        String rawValue = (String) em.getEntityManager()
                .createNativeQuery("SELECT model_api_key FROM ai_model_config LIMIT 1")
                .getSingleResult();

        assertThat(rawValue).isNotEqualTo("sk-ant-supersecret");
        assertThat(rawValue).isBase64(); 
    }

    @Test
    void find_by_project_id_fonctionne_et_dechiffre_correctement() {
        Project project = em.persist(new Project("Test Project"));
        em.flush();

        AiModelConfig config = new AiModelConfig(project, "gemini-pro", "google");
        config.setModelApiKey("AIza-test-key");
        repository.save(config);
        em.flush();
        em.clear();

        AiModelConfig found = repository.findByProjectId(project.getId()).orElseThrow();
        assertThat(found.getModelApiKey()).isEqualTo("AIza-test-key");
    }
}
