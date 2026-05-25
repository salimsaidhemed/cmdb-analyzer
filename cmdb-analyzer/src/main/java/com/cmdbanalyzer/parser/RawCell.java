package com.cmdbanalyzer.parser;

/**
 * Library-neutral cell value used by parser contracts.
 *
 * @param rowIndex one-based row index
 * @param columnIndex one-based column index
 * @param value raw display or parsed value supplied by a future Excel adapter
 */
public record RawCell(int rowIndex, int columnIndex, String value) {
}
