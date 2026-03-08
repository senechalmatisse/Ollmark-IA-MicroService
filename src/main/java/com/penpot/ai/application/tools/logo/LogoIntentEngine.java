package com.penpot.ai.application.tools.logo;

import com.penpot.ai.core.domain.logo.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Moteur d'analyse sémantique et stratégique pour la conception de logos.
 * <p>
 * Cette classe a pour mission d'interpréter les spécifications brutes fournies par l'utilisateur 
 * (nom de marque, slogan, style) afin de les traduire en décisions de design concrètes.
 */
@Component
public class LogoIntentEngine {

    /**
     * Analyse la spécification du logo pour déterminer l'intention créative globale.
     * <p>
     * Cette méthode transforme les mots-clés extraits du prompt (en français ou anglais) en 
     * un objet {@link LogoIntent}. La logique de décision s'articule autour de cinq piliers :
     * </p>
     * <ul>
     * <li><b>Ambiance :</b> Détection d'un univers "Startup" ou "Traditionnel".</li>
     * <li><b>Géométrie :</b> Définition du rayon de courbure (Border Radius) selon l'image de marque.</li>
     * <li><b>Typographie :</b> Choix de l'épaisseur des polices en fonction du style sélectionné.</li>
     * <li><b>Chromatie :</b> Décision sur l'usage de dégradés pour le dynamisme.</li>
     * <li><b>Proportions :</b> Ajustement de l'échelle du symbole par rapport à la longueur du nom.</li>
     * </ul>
     *
     * @param spec L'objet {@link LogoSpec} contenant les données sources (nom, slogan, style).
     * @return Une instance de {@link LogoIntent} regroupant les décisions de design calculées.
     */
    public LogoIntent analyze(LogoSpec spec) {
        String brandName = Optional.ofNullable(spec.getBrandName()).orElse("").toLowerCase();
        String tagline = Optional.ofNullable(spec.getTagline()).orElse("").toLowerCase();
        String combined = brandName + " " + tagline;

        boolean isStartupVibe = detectStartupVibe(combined);
        int borderRadius = isStartupVibe ? 999 : 4;
        boolean useBoldTypography = spec.getStyle() != LogoStyle.MINIMALISTE;
        boolean useGradient = isStartupVibe || spec.getStyle() == LogoStyle.ABSTRAIT;
        double scalingFactor = brandName.length() < 6 ? 1.2 : 1.0;

        return new LogoIntent(
            isStartupVibe,
            borderRadius,
            scalingFactor,
            "#FF5C00",
            "#000000",
            "#1A1A1A",
            useBoldTypography,
            useGradient
        );
    }

    /**
     * Identifie si le projet nécessite une esthétique de type "Startup" ou "Moderne".
     * <p>
     * La recherche s'appuie sur un dictionnaire de mots-clés thématiques couvrant le 
     * numérique, la rapidité, et les nouveaux modes de consommation (notamment les concepts 
     * de proximité et de digitalisation comme le Drive ou la Livraison).
     * </p>
     *
     * @param text Le texte consolidé (nom + slogan) à analyser.
     * @return {@code true} si au moins un mot-clé de modernité est détecté, sinon {@code false}.
     */
    private boolean detectStartupVibe(String text) {
        Set<String> startupKeywords = Set.of(
            "app", "digital", "plateforme", "connect", "clic", "scan", "online", "drive", "click",
            "express", "rapide", "vite", "chrono", "flux", "direct", "livraison", "pret", "dispo",
            "nouveau", "moderne", "tendance", "vif", "peps", "boost", "startup", "smart", "frais", "bio",
            "local", "shop", "hub", "city", "commercant", "quartier", "village"
        );
        return startupKeywords.stream().anyMatch(text::contains);
    }
}