package com.cmdbanalyzer.model;

import java.util.List;

/**
 * Represents a variable-sized CI block in a block-based worksheet.
 *
 * @param ciRowIndex one-based row containing the CI definition
 * @param relationshipRowIndexes one-based rows containing relationships for the CI
 * @param separatorRowIndex one-based blank separator row, if present
 * @param sourceSheetName worksheet where the block was found
 */
public record CmdbBlock(
        int ciRowIndex,
        List<Integer> relationshipRowIndexes,
        Integer separatorRowIndex,
        String sourceSheetName
) {
    public CmdbBlock {
        relationshipRowIndexes = List.copyOf(relationshipRowIndexes);
    }
}
