package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.CmdbBlock;
import com.cmdbanalyzer.model.ParserWarning;

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
     * @param sheet raw worksheet snapshot
     * @param headers detected header information
     * @return block extraction result containing blocks and warnings
     */
    BlockExtractionResult extractBlocks(RawSheet sheet, HeaderDetectionResult headers);

    /**
     * Result of CI block extraction.
     *
     * @param blocks extracted CI blocks
     * @param warnings diagnostics for malformed or ambiguous row groups
     */
    record BlockExtractionResult(List<CmdbBlock> blocks, List<ParserWarning> warnings) {
        public BlockExtractionResult {
            blocks = List.copyOf(blocks);
            warnings = List.copyOf(warnings);
        }
    }
}
