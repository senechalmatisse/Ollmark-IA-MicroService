package com.penpot.ai.application.tools.logo;

import com.penpot.ai.core.domain.logo.*;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Moteur chargé de déterminer la palette de couleurs à appliquer
 * lors de la génération d'un logo.
 *
 * <p>Cette classe analyse le nom de la marque ainsi que l'intention
 * graphique représentée par {@link LogoIntent} afin de sélectionner
 * une palette cohérente avec l'univers visuel et marketing de la
 * marque.</p>
 *
 * <p>Le moteur applique plusieurs règles heuristiques :</p>
 * <ul>
 *     <li>détection de mots-clés dans le nom de la marque</li>
 *     <li>prise en compte du positionnement startup ou corporate</li>
 *     <li>association avec des palettes thématiques prédéfinies</li>
 * </ul>
 *
 * <p>Les palettes utilisées correspondent à différents univers
 * visuels fréquemment rencontrés dans le branding :</p>
 * <ul>
 *     <li>énergie / startup</li>
 *     <li>technologie</li>
 *     <li>nature / écologie</li>
 *     <li>luxe</li>
 *     <li>gastronomie / passion</li>
 * </ul>
 *
 * <p>La sélection finale est injectée dans un nouvel objet
 * {@link LogoIntent} qui reprend les propriétés existantes
 * tout en ajoutant les couleurs finales du logo.</p>
 */
@Component
public class LogoThemeEngine {

    /**
     * Sélectionne et applique une palette de couleurs en fonction
     * de l'intention graphique et du nom de la marque.
     *
     * @param spec spécification du logo contenant notamment
     *             le nom de la marque
     * @param intent intention graphique calculée pour le logo
     * @return nouvel objet {@link LogoIntent} contenant la palette
     *         de couleurs sélectionnée
     */
    public LogoIntent applyTheme(LogoSpec spec, LogoIntent intent) {
        String brandName = spec.getBrandName().toLowerCase();
        Map<String, Palette> themes = Map.of(
            "energie", new Palette("#FF5C00", "#FF8A00", "#1A1A1A"),
            "tech", new Palette("#4F46E5", "#06B6D4", "#FFFFFF"),
            "nature", new Palette("#059669", "#10B981", "#064E3B"),
            "luxe", new Palette("#111827", "#374151", "#F3F4F6"),
            "passion", new Palette("#E11D48", "#FB7185", "#4C0519")
        );

        Palette selected = pickBestPalette(brandName, intent.isStartupVibe(), themes);

        return new LogoIntent(
            intent.isStartupVibe(),
            intent.getBorderRadius(),
            intent.getScalingFactor(),
            selected.primary,
            selected.secondary,
            selected.text,
            intent.isUseBoldTypography(),
            intent.isUseGradient()
        );
    }

    /**
     * Détermine la palette la plus appropriée en fonction
     * du nom de la marque et du positionnement marketing.
     *
     * <p>La sélection s'effectue par détection de mots-clés
     * dans le nom de la marque :</p>
     * <ul>
     *     <li>univers écologique → palette nature</li>
     *     <li>positionnement premium → palette luxe</li>
     *     <li>univers gastronomique → palette passion</li>
     * </ul>
     *
     * <p>Si aucun mot-clé spécifique n'est détecté :</p>
     * <ul>
     *     <li>les marques startup utilisent une palette énergique</li>
     *     <li>les autres utilisent une palette technologique</li>
     * </ul>
     *
     * @param name nom de la marque normalisé en minuscules
     * @param isStartup indique si la marque adopte un positionnement startup
     * @param themes ensemble des palettes disponibles
     * @return palette sélectionnée
     */
    private Palette pickBestPalette(String name, boolean isStartup, Map<String, Palette> themes) {
        if (name.contains("bio") || name.contains("vert") || name.contains("frais")) {
            return themes.get("nature");
        }
        if (name.contains("luxe") || name.contains("premium") || name.contains("noir")) {
            return themes.get("luxe");
        }
        if (name.contains("boucherie") || name.contains("viande") || name.contains("gourmet")) {
            return themes.get("passion");
        }
        return isStartup ? themes.get("energie") : themes.get("tech");
    }

    /**
     * Structure interne représentant une palette de couleurs.
     *
     * <p>Chaque palette contient :</p>
     * <ul>
     *     <li>une couleur principale (primary)</li>
     *     <li>une couleur secondaire (secondary)</li>
     *     <li>une couleur de texte (text)</li>
     * </ul>
     *
     * <p>Cette structure est utilisée uniquement pour transporter
     * temporairement les couleurs lors du processus de sélection.</p>
     *
     * @param primary couleur principale du logo
     * @param secondary couleur secondaire ou d'accent
     * @param text couleur utilisée pour la typographie
     */
    private record Palette(String primary, String secondary, String text) {}
}