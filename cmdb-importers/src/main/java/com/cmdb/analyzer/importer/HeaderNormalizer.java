package com.cmdb.analyzer.importer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Thread-safe utility class for normalizing CMDB Excel column headers.
 * <p>
 * This class standardizes inconsistent or human-formatted column names
 * (e.g. "Parent CI Name", "CI Name", "Relationship Type")
 * into consistent lowercase keys (e.g. "parent_ci", "name", "relationship").
 * <p>
 * All normalization logic is case-insensitive and whitespace-tolerant.
 */
public final class HeaderNormalizer {

    private static final Map<Pattern, String> NORMALIZATION_RULES = new LinkedHashMap<>();
    static {
        NORMALIZATION_RULES.put(Pattern.compile(".*parent.?ci.*", Pattern.CASE_INSENSITIVE), "parent_ci");
        NORMALIZATION_RULES.put(Pattern.compile(".*ci.?name.*", Pattern.CASE_INSENSITIVE), "name");
        NORMALIZATION_RULES.put(Pattern.compile(".*class.*", Pattern.CASE_INSENSITIVE), "class");
        NORMALIZATION_RULES.put(Pattern.compile(".*relationship.*", Pattern.CASE_INSENSITIVE), "relationship");
        NORMALIZATION_RULES.put(Pattern.compile(".*desc.*", Pattern.CASE_INSENSITIVE), "description");
        NORMALIZATION_RULES.put(Pattern.compile(".*project.*", Pattern.CASE_INSENSITIVE), "project");
        NORMALIZATION_RULES.put(Pattern.compile(".*location.*", Pattern.CASE_INSENSITIVE), "location");
        NORMALIZATION_RULES.put(Pattern.compile(".*service.?offering.*", Pattern.CASE_INSENSITIVE), "service_offering");
        NORMALIZATION_RULES.put(Pattern.compile(".*business.?service.*", Pattern.CASE_INSENSITIVE), "business_service");
    }

    private HeaderNormalizer() {
    }

    /**
     * Normalizes a list of Excel column headers into a mapping of
     * original header → canonical key.
     * <p>
     * This method is thread-safe and can be called concurrently.
     *
     * @param rawHeaders list of raw header strings
     * @return mapping from original header to normalized key
     */
    public static Map<String, String> normalizeHeaders(List<String> rawHeaders) {
        Map<String, String> normalized = new ConcurrentHashMap<>();
        if (rawHeaders == null)
            return normalized;

        rawHeaders.parallelStream().forEach(header -> {
            String cleaned = header.trim().toLowerCase().replaceAll("\\s+", "_");
            String canonical = applyRules(cleaned);
            normalized.put(header, canonical);
        });

        return normalized;
    }

    /**
     * Applies regex-based normalization rules to a header string.
     *
     * @param cleaned lowercased and trimmed header
     * @return canonical normalized header name
     */
    private static String applyRules(String cleaned) {
        for (Map.Entry<Pattern, String> rule : NORMALIZATION_RULES.entrySet()) {
            if (rule.getKey().matcher(cleaned).matches()) {
                return rule.getValue();
            }
        }
        // Default: keep sanitized form
        return cleaned.replaceAll("[^a-z0-9_]+", "_");
    }
}
