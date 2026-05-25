package com.cmdbanalyzer.parser;

import java.util.List;

/**
 * Library-neutral worksheet snapshot used by parser contracts.
 *
 * @param name worksheet name
 * @param rows rows supplied by a future workbook adapter
 */
public record RawSheet(String name, List<RawRow> rows) {
    public RawSheet {
        rows = List.copyOf(rows);
    }
}
