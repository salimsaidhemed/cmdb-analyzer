package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.CmdbWorkbook;

import java.nio.file.Path;

/**
 * Entry point for converting an Excel workbook into a library-neutral CMDB workbook model.
 *
 * <p>Future implementations should coordinate workbook loading, sheet classification, header
 * detection, block extraction, CI creation, relationship creation, and relationship resolution.
 * Implementations must keep IO and Apache POI concerns outside the domain model.</p>
 */
public interface WorkbookParser {

    /**
     * Parses the supplied workbook path into the CMDB workbook model.
     *
     * @param sourceFile path to the source workbook
     * @return parsed workbook container with warnings and traceability
     */
    CmdbWorkbook parse(Path sourceFile);
}
