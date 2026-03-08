package com.penpot.ai.application.tools.config;

import com.penpot.ai.core.domain.marketing.MarketingStyle;
import org.springframework.stereotype.Component;

/**
 * Ce composant assure la passerelle entre les données textuelles brutes générées 
 * par l'intelligence artificielle et le domaine métier strict de l'application. 
 * <p>A comme responsabilité de la normalisation et de la conversion des intentions 
 * stylistiques approximatives vers l'énumération fortement typée {@link MarketingStyle}. 
 */
@Component
public class AiEnumResolver {

    /**
     * Procède à l'évaluation et à l'affectation d'un style marketing normalisé en fonction d'une entrée textuelle.
     *
     * @param style La chaîne de caractères brute fournie par le modèle de langage, 
     * représentant l'intention stylistique de l'utilisateur (ex. "DARK_MODE", "GLASS").
     * @return      L'instance de {@link MarketingStyle} correspondante, ou la valeur de 
     * secours {@code MarketingStyle.BOLD_ECOMMERCE} si l'entrée n'est pas reconnue ou invalide.
     */
    public MarketingStyle resolveStyle(String style) {
        if (style == null || style.isBlank()) return MarketingStyle.BOLD_ECOMMERCE;

        String normalized = style.trim().toUpperCase();
        return switch (normalized) {
            case "MODERN_ECOMMERCE" -> MarketingStyle.BOLD_ECOMMERCE;
            case "ECOMMERCE" -> MarketingStyle.BOLD_ECOMMERCE;
            case "MODERN" -> MarketingStyle.MODERN_GRADIENT;
            case "GRADIENT" -> MarketingStyle.MODERN_GRADIENT;
            case "MINIMAL" -> MarketingStyle.MINIMAL_LIGHT;
            case "LIGHT_MINIMAL" -> MarketingStyle.MINIMAL_LIGHT;
            case "DARK" -> MarketingStyle.DARK_SAAS;
            case "DARK_MODE" -> MarketingStyle.DARK_SAAS;
            case "GLASS" -> MarketingStyle.GLASSMORPHISM;
            case "GLASSMORPHISM" -> MarketingStyle.GLASSMORPHISM;
            case "NEUMORPHIC" -> MarketingStyle.NEUMORPHIC;
            case "NEUMORPHISM" -> MarketingStyle.NEUMORPHIC;
            default -> MarketingStyle.BOLD_ECOMMERCE;
        };
    }
}