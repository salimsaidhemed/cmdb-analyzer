package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.ParserWarning;

import java.util.List;
import java.util.Map;

/**
 * Result of locating and normalizing a worksheet header row.
 *
 * @param headerRowIndex one-based header row index, or null when no header was detected
 * @param headerMap normalized header names mapped to one-based column indexes
 * @param warnings diagnostics produced while detecting headers
 */
public record HeaderDetectionResult(
        Integer headerRowIndex,
        Map<String, Integer> headerMap,
        List<ParserWarning> warnings
) {
    public HeaderDetectionResult {
        headerMap = Map.copyOf(headerMap);
        warnings = List.copyOf(warnings);
    }
}
