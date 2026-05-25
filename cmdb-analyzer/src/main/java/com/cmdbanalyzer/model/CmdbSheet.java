package com.cmdbanalyzer.model;

import java.util.List;
import java.util.Map;

/**
 * Parsed representation of one worksheet, independent of Excel libraries.
 *
 * @param name worksheet name
 * @param type detected sheet type
 * @param headerRowIndex one-based header row index, when detected
 * @param headerMap normalized header names mapped to one-based column indexes
 * @param ciBlocks extracted CI blocks for block-based sheets
 * @param flatRows flat tabular rows for non-block sheets, keyed by normalized header
 * @param warnings sheet-level parser diagnostics
 */
public record CmdbSheet(
        String name,
        SheetType type,
        Integer headerRowIndex,
        Map<String, Integer> headerMap,
        List<CmdbBlock> ciBlocks,
        List<Map<String, String>> flatRows,
        List<ParserWarning> warnings
) {
    public CmdbSheet {
        headerMap = Map.copyOf(headerMap);
        ciBlocks = List.copyOf(ciBlocks);
        flatRows = flatRows.stream()
                .map(Map::copyOf)
                .toList();
        warnings = List.copyOf(warnings);
    }
}
