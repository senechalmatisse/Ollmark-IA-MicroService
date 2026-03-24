package com.penpot.ai.application.tools.support;

import lombok.experimental.UtilityClass;

/**
 * Résout les valeurs de style vers le format attendu par l'API Penpot.
 *
 * <p>Penpot n'accepte pas les valeurs sémantiques CSS ("bold", "normal") 
 * pour fontWeight — il attend des valeurs numériques ("700", "400").</p>
 *
 * <p>De même, les couleurs rgba() doivent être décomposées en 
 * fillColor + fillOpacity dans les objets Fill.</p>
 */
@UtilityClass
public class PenpotStyleValues {

    /**
     * Convertit un poids typographique sémantique ou numérique vers
     * la valeur acceptée par l'API Penpot.
     *
     * <p>Penpot attend une chaîne numérique CSS standard :</p>
     * <ul>
     *   <li>"bold" → "700"</li>
     *   <li>"normal" → "400"</li>
     *   <li>"light" → "300"</li>
     *   <li>"semibold" → "600"</li>
     *   <li>Valeur numérique existante → inchangée</li>
     * </ul>
     *
     * @param fontWeight la valeur sémantique ou numérique entrante
     * @return la valeur numérique attendue par Penpot, ou "400" par défaut
     */
    public static String resolveFontWeight(String fontWeight) {
        if (fontWeight == null || fontWeight.isBlank()) return "400";
        return switch (fontWeight.toLowerCase().trim()) {
            case "thin"       -> "100";
            case "extralight" -> "200";
            case "light"      -> "300";
            case "normal",
                 "regular"    -> "400";
            case "medium"     -> "500";
            case "semibold",
                 "semi-bold"  -> "600";
            case "bold"       -> "700";
            case "extrabold",
                 "extra-bold" -> "800";
            case "black",
                 "heavy"      -> "900";
            default           -> fontWeight.trim(); // déjà numérique
        };
    }

    /**
     * Génère un fragment JS valide pour un objet Fill Penpot.
     *
     * <p>Penpot rejette rgba() comme fillColor. Il faut décomposer en
     * fillColor (hex) + fillOpacity (float 0-1).</p>
     *
     * <p>Exemples :</p>
     * <ul>
     *   <li>fillObject("#FF0000", 1.0)   → { fillColor: '#FF0000', fillOpacity: 1 }</li>
     *   <li>fillObject("#FFFFFF", 0.12)  → { fillColor: '#FFFFFF', fillOpacity: 0.12 }</li>
     * </ul>
     *
     * @param hexColor couleur au format hex (#RRGGBB)
     * @param opacity opacité entre 0 et 1
     * @return fragment JS de l'objet Fill
     */
    public static String fillObject(String hexColor, double opacity) {
        return String.format("{ fillColor: '%s', fillOpacity: %s }",
            hexColor, formatOpacity(opacity));
    }

    /**
     * Formate une valeur d'opacité en évitant les décimales inutiles.
     * 1.0 → "1", 0.12 → "0.12"
     */
    private static String formatOpacity(double opacity) {
        if (opacity == Math.floor(opacity)) {
            return String.valueOf((int) opacity);
        }
        return String.valueOf(opacity);
    }
}