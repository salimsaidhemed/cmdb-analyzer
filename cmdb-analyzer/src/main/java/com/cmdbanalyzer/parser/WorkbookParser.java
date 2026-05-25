package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.CmdbWorkbook;

import java.nio.file.Path;

/**
 * Entry point for parsing a CMDB workbook file into a domain workbook model.
 *
 * <p>Implementations may use Apache POI internally in a later feature, but the contract exposes
 * only Java standard library and CMDB domain types.</p>
 */
public interface WorkbookParser {

    /**
     * Parses the supplied workbook path.
     *
     * @param workbookPath path to the source workbook
     * @return parse result containing a workbook, parser warnings, and error state
     */
    ParseResult<CmdbWorkbook> parse(Path workbookPath);
}
