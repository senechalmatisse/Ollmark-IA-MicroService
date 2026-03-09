package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.engine.Theme;
import com.penpot.ai.application.tools.support.JsScriptLoader;

import java.util.Map;

/**
 * Composant technique chargé de la génération et de l'assemblage dynamique du code JavaScript 
 * destiné à appliquer une identité visuelle complète (thème) sur une section au format A4.
 */
public class A4ThemeRenderer {

    /**
     * Compile séquentiellement les différents fragments JavaScript nécessaires à la matérialisation du thème visuel.
     *
     * @param theme L'entité encapsulant l'ensemble des variables et directives colorimétriques de l'identité visuelle.
     * @return      Une chaîne de caractères contenant le script JavaScript agrégé, prête à être exécutée par le moteur du canevas.
     */
    public String render(Theme theme) {
        String textColor = theme.gradient ? theme.textOnDark : theme.textOnLight;
        StringBuilder code = new StringBuilder();

        code.append(JsScriptLoader.loadWith("tools/a4engine/a4-theme-colors.js", Map.of(
            "textColor", textColor,
            "accent", theme.accent
        )));

        code.append(theme.gradient
            ? JsScriptLoader.loadWith("tools/a4engine/a4-theme-gradient.js", Map.of(
                "g1", theme.g1,
                "g2", theme.g2))
            : JsScriptLoader.loadWith("tools/a4engine/a4-theme-solid.js", Map.of(
                "bgSolid", theme.bgSolid))
        );

        code.append(JsScriptLoader.load("snippets/a4-accent-line.js"));
        code.append(JsScriptLoader.loadWith("snippets/a4-theme-deco.js", Map.of(
            "subtle", theme.subtle
        )));

        code.append(JsScriptLoader.load("snippets/a4-glow.js"));
        return code.toString();
    }
}