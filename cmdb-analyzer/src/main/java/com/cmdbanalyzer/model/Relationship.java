package com.cmdbanalyzer.model;

/**
 * Relationship edge between two CIs, preserving raw source context.
 *
 * @param id internal stable identifier assigned by the parser/import session
 * @param sourceCiId source CI identifier
 * @param targetCiId resolved target CI identifier, when available
 * @param targetName raw target CI name from the workbook
 * @param relationshipType normalized relationship type
 * @param rawRelationshipType original relationship type as found in Excel
 * @param sourceWorkbook source workbook name or path
 * @param sourceSheet source worksheet name
 * @param sourceRow one-based source row number
 * @param status resolution/malformed status
 */
public record Relationship(
        String id,
        String sourceCiId,
        String targetCiId,
        String targetName,
        String relationshipType,
        String rawRelationshipType,
        String sourceWorkbook,
        String sourceSheet,
        int sourceRow,
        RelationshipStatus status
) {
}
