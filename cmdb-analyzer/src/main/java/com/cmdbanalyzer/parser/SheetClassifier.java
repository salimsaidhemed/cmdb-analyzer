package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.SheetType;

/**
 * Classifies worksheets before content extraction.
 *
 * <p>Implementations should use worksheet name, detected headers, and row shape to distinguish
 * block-based CI sheets from flat CI lists, event binding sheets, lookup/reference sheets,
 * metadata sheets, and unknown content.</p>
 */
public interface SheetClassifier {

    /**
     * Determines the worksheet type from a raw sheet and detected headers.
     *
     * @param sheet raw worksheet snapshot
     * @param headers detected header information
     * @return classified sheet type
     */
    SheetType classify(RawSheet sheet, HeaderDetectionResult headers);
}
