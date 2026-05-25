package com.cmdbanalyzer.parser;

/**
 * Detects worksheet headers without assuming they start in row 1 or column A.
 *
 * <p>Future implementations should normalize whitespace, tolerate leading blank columns,
 * report duplicate or unknown headers, and preserve one-based column indexes for source
 * traceability.</p>
 */
public interface HeaderDetector {

    /**
     * Finds and normalizes the header row for a worksheet.
     *
     * @param sheet raw worksheet snapshot
     * @return detected header row, normalized header map, and warnings
     */
    HeaderDetectionResult detectHeaders(RawSheet sheet);
}
