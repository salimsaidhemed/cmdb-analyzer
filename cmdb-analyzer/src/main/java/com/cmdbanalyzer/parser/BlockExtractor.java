package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.CmdbBlock;

import java.util.List;

/**
 * Extracts variable-sized CI blocks from block-based worksheets.
 *
 * <p>A CI block is expected to contain one CI row followed by zero or more relationship rows
 * and an optional blank separator row. Implementations should tolerate malformed blocks and
 * emit warnings instead of failing the whole sheet.</p>
 */
public interface BlockExtractor {

    /**
     * Extracts CI block boundaries from a worksheet.
     *
     * @param sheetDescriptor parser-neutral worksheet descriptor
     * @param headers detected header information
     * @return extracted CI block row boundaries
     */
    List<CmdbBlock> extractBlocks(SheetDescriptor sheetDescriptor, HeaderDetectionResult headers);
}
