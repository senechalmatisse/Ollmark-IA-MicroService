package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.pipeline.*;
import com.penpot.ai.application.tools.pipeline.A4SectionPipeline.A4SectionRequest;
import com.penpot.ai.application.tools.support.*;
import com.penpot.ai.core.domain.logo.*;
import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.shared.util.JsStringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Façade Spring AI regroupant tous les outils de création de contenu Penpot.
 *
 * <p>Cette classe a deux types de responsabilités :</p>
 * <ul>
 *   <li><b>Tools simples</b> (title, subtitle, paragraph, image, button) : génèrent
 *       le JS directement via {@link PenpotJsSnippets} / {@link JsScriptLoader} et
 *       délèguent l'exécution à {@link PenpotToolExecutor}.</li>
 *   <li><b>Tools complexes</b> (logo, A4 section) : mappent les paramètres {@code @ToolParam}
 *       vers un objet spec typé puis délèguent à leur pipeline
 *       ({@link LogoPipeline}, {@link A4SectionPipeline}) qui applique le patron de méthode.</li>
 * </ul>
 * 
 * @version 1.3
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotContentTools {

    private final PenpotToolExecutor toolExecutor;
    private final LogoPipeline logoPipeline;
    private final A4SectionPipeline a4Pipeline;
    private final SectionPipeline sectionPipeline;

    /** Taille de police standardisée pour les titres principaux (H1). */
    private static final int H1_SIZE = 48;

    /** Taille de police standardisée pour les sous-titres (H2). */
    private static final int H2_SIZE = 32;

    /** Taille de police standardisée pour les paragraphes de texte courant. */
    private static final int P_SIZE = 16;

    /** Constante définissant l'épaisseur de police en gras. */
    private static final String BOLD = "bold";

    /** Constante définissant l'épaisseur de police normale. */
    private static final String NORMAL = "normal";

    /**
     * Génère un élément textuel de niveau principal (H1) au sein de la zone de travail.
     *
     * @param content Le texte brut destiné à être affiché comme titre.
     * @param x       La coordonnée horizontale définissant le point d'ancrage de l'élément.
     * @param y       La coordonnée verticale définissant le point d'ancrage de l'élément.
     * @param color   La valeur hexadécimale de la couleur du texte (paramètre optionnel).
     * @return        Une chaîne de caractères représentant l'identifiant technique de l'élément créé ou le statut de l'opération.
     */
    @Tool(description = """
        Generates a top-level H1 heading.
        Use this strictly for the main title of a document, page, or the most prominent textual element on the canvas.
    """)
    public String createTitle(
        @ToolParam(description = "The exact text content to display as the title.") String content,
        @ToolParam(description = "Absolute X coordinate on the canvas.") Integer x,
        @ToolParam(description = "Absolute Y coordinate on the canvas.") Integer y,
        @ToolParam(description = "Valid CSS hex color code (e.g., '#000000', '#FF5733'). Defaults to black if omitted.", required = false) String color
    ) {
        log.info("Tool called: createTitle");
        return toolExecutor.createContent(
            PenpotJsSnippets.createText(content, x, y, H1_SIZE, BOLD, color, "Title"),
            "title"
        );
    }

    /**
     * Instancie un sous-titre (H2) destiné à structurer hiérarchiquement l'interface.
     *
     * @param content Le contenu textuel du sous-titre.
     * @param x       La position horizontale sur la zone de travail.
     * @param y       La position verticale sur la zone de travail.
     * @param color   La teinte hexadécimale applicable à la police de caractères.
     * @return        La réponse formatée du moteur d'exécution confirmant la création.
     */
    @Tool(description = """
        Generates an H2 subheading.
        Ideal for section titles, grouping related content, or secondary hierarchical text elements to divide the layout.
    """)
    public String createSubtitle(
        @ToolParam(description = "The exact text content for the subtitle.") String content,
        @ToolParam(description = "Absolute X coordinate on the canvas.") Integer x,
        @ToolParam(description = "Absolute Y coordinate on the canvas.") Integer y,
        @ToolParam(description = "Valid CSS hex color code (e.g., '#333333'). Defaults to dark gray if omitted.", required = false) String color
    ) {
        log.info("Tool called: createSubtitle");
        return toolExecutor.createContent(
            PenpotJsSnippets.createText(content, x, y, H2_SIZE, BOLD, color, "Subtitle"),
            "subtitle"
        );
    }

    /**
     * Produit un bloc de texte courant conçu pour l'affichage de paragraphes réguliers.
     *
     * @param content Le corps du texte à intégrer.
     * @param x       L'axe horizontal du point d'insertion.
     * @param y       L'axe vertical du point d'insertion.
     * @param color   Le code couleur hexadécimal ciblé.
     * @return        Le résultat JSON contenant l'identifiant généré par Penpot.
     */
    @Tool(description = """
        Generates standard body text.
        Use this for descriptions, long paragraphs or any informational text that is not a heading.
    """)
    public String createParagraph(
        @ToolParam(description = "The body text content. Can be multiple sentences.") String content,
        @ToolParam(description = "Absolute X coordinate on the canvas.") Integer x,
        @ToolParam(description = "Absolute Y coordinate on the canvas.") Integer y,
        @ToolParam(description = "Valid CSS hex color code (e.g., '#666666').", required = false) String color
    ) {
        log.info("Tool called: createParagraph");
        return toolExecutor.createContent(
            PenpotJsSnippets.createText(content, x, y, P_SIZE, NORMAL, color, "Paragraph"),
            "paragraph"
        );
    }

    /**
     * Intègre une ressource graphique externe au sein de la maquette via son URL.
     *
     * @param url    L'adresse web pointant vers le fichier image source.
     * @param x      La position d'ancrage sur l'axe des abscisses.
     * @param y      La position d'ancrage sur l'axe des ordonnées.
     * @param width  La largeur cible de l'image rendue (optionnelle, défaut : 300).
     * @param height La hauteur cible de l'image rendue (optionnelle, défaut : 200).
     * @return       La chaîne de validation renvoyée par le coordinateur d'outils.
     */
    @Tool(description = "Embeds an external image onto the canvas using a provided URL.")
    public String createImage(
        @ToolParam(description = "A direct, publicly accessible HTTP/HTTPS URL pointing to an image file (e.g., JPG, PNG).") String url,
        @ToolParam(description = "Absolute X coordinate for the top-left corner of the image.") Integer x,
        @ToolParam(description = "Absolute Y coordinate for the top-left corner of the image.") Integer y,
        @ToolParam(description = "Desired width in pixels. Defaults to 300 if omitted.", required = false) Integer width,
        @ToolParam(description = "Desired height in pixels. Defaults to 200 if omitted.", required = false) Integer height
    ) {
        log.info("Tool called: createImage (url={})", url);
        int w = (width  != null) ? width  : 300;
        int h = (height != null) ? height : 200;

        String code = JsScriptLoader.loadWith("tools/content/create-image.js", Map.of(
            "url", JsStringUtils.jsSafe(url),
            "width", String.valueOf(w),
            "height", String.valueOf(h),
            "x", String.valueOf(x),
            "y", String.valueOf(y)
        ));

        return toolExecutor.createContent(code, "image");
    }

    /**
     * Génère un composant interactif de type bouton, doté d'une esthétique épurée.
     *
     * @param label           Le texte affiché au centre du composant.
     * @param x               La position horizontale de l'élément.
     * @param y               La position verticale de l'élément.
     * @param width           La largeur minimale du bouton (optionnelle, défaut : 200).
     * @param height          La hauteur minimale du bouton (optionnelle, défaut : 60).
     * @param backgroundColor La couleur d'arrière-plan sous forme de code hexadécimal (optionnelle).
     * @param textColor       La couleur de la typographie du libellé (optionnelle).
     * @param radius          L'arrondi appliqué aux angles du rectangle (optionnel, défaut : 12).
     * @return                Le retour d'exécution signalant la création de la forme graphique correspondante.
     */
    @Tool(description = """
        Generates an interactive Call-To-Action (CTA) button component. 
        Use this whenever the user requests a clickable button or a distinct action trigger in a UI.
    """)
    public String createButton(
        @ToolParam(description = "The short text displayed inside the button.") String label,
        @ToolParam(description = "Absolute X coordinate.") Integer x,
        @ToolParam(description = "Absolute Y coordinate.") Integer y,
        @ToolParam(description = "Minimum width in pixels. Defaults to 200.", required = false) Integer width,
        @ToolParam(description = "Minimum height in pixels. Defaults to 60.", required = false) Integer height,
        @ToolParam(description = "Background color as a hex code (e.g., '#4F46E5'). Defaults to indigo.", required = false) String backgroundColor,
        @ToolParam(description = "Text color as a hex code (e.g., '#FFFFFF'). Defaults to white.", required = false) String textColor,
        @ToolParam(description = "Border radius for rounded corners. Defaults to 12.", required = false) Integer radius
    ) {
        log.info("Tool called: createButton");

        int w = (width != null && width > 0) ? width : 200;
        int h = (height != null && height > 0) ? height : 60;
        int r = (radius != null && radius >= 0) ? radius : 12;
        String bg = (backgroundColor != null && !backgroundColor.isBlank()) ? backgroundColor : "#4F46E5";
        String tc = (textColor != null && !textColor.isBlank()) ? textColor : "#FFFFFF";
        int posX = (x != null) ? x : 50;
        int posY = (y != null) ? y : 50;
        String safeLabel = (label == null) ? "" : JsStringUtils.jsSafe(label);

        String code = JsScriptLoader.loadWith("tools/content/create-button.js", Map.of(
            "minW", String.valueOf(w),
            "minH", String.valueOf(h),
            "padX", "24",
            "posX", String.valueOf(posX),
            "posY", String.valueOf(posY),
            "bg", bg,
            "textColor", tc,
            "radius", String.valueOf(r),
            "label", safeLabel
        ));

        return toolExecutor.createContent(code, "button");
    }

    /**
     * Orchestre la conception complète d'une identité visuelle d'entreprise (logo).
     *
     * @param brandName La dénomination officielle de la marque à valoriser.
     * @param tagline   La devise ou l'accroche commerciale complétant le logo (optionnelle).
     * @param style     La catégorisation stylistique du symbole attendu (ex. abstrait, minimaliste).
     * @param layout    La stratégie de répartition spatiale des éléments constitutifs (ex. empilé, horizontal).
     * @param x         L'emplacement initial prévu sur l'axe X (optionnel).
     * @param y         L'emplacement initial prévu sur l'axe Y (optionnel).
     * @return          L'identifiant du groupe graphique constituant l'entité logo finalisée.
     */
    @Tool(description = """
        Generates a professional, vector-based logo for a brand or business. 
        Use this exclusively when the user specifically asks to design, create, or generate a logo.
    """)
    public String createLogo(
        @ToolParam(description = "The official brand or company name.") String brandName,
        @ToolParam(description = "An optional short slogan, catchphrase, or descriptor to accompany the brand name.", required = false) String tagline,
        @ToolParam(description = "The desired visual style for the symbol " +
            "(Choices: ABSTRAIT, GEOMETRIQUE, MONOGRAMME, MINIMALISTE, EMBLEME).")
        LogoStyle style,
        @ToolParam(description = "The spatial arrangement of the text and symbol " +
            "(Choices: HORIZONTAL, VERTICAL, STACKED, EMBLEM).")
        LogoLayout layout,
        @ToolParam(description = "Absolute X coordinate. Defaults to 100.", required = false) Integer x,
        @ToolParam(description = "Absolute Y coordinate. Defaults to 100.", required = false) Integer y
    ) {
        log.info("Tool called: createLogo (brand={})", brandName);

        LogoSpec spec = LogoSpec.builder()
            .brandName(brandName)
            .tagline(tagline)
            .style(style)
            .layout(layout)
            .x(x != null ? x : 100)
            .y(y != null ? y : 100)
            .build();

        return logoPipeline.execute(spec);
    }

    /**
     * Lance la génération d'un ensemble de contenus agencés spécifiquement pour un support normé A4.
     *
     * @param spec La nomenclature des éléments constitutifs de la section (titre, paragraphes, médias).
     * @param x    La coordonnée de positionnement global de la section sur l'axe horizontal (optionnelle).
     * @param y    La coordonnée de positionnement global de la section sur l'axe vertical (optionnelle).
     * @return     Le statut d'exécution renvoyé par le processus de mise en page structurée.
     */
    @Tool(description = """
        Generates a comprehensive layout section specifically optimized for A4 print document dimensions.
        Use this when the user explicitly requests a document layout or physical print media structure.
    """)
    public String createA4Section(
        @ToolParam(description = "A structured object detailing the content of the section (title, subtitle, paragraphs, images).") SectionSpec spec,
        @ToolParam(description = "Starting X coordinate for the entire section.", required = false) Integer x,
        @ToolParam(description = "Starting Y coordinate for the entire section.", required = false) Integer y
    ) {
        log.info("Tool called: createA4Section");

        A4SectionRequest request = new A4SectionRequest(
            spec != null ? spec : new SectionSpec(),
            x != null ? x : 0,
            y != null ? y : 0
        );

        return a4Pipeline.execute(request);
    }

    /**
     * Assemble et positionne une section d'appel principale (Hero section) destinée aux interfaces numériques.
     * En mobilisant le pipeline de section standard, cette méthode organise l'espace de présentation 
     * en articulant logiquement l'accroche, les descriptifs et les appels à l'action.
     *
     * @param spec Le modèle définissant la sémantique et le contenu attendu de la section.
     * @param x    L'origine horizontale de l'encart généré (optionnelle, défaut : 80).
     * @param y    L'origine verticale de l'encart généré (optionnelle, défaut : 120).
     * @return     L'identifiant de la structure parente regroupant les éléments générés.
     */
    @Tool(description = """
        Generates a digital marketing hero section or UI block optimized for web design. 
        Use this for constructing landing pages, website headers, or any digital UI screen layouts.
    """)
    public String createSection(
        @ToolParam(description = "A structured object defining the semantic content of the digital layout.") SectionSpec spec,
        @ToolParam(description = "Starting X coordinate for the section. Defaults to 80.", required = false) Integer x,
        @ToolParam(description = "Starting Y coordinate for the section. Defaults to 120.", required = false) Integer y
    ) {
        log.info("Tool called: createSection");
        SectionPipeline.SectionRequest request = new SectionPipeline.SectionRequest(
            spec,
            x != null ? x : 80,
            y != null ? y : 120
        );
        return sectionPipeline.execute(request);
    }
}