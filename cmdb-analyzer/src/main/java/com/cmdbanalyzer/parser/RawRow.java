package com.cmdbanalyzer.parser;

import java.util.List;

/**
 * Library-neutral worksheet row used by parser contracts.
 *
 * @param rowIndex one-based row index
 * @param cells cells present in the row
 */
public record RawRow(int rowIndex, List<RawCell> cells) {
    public RawRow {
        cells = List.copyOf(cells);
    }
}
