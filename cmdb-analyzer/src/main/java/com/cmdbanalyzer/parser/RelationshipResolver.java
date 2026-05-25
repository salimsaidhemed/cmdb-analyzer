package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.CmdbWorkbook;

/**
 * Resolves imported relationship target names to known Configuration Item identifiers.
 *
 * <p>Implementations should support unresolved targets, duplicate CI names, malformed
 * relationship rows, raw relationship-type preservation, and graph-friendly relationship
 * statuses.</p>
 */
public interface RelationshipResolver {

    /**
     * Resolves relationships contained in a parsed workbook.
     *
     * @param workbook parsed workbook containing sheets, CIs, relationships, and warnings
     * @return aggregate relationship resolution counts and warnings
     */
    RelationshipResolutionResult resolve(CmdbWorkbook workbook);
}
