package com.penpot.ai.infrastructure.factory;

import com.penpot.ai.infrastructure.strategy.ResultFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Fabrique de {@link ResultFormatter} sélectionnant automatiquement
 * le formateur le plus adapté selon le type de résultat à traiter.
 *
 * <h2>Ordre de priorité</h2>
 * <p>Les formateurs sont triés par priorité décroissante lors de la construction.
 * Le premier formateur déclarant supporter le type demandé via
 * {@link ResultFormatter#supports(Class)} est retenu. En l'absence de formateur
 * compatible, un formateur de secours basé sur {@link Object#toString()} est
 * utilisé.</p>
 *
 * <h2>Enregistrement des formateurs</h2>
 * <p>Tout bean Spring implémentant {@link ResultFormatter} est automatiquement
 * découvert et enregistré. Il suffit d'annoter la nouvelle implémentation avec
 * {@code @Component} (ou équivalent) pour qu'elle soit prise en compte.</p>
 *
 * @see ResultFormatter
 */
@Slf4j
@Component
public class ResultFormatterFactory {

    /**
     * Liste de tous les formateurs disponibles, triés par priorité décroissante.
     * Le tri est effectué une seule fois à la construction afin que
     * {@link #getFormatter(Class)} reste O(n) sans tri répété.
     */
    private final List<ResultFormatter> formatters;

    /**
     * Formateur par défaut utilisé lorsqu'aucun formateur spécifique ne
     * correspond au type demandé.
     *
     * <p>Il s'agit du formateur de plus basse priorité parmi ceux enregistrés,
     * ou du formateur de secours interne si la liste est vide.</p>
     *
     * @see #createFallbackFormatter()
     */
    private final ResultFormatter defaultFormatter;

    /**
     * Construit la fabrique et initialise la liste ordonnée des formateurs.
     *
     * <p>Spring injecte automatiquement tous les beans implémentant
     * {@link ResultFormatter}. Les formateurs sont triés par
     * {@link ResultFormatter#priority()} décroissant afin que
     * {@link #getFormatter(Class)} retourne toujours le formateur
     * de plus haute priorité parmi ceux supportant le type cible.</p>
     *
     * <p>Si aucun formateur n'est disponible, un formateur de secours
     * délégant à {@link Object#toString()} est assigné comme valeur par défaut.</p>
     *
     * @param formatters liste de tous les formateurs enregistrés en tant que
     *                   beans Spring ; ne doit pas être {@code null}
     */
    public ResultFormatterFactory(List<ResultFormatter> formatters) {
        this.formatters = formatters.stream()
            .sorted(Comparator.comparingInt(ResultFormatter::priority).reversed())
            .toList();
        this.defaultFormatter = this.formatters.isEmpty()
            ? createFallbackFormatter()
            : this.formatters.get(this.formatters.size() - 1);
        log.info("Initialized ResultFormatterFactory with {} formatters", formatters.size());
    }

    /**
     * Retourne le formateur approprié pour l'objet donné.
     *
     * <p>Délègue à {@link #getFormatter(Class)} en passant le type réel de
     * l'objet via {@link Object#getClass()}. Si {@code result} est {@code null},
     * le formateur par défaut est retourné immédiatement.</p>
     *
     * @param result l'objet à formater ; peut être {@code null}
     * @return le formateur le plus adapté au type de {@code result},
     *         ou le formateur par défaut si {@code result} est {@code null}
     */
    public ResultFormatter getFormatterForObject(Object result) {
        if (result == null) return defaultFormatter;
        return getFormatter(result.getClass());
    }

    /**
     * Retourne le formateur de plus haute priorité supportant le type donné.
     *
     * <p>Parcourt la liste triée des formateurs et retourne le premier
     * dont {@link ResultFormatter#supports(Class)} renvoie {@code true}.
     * Si aucun formateur ne supporte le type, un avertissement est journalisé
     * et le formateur par défaut est retourné.</p>
     *
     * @param resultType le type {@link Class} de l'objet à formater ;
     *                   si {@code null}, le formateur par défaut est retourné
     * @return le formateur le plus adapté, ou {@link #defaultFormatter}
     *         si aucun formateur compatible n'est trouvé
     */
    private ResultFormatter getFormatter(Class<?> resultType) {
        if (resultType == null) return defaultFormatter;
        return formatters.stream()
            .filter(formatter -> formatter.supports(resultType))
            .findFirst()
            .orElseGet(() -> {
                log.warn(
                    "No specific formatter found for type {}, using default",
                    resultType.getSimpleName()
                );
                return defaultFormatter;
            });
    }

    /**
     * Crée un formateur de secours utilisé lorsqu'aucun bean {@link ResultFormatter}
     * n'est disponible dans le contexte Spring.
     *
     * <p>Ce formateur accepte tous les types ({@link ResultFormatter#supports(Class)}
     * retourne toujours {@code true}) et délègue le formatage à
     * {@link Object#toString()}, retournant une chaîne vide si l'objet est
     * {@code null}. Sa priorité est {@code -1} afin de ne jamais être préféré
     * à un formateur correctement configuré.</p>
     *
     * @return une instance anonyme de {@link ResultFormatter} servant de
     *         garde-fou en cas d'absence totale de formateurs
     */
    private ResultFormatter createFallbackFormatter() {
        return new ResultFormatter() {
            @Override
            public String format(Object result) {
                return result != null ? result.toString() : "";
            }

            @Override
            public boolean supports(Class<?> resultType) {
                return true;
            }

            @Override
            public int priority() {
                return -1;
            }
        };
    }
}