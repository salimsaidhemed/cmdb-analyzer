package com.cmdbanalyzer.analyzer.validation;

/**
 * Categorizes validation findings for filtering, reporting, and future remediation workflows.
 */
public enum ValidationIssueType {
    MISSING_CI_NAME,
    MISSING_CI_CLASS,
    DUPLICATE_CI_IDENTITY,
    UNRESOLVED_RELATIONSHIP,
    MALFORMED_RELATIONSHIP,
    SELF_REFERENCE,
    ORPHAN_CI,
    CIRCULAR_DEPENDENCY,
    DUPLICATE_RELATIONSHIP,
    ISOLATED_SUBGRAPH,
    MISSING_GRAPH_EDGE,
    UNKNOWN_SHEET_TYPE,
    PARSER_WARNING
}
