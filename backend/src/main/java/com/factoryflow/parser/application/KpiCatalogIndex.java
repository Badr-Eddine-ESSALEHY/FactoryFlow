package com.factoryflow.parser.application;

import com.factoryflow.kpi.domain.KpiAlias;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.shared.text.TextNormalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class KpiCatalogIndex {

    private final List<KpiDefinition> definitions;
    private final List<LabelVariant> variants;

    private final Map<String, List<LabelVariant>> exactCanonical;
    private final Map<String, List<LabelVariant>> exactAliases;
    private final Map<String, List<LabelVariant>> normalized;
    private final Map<String, List<LabelVariant>> compact;

    public KpiCatalogIndex(List<KpiDefinition> definitions) {
        this.definitions = definitions == null
                ? List.of()
                : List.copyOf(definitions);

        List<LabelVariant> allVariants = new ArrayList<>();

        Map<String, List<LabelVariant>> canonicalExactBuilder =
                new LinkedHashMap<>();

        Map<String, List<LabelVariant>> aliasExactBuilder =
                new LinkedHashMap<>();

        Map<String, List<LabelVariant>> normalizedBuilder =
                new LinkedHashMap<>();

        Map<String, List<LabelVariant>> compactBuilder =
                new LinkedHashMap<>();

        for (KpiDefinition definition : this.definitions) {
            addVariant(
                    new LabelVariant(
                            definition,
                            definition.getCode(),
                            TextNormalizer.normalizeLabel(definition.getCode()),
                            TextNormalizer.compactLabel(definition.getCode()),
                            LabelKind.CODE
                    ),
                    allVariants,
                    canonicalExactBuilder,
                    normalizedBuilder,
                    compactBuilder
            );

            addVariant(
                    new LabelVariant(
                            definition,
                            definition.getDisplayName(),
                            TextNormalizer.normalizeLabel(definition.getDisplayName()),
                            TextNormalizer.compactLabel(definition.getDisplayName()),
                            LabelKind.DISPLAY_NAME
                    ),
                    allVariants,
                    canonicalExactBuilder,
                    normalizedBuilder,
                    compactBuilder
            );

            for (KpiAlias alias : definition.getAliases()) {
                LabelVariant variant = new LabelVariant(
                        definition,
                        alias.getAlias(),
                        TextNormalizer.normalizeLabel(alias.getAlias()),
                        TextNormalizer.compactLabel(alias.getAlias()),
                        LabelKind.ALIAS
                );

                allVariants.add(variant);

                put(aliasExactBuilder, exactKey(alias.getAlias()), variant);
                put(normalizedBuilder, variant.normalized(), variant);
                put(compactBuilder, variant.compact(), variant);
            }
        }

        this.variants = List.copyOf(allVariants);
        this.exactCanonical = freeze(canonicalExactBuilder);
        this.exactAliases = freeze(aliasExactBuilder);
        this.normalized = freeze(normalizedBuilder);
        this.compact = freeze(compactBuilder);
    }

    public List<KpiDefinition> definitions() {
        return definitions;
    }

    public List<LabelVariant> variants() {
        return variants;
    }

    public List<LabelVariant> findExactCanonical(String sourceLabel) {
        return lookup(exactCanonical, exactKey(sourceLabel));
    }

    public List<LabelVariant> findExactAlias(String sourceLabel) {
        return lookup(exactAliases, exactKey(sourceLabel));
    }

    public List<LabelVariant> findNormalized(String sourceLabel) {
        return lookup(
                normalized,
                TextNormalizer.normalizeLabel(sourceLabel)
        );
    }

    public List<LabelVariant> findCompact(String sourceLabel) {
        return lookup(
                compact,
                TextNormalizer.compactLabel(sourceLabel)
        );
    }

    private void addVariant(
            LabelVariant variant,
            List<LabelVariant> allVariants,
            Map<String, List<LabelVariant>> canonicalExactBuilder,
            Map<String, List<LabelVariant>> normalizedBuilder,
            Map<String, List<LabelVariant>> compactBuilder
    ) {
        allVariants.add(variant);

        put(
                canonicalExactBuilder,
                exactKey(variant.original()),
                variant
        );

        put(
                normalizedBuilder,
                variant.normalized(),
                variant
        );

        put(
                compactBuilder,
                variant.compact(),
                variant
        );
    }

    private void put(
            Map<String, List<LabelVariant>> target,
            String key,
            LabelVariant value
    ) {
        if (key == null || key.isBlank()) {
            return;
        }

        target.computeIfAbsent(
                key,
                ignored -> new ArrayList<>()
        ).add(value);
    }

    private List<LabelVariant> lookup(
            Map<String, List<LabelVariant>> source,
            String key
    ) {
        if (key == null || key.isBlank()) {
            return List.of();
        }

        return source.getOrDefault(key, List.of());
    }

    private Map<String, List<LabelVariant>> freeze(
            Map<String, List<LabelVariant>> source
    ) {
        Map<String, List<LabelVariant>> result =
                new LinkedHashMap<>();

        source.forEach(
                (key, value) -> result.put(
                        key,
                        List.copyOf(value)
                )
        );

        return Map.copyOf(result);
    }

    private String exactKey(String value) {
        return TextNormalizer.normalizeWhitespace(value)
                .toLowerCase(Locale.ROOT);
    }

    public enum LabelKind {
        CODE,
        DISPLAY_NAME,
        ALIAS
    }

    public record LabelVariant(
            KpiDefinition definition,
            String original,
            String normalized,
            String compact,
            LabelKind kind
    ) {
    }
}