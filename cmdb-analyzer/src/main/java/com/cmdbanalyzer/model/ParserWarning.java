package com.cmdbanalyzer.model;

/**
 * Non-fatal or fatal diagnostic emitted while parsing workbook content.
 *
 * @param severity severity level
 * @param message human-readable diagnostic message
 * @param workbook source workbook name or path
 * @param sheet source worksheet name, when known
 * @param row one-based source row number, when known
 * @param column one-based source column number or header name, when known
 * @param rawValue original cell value related to the warning, when useful
 */
public record ParserWarning(
        WarningSeverity severity,
        String message,
        String workbook,
        String sheet,
        Integer row,
        String column,
        String rawValue
) {
}
