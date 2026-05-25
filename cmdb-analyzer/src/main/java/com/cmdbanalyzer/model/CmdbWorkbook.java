package com.cmdbanalyzer.model;

import java.time.Instant;
import java.util.List;

/**
 * Parsed workbook container preserving source and parser diagnostics.
 *
 * @param sourceFile workbook file name or path
 * @param importedAt import timestamp
 * @param sheets parsed worksheet containers
 * @param parserWarnings workbook-level parser diagnostics
 */
public record CmdbWorkbook(
        String sourceFile,
        Instant importedAt,
        List<CmdbSheet> sheets,
        List<ParserWarning> parserWarnings
) {
    public CmdbWorkbook {
        sheets = List.copyOf(sheets);
        parserWarnings = List.copyOf(parserWarnings);
    }
}
