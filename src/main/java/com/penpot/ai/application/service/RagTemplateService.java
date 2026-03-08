package com.penpot.ai.application.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.model.MarketingTemplate;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service applicatif responsable de la mise en œuvre de l'architecture RAG (Retrieval-Augmented Generation) 
 * pour la gestion et la recherche de modèles marketing.
 * <p>
 * Ce composant orchestre l'intégralité du cycle de vie des modèles, depuis leur chargement initial 
 * jusqu'à leur restitution via des requêtes de similarité. Le catalogue est constitué de 24 fichiers 
 * JSON, couvrant systématiquement 6 thématiques (mer, légumes, viande, boulangerie, fruits, fromage) 
 * déclinées en 4 formats (poster_a4, social_media_post, flyer_a5, social_media_story).
 * </p>
 * <p>
 * Par conception, chaque modèle JSON adopte une structure directe et simplifiée. En effet, les 
 * couleurs hexadécimales sont intégrées nativement dans les paramètres, le nombre d'éléments visuels 
 * est plafonné à 10 pour respecter les contraintes du modèle (qwen3:8b), et l'utilisation des 
 * espaces réservés (placeholders) textuels est strictement limitée à 3 ou 4 variables clés 
 * (ex. TITRE_PRODUIT, PRIX, NOM_MARQUE).
 * </p>
 * <p>
 * L'architecture s'articule autour de quatre phases principales :
 * <ul>
 *   <li><b>Chargement :</b> Lecture des fichiers JSON depuis le répertoire des ressources.</li>
 *   <li><b>Vectorisation :</b> Conversion des modèles en vecteurs d'encastrement (embeddings) via Ollama.</li>
 *   <li><b>Stockage :</b> Persistance des documents vectorisés au sein d'un VectorStore en mémoire.</li>
 *   <li><b>Recherche :</b> Identification et extraction des modèles pertinents par calcul de similarité.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagTemplateService {

    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;
    private final EmbeddingCacheService embeddingCache;

    @Value("${penpot.ai.rag.templates-path}")
    private String templatesPath;

    @Value("${penpot.ai.rag.similarity-threshold:0.5}")
    private double similarityThreshold;

    @Value("${penpot.ai.rag.top-k:3}")
    private int topK;

    /**
     * Contrôle l'indexation au démarrage.
     * <p>{@code true} (défaut) = comportement normal, les templates sont chargés et vectorisés.</p>
     * <p>{@code false} = chargement des métadonnées uniquement, sans appel à l'EmbeddingModel.
     */
    @Value("${penpot.ai.rag.eager-embedding:true}")
    private boolean eagerEmbedding;

    /**
     * Registre en mémoire (cache) des modèles chargés, permettant un accès direct et optimisé
     * lors des opérations de restitution qui ne nécessitent pas de calcul de similarité. 
     */
    private final Map<String, MarketingTemplate> templatesCache = new HashMap<>();

    /** Initialise le service RAG lors du démarrage de l'application. */
    @PostConstruct
    public void init() {
        try {
            if (eagerEmbedding) {
                loadAndIndexTemplates();
                log.info("RAG initialized — {} templates loaded and indexed (6 themes × 4 formats)",
                        templatesCache.size());
            } else {
                loadTemplatesWithoutIndexing();
                log.info("RAG initialized (no embedding) — {} templates loaded (eager-embedding=false)",
                        templatesCache.size());
            }
        } catch (Exception e) {
            log.error("Failed to initialize RAG Template Service", e);
        }
    }

    /**
     * Parcourt le répertoire des ressources configuré, instancie les modèles JSON et 
     * procède à leur indexation au sein du magasin vectoriel.
     * 
     * @throws IOException Si une erreur d'entrée/sortie survient lors de l'accès aux fichiers physiques.
     */
    private void loadAndIndexTemplates() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(templatesPath);

        log.info("Loading {} template files from {}", resources.length, templatesPath);
        List<Document> documents = new ArrayList<>();

        for (Resource resource : resources) {
            try {
                MarketingTemplate template = objectMapper.readValue(
                        resource.getInputStream(), MarketingTemplate.class);

                templatesCache.put(template.getId(), template);
                documents.add(createDocument(template));

                log.debug("Loaded: {} | type: {} | elements: {}",
                        template.getId(), template.getType(),
                        template.getLayoutStructure() != null
                                ? template.getLayoutStructure().size() : 0);
            } catch (Exception e) {
                log.error("Failed to load template from {}: {}",
                        resource.getFilename(), e.getMessage());
            }
        }

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            log.info("Indexed {} templates in VectorStore", documents.size());
        }
    }

    /**
     * Charge uniquement les métadonnées des templates, sans vectorisation.
     */
    private void loadTemplatesWithoutIndexing() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(templatesPath);

        log.info("Loading {} template files (metadata only) from {}", resources.length, templatesPath);

        for (Resource resource : resources) {
            try {
                MarketingTemplate template = objectMapper.readValue(
                        resource.getInputStream(), MarketingTemplate.class);
                templatesCache.put(template.getId(), template);
                log.debug("Loaded (no index): {}", template.getId());
            } catch (Exception e) {
                log.error("Failed to load template from {}: {}",
                        resource.getFilename(), e.getMessage());
            }
        }
    }

    /**
     * Transforme un modèle métier en un document compatible avec le magasin vectoriel.
     * <p>
     * Le processus consiste à concaténer les propriétés descriptives clés (identifiant, type, 
     * description, balises et structure visuelle) sous forme de chaîne de caractères. Ainsi, 
     * le VectorStore pourra générer et gérer les embeddings de manière autonome lors de 
     * l'insertion.
     * </p>
     *
     * @param template L'entité {@link MarketingTemplate} à convertir.
     * @return Une instance de {@link Document} encapsulant le contenu textuel et les métadonnées.
     */
    private Document createDocument(MarketingTemplate template) {
        StringBuilder content = new StringBuilder();
        content.append("Template: ").append(template.getId()).append("\n");
        content.append("Type: ").append(template.getType()).append("\n");
        content.append("Description: ").append(template.getDescription()).append("\n");

        if (template.getTags() != null && !template.getTags().isEmpty()) {
            content.append("Keywords: ").append(String.join(", ", template.getTags())).append("\n");
        }

        if (template.getLayoutStructure() != null) {
            content.append("Visual elements: ");
            template.getLayoutStructure().forEach(el -> {
                Object name = el.get("element");
                if (name != null) content.append(name).append(", ");
            });
            content.append("\n");
        }

        log.debug("Indexed '{}' — {} chars", template.getId(), content.length());
        return new Document(
            content.toString(),
            Map.of("id", template.getId(), "type", template.getType())
        );
    }

    /**
     * Exécute une recherche sémantique afin d'identifier les modèles les plus pertinents 
     * au regard d'une requête formulée en langage naturel.
     * <p>
     * Le mécanisme exploite un cache d'embeddings pour optimiser le temps de traitement de la requête. 
     * Ensuite, il interroge le magasin vectoriel en appliquant les seuils de tolérance et les limites 
     * de résultats définis dans la configuration applicative.
     * </p>
     *
     * @param query La chaîne de caractères représentant l'intention de l'utilisateur (ex. "social media post for product launch").
     * @return Une liste ordonnée d'objets {@link MarketingTemplate} correspondant aux critères de pertinence.
     */
    public List<MarketingTemplate> searchTemplates(String query) {
        log.info("Searching templates for: '{}'", query);
        long start = System.currentTimeMillis();

        embeddingCache.embedQuery(query);
        SearchRequest request = SearchRequest.builder()
            .query(query)
            .similarityThreshold(similarityThreshold)
            .topK(topK)
            .build();

        List<Document> results = vectorStore.similaritySearch(request);
        log.info("Found {} templates in {}ms", results.size(),
                System.currentTimeMillis() - start);

        return results.stream()
            .map(doc -> templatesCache.get(doc.getMetadata().get("id")))
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Extrait un modèle spécifique de manière déterministe depuis le registre en mémoire.
     *
     * @param templateId L'identifiant unique du modèle recherché.
     * @return Un {@link Optional} contenant le modèle s'il existe, ou vide le cas échéant.
     */
    public Optional<MarketingTemplate> getTemplateById(String templateId) {
        return Optional.ofNullable(templatesCache.get(templateId));
    }

    /**
     * Filtre et restitue l'ensemble des modèles appartenant à une catégorie fonctionnelle précise.
     *
     * @param type La typologie recherchée (ex. "social_media_post", "email").
     * @return Une liste de modèles validant le critère de type.
     */
    public List<MarketingTemplate> getTemplatesByType(String type) {
        return templatesCache.values().stream()
            .filter(t -> type.equalsIgnoreCase(t.getType()))
            .toList();
    }

    /**
     * Filtre et restitue l'ensemble des modèles associés à un marqueur de classification (tag) spécifique.
     *
     * @param tag L'étiquette de classification recherchée.
     * @return Une liste de modèles possédant le marqueur indiqué.
     */
    public List<MarketingTemplate> getTemplatesByTag(String tag) {
        return templatesCache.values().stream()
            .filter(t -> t.getTags() != null && t.getTags().contains(tag))
            .toList();
    }

    /**
     * Fournit une extraction exhaustive du catalogue de modèles actuellement en mémoire.
     *
     * @return Une nouvelle liste contenant toutes les instances de {@link MarketingTemplate} disponibles.
     */
    public List<MarketingTemplate> getAllTemplates() {
        return new ArrayList<>(templatesCache.values());
    }

    /**
     * Restitue la volumétrie totale des modèles actuellement indexés par le service.
     *
     * @return Un entier représentant le nombre de modèles chargés.
     */
    public int getTemplateCount() {
        return templatesCache.size();
    }

    /**
     * Compile et restitue la liste dédoublonnée des typologies de modèles gérées par le système.
     *
     * @return Un ensemble (Set) de chaînes de caractères recensant les types disponibles.
     */
    public Set<String> getAvailableTypes() {
        return templatesCache.values().stream()
            .map(MarketingTemplate::getType)
            .collect(Collectors.toSet());
    }

    /**
     * Compile et restitue l'inventaire complet et dédoublonné des étiquettes (tags) utilisées au sein du catalogue.
     *
     * @return Un ensemble (Set) regroupant toutes les balises associées aux modèles.
     */
    public Set<String> getAvailableTags() {
        return templatesCache.values().stream()
            .filter(t -> t.getTags() != null)
            .flatMap(t -> t.getTags().stream())
            .collect(Collectors.toSet());
    }
}