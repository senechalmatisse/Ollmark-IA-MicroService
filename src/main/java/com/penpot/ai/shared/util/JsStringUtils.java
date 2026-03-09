package com.penpot.ai.shared.util;

/**
 * Utilitaires d'échappement pour les chaînes JavaScript injectées dans les templates Penpot.
 *
 * <p>Centralise les helpers string dupliqués dans
 * {@code ThemeEngine}, {@code TemplateResolver}, {@code MarketingIntentEngine},
 * {@code A4MarketingIntentEngine}, {@code LayoutRenderer}, {@code CtaRenderer}
 * et {@code A4CtaRenderer}.</p>
 */
public final class JsStringUtils {

    private JsStringUtils() {}

    /**
     * Retourne {@code ""} si {@code s} est {@code null}, sinon {@code s}.
     */
    public static String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * Retourne {@code ""} si {@code e} est {@code null}, sinon {@code e.name()}.
     * Remplace le helper privé {@code safeEnum()} de {@code ThemeEngine}.
     */
    public static String safeEnum(Enum<?> e) {
        return e == null ? "" : e.name();
    }

    /**
     * Échappe une chaîne pour injection sécurisée dans un littéral JavaScript.
     *
     * <p>Transformations dans l'ordre :</p>
     * <ol>
     *   <li>{@code \} → {@code \\}</li>
     *   <li>{@code '} → {@code \'}</li>
     *   <li>{@code \r} supprimé</li>
     *   <li>{@code \n} → {@code \\n}</li>
     * </ol>
     *
     * @param s la chaîne à échapper, peut être {@code null}
     * @return chaîne échappée, jamais {@code null}
     */
    public static String jsSafe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    /**
     * Retourne {@code true} si {@code text} contient au moins un des mots-clés fournis.
     *
     * <p>Remplace la méthode privée dupliquée dans {@code A4MarketingIntentEngine}
     * et le pattern {@code text.contains(x) || text.contains(y)} de {@code MarketingIntentEngine}.</p>
     *
     * @param text     texte à analyser, ne doit pas être {@code null}
     * @param keywords mots-clés à rechercher
     */
    public static boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}