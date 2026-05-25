package com.cmdbanalyzer.service;

import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Normalized lookup index for configuration items in a parsed CMDB workbook.
 */
public class CmdbIdentityIndex {

    private final Map<String, List<ConfigurationItem>> byName;
    private final Map<String, List<ConfigurationItem>> byIdentityKey;
    private final Map<String, List<ConfigurationItem>> byNameAndClass;

    public CmdbIdentityIndex(CmdbWorkbook workbook) {
        List<ConfigurationItem> items = workbook.getSheets().stream()
                .flatMap(sheet -> sheet.getConfigurationItems().stream())
                .toList();
        byName = groupBy(items, item -> normalize(item.getName()));
        byIdentityKey = groupBy(items, item -> normalize(item.getIdentityKey()));
        byNameAndClass = groupBy(items, item -> composite(item.getName(), item.getCiClass()));
    }

    public List<ConfigurationItem> findCandidates(String targetName) {
        String normalizedTarget = normalize(targetName);
        if (normalizedTarget.isEmpty()) {
            return List.of();
        }

        Set<ConfigurationItem> matches = new LinkedHashSet<>();
        matches.addAll(byName.getOrDefault(normalizedTarget, List.of()));
        matches.addAll(byIdentityKey.getOrDefault(normalizedTarget, List.of()));
        matches.addAll(byNameAndClass.getOrDefault(normalizedTarget, List.of()));
        return new ArrayList<>(matches);
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String composite(String name, String ciClass) {
        String normalizedName = normalize(name);
        String normalizedClass = normalize(ciClass);
        if (normalizedName.isEmpty() || normalizedClass.isEmpty()) {
            return "";
        }
        return normalizedName + "|" + normalizedClass;
    }

    private interface KeyProvider {
        String key(ConfigurationItem item);
    }

    private Map<String, List<ConfigurationItem>> groupBy(List<ConfigurationItem> items, KeyProvider keyProvider) {
        return items.stream()
                .collect(Collectors.groupingBy(keyProvider::key))
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
