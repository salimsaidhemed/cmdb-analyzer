package com.cmdbanalyzer.parser.poi;

import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.model.WarningSeverity;
import com.cmdbanalyzer.parser.HeaderDetectionResult;
import com.cmdbanalyzer.parser.HeaderDetector;
import com.cmdbanalyzer.parser.SheetDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Basic tolerant header detector for CMDB sample workbooks.
 */
public class BasicHeaderDetector implements HeaderDetector {

    private static final int MAX_HEADER_SCAN_ROWS = 20;
    private static final Map<String, String> HEADER_ALIASES = Map.ofEntries(
            Map.entry("name", "name"),
            Map.entry("ci name", "name"),
            Map.entry("configuration item", "configuration item"),
            Map.entry("class", "class"),
            Map.entry("ci class", "class"),
            Map.entry("description", "description"),
            Map.entry("environment", "environment"),
            Map.entry("owner", "owner"),
            Map.entry("status", "status"),
            Map.entry("parent ci", "parent ci"),
            Map.entry("dependency", "parent ci"),
            Map.entry("connected service", "parent ci"),
            Map.entry("relationship", "relationship"),
            Map.entry("relationship type", "relationship")
    );

    @Override
    public HeaderDetectionResult detectHeaders(SheetDescriptor sheetDescriptor) {
        List<ParserWarning> warnings = new ArrayList<>();
        int scanLimit = Math.min(MAX_HEADER_SCAN_ROWS, sheetDescriptor.sampleRows().size());
        int bestRowIndex = 0;
        int bestScore = 0;
        Map<String, Integer> bestHeaders = Map.of();

        for (int rowIndex = 1; rowIndex <= scanLimit; rowIndex++) {
            Map<String, Integer> headers = normalizeHeaders(sheetDescriptor.sampleRows().get(rowIndex - 1), sheetDescriptor, warnings);
            int score = score(headers);
            if (score > bestScore) {
                bestScore = score;
                bestRowIndex = rowIndex;
                bestHeaders = headers;
            }
        }

        if (bestScore < 2) {
            warnings.add(new ParserWarning(
                    WarningSeverity.WARNING,
                    "No reliable header row detected",
                    sheetDescriptor.workbookName(),
                    sheetDescriptor.sheetName(),
                    null,
                    null,
                    null
            ));
            return new HeaderDetectionResult(null, Map.of(), warnings);
        }

        return new HeaderDetectionResult(bestRowIndex, bestHeaders, warnings);
    }

    public static String normalizeHeaderName(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return HEADER_ALIASES.getOrDefault(normalized, normalized);
    }

    private static Map<String, Integer> normalizeHeaders(
            Map<Integer, String> row,
            SheetDescriptor sheetDescriptor,
            List<ParserWarning> warnings
    ) {
        Map<String, Integer> headers = new HashMap<>();
        row.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String normalized = normalizeHeaderName(entry.getValue());
                    if (normalized.isBlank()) {
                        return;
                    }
                    if (headers.containsKey(normalized)) {
                        warnings.add(new ParserWarning(
                                WarningSeverity.WARNING,
                                "Duplicate or ambiguous header detected",
                                sheetDescriptor.workbookName(),
                                sheetDescriptor.sheetName(),
                                null,
                                String.valueOf(entry.getKey()),
                                entry.getValue()
                        ));
                        return;
                    }
                    headers.put(normalized, entry.getKey());
                });
        return headers;
    }

    private static int score(Map<String, Integer> headers) {
        int score = 0;
        if (headers.containsKey("name")) {
            score += 3;
        }
        if (headers.containsKey("class")) {
            score += 3;
        }
        if (headers.containsKey("description")) {
            score += 2;
        }
        if (headers.containsKey("parent ci")) {
            score += 2;
        }
        if (headers.containsKey("relationship")) {
            score += 2;
        }
        if (headers.containsKey("configuration item")) {
            score += 3;
        }
        if (headers.containsKey("node (source)")) {
            score += 2;
        }
        return score;
    }
}
