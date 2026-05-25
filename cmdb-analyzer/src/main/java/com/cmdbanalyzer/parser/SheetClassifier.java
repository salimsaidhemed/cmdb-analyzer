package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.SheetType;

/**
 * Classifies worksheets before parsing their contents.
 *
 * <p>Implementations should use worksheet name, detected headers, and row shape to distinguish
 * block-based CI sheets from flat CI lists, event binding sheets, lookup/reference sheets,
 * metadata sheets, and unknown content.</p>
 */
public interface SheetClassifier {

    /**
     * Determines the worksheet type from parser-neutral worksheet metadata.
     *
     * @param sheetDescriptor parser-neutral worksheet descriptor
     * @return classified sheet type
     */
    SheetType classify(SheetDescriptor sheetDescriptor);
}
