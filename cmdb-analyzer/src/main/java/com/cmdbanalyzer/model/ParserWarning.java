package com.cmdbanalyzer.model;

import java.util.Objects;

/**
 * Diagnostic emitted while interpreting workbook, sheet, row, or cell content.
 */
public class ParserWarning {

    private WarningSeverity severity;
    private String message;
    private String workbook;
    private String sheet;
    private Integer row;
    private String column;
    private String rawValue;

    public ParserWarning() {
        this(WarningSeverity.INFO, null, null, null, null, null, null);
    }

    public ParserWarning(
            WarningSeverity severity,
            String message,
            String workbook,
            String sheet,
            Integer row,
            String column,
            String rawValue
    ) {
        this.severity = severity == null ? WarningSeverity.INFO : severity;
        this.message = message;
        this.workbook = workbook;
        this.sheet = sheet;
        this.row = row;
        this.column = column;
        this.rawValue = rawValue;
    }

    public WarningSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(WarningSeverity severity) {
        this.severity = severity == null ? WarningSeverity.INFO : severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWorkbook() {
        return workbook;
    }

    public void setWorkbook(String workbook) {
        this.workbook = workbook;
    }

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParserWarning that)) {
            return false;
        }
        return severity == that.severity
                && Objects.equals(message, that.message)
                && Objects.equals(workbook, that.workbook)
                && Objects.equals(sheet, that.sheet)
                && Objects.equals(row, that.row)
                && Objects.equals(column, that.column)
                && Objects.equals(rawValue, that.rawValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(severity, message, workbook, sheet, row, column, rawValue);
    }
}
