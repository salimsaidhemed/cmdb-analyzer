package com.cmdbanalyzer.service;

import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.model.Relationship;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.model.WarningSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Resolves relationship target names to parsed configuration item identifiers.
 */
public class RelationshipResolutionService {

    public RelationshipResolutionSummary resolve(CmdbWorkbook workbook) {
        Objects.requireNonNull(workbook, "workbook must not be null");

        CmdbIdentityIndex identityIndex = new CmdbIdentityIndex(workbook);
        List<ParserWarning> warnings = new ArrayList<>();
        int totalCount = 0;
        int resolvedCount = 0;
        int unresolvedCount = 0;
        int malformedCount = 0;
        int ambiguousCount = 0;

        for (CmdbSheet sheet : workbook.getSheets()) {
            for (Relationship relationship : sheet.getRelationships()) {
                totalCount++;

                if (isBlank(relationship.getTargetName())) {
                    relationship.setStatus(RelationshipStatus.MALFORMED);
                    malformedCount++;
                    ParserWarning warning = warning(
                            workbook,
                            relationship,
                            "Relationship target is blank",
                            relationship.getTargetName()
                    );
                    addWarning(workbook, sheet, warnings, warning);
                    continue;
                }

                if (relationship.getStatus() == RelationshipStatus.MALFORMED) {
                    malformedCount++;
                    continue;
                }

                List<ConfigurationItem> candidates = identityIndex.findCandidates(relationship.getTargetName());
                if (candidates.size() == 1) {
                    relationship.setTargetCiId(candidates.get(0).getId());
                    relationship.setStatus(RelationshipStatus.RESOLVED);
                    resolvedCount++;
                } else if (candidates.isEmpty()) {
                    relationship.setStatus(RelationshipStatus.UNRESOLVED);
                    unresolvedCount++;
                    ParserWarning warning = warning(
                            workbook,
                            relationship,
                            "Relationship target could not be resolved",
                            relationship.getTargetName()
                    );
                    addWarning(workbook, sheet, warnings, warning);
                } else {
                    relationship.setStatus(RelationshipStatus.UNRESOLVED);
                    unresolvedCount++;
                    ambiguousCount++;
                    ParserWarning warning = warning(
                            workbook,
                            relationship,
                            "Relationship target is ambiguous: " + candidates.size() + " matching CIs found",
                            relationship.getTargetName()
                    );
                    addWarning(workbook, sheet, warnings, warning);
                }
            }
        }

        return new RelationshipResolutionSummary(
                totalCount,
                resolvedCount,
                unresolvedCount,
                malformedCount,
                ambiguousCount,
                warnings
        );
    }

    private void addWarning(
            CmdbWorkbook workbook,
            CmdbSheet sheet,
            List<ParserWarning> warnings,
            ParserWarning warning
    ) {
        warnings.add(warning);
        workbook.addParserWarning(warning);
        sheet.addWarning(warning);
    }

    private ParserWarning warning(CmdbWorkbook workbook, Relationship relationship, String message, String rawValue) {
        return new ParserWarning(
                WarningSeverity.WARNING,
                message,
                workbook.getSourceFile(),
                relationship.getSourceSheet(),
                relationship.getSourceRow(),
                "targetName",
                rawValue
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
