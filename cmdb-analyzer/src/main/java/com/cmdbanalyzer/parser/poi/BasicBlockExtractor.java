package com.cmdbanalyzer.parser.poi;

import com.cmdbanalyzer.model.CmdbBlock;
import com.cmdbanalyzer.parser.BlockExtractor;
import com.cmdbanalyzer.parser.HeaderDetectionResult;
import com.cmdbanalyzer.parser.SheetDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extracts simple variable-sized CI blocks from descriptor rows.
 */
public class BasicBlockExtractor implements BlockExtractor {

    @Override
    public List<CmdbBlock> extractBlocks(SheetDescriptor sheetDescriptor, HeaderDetectionResult headers) {
        List<CmdbBlock> blocks = new ArrayList<>();
        Integer headerRowIndex = headers.headerRowIndex();
        if (headerRowIndex == null) {
            return blocks;
        }

        Integer currentCiRow = null;
        List<Integer> relationshipRows = new ArrayList<>();

        for (int rowIndex = headerRowIndex + 1; rowIndex <= sheetDescriptor.sampleRows().size(); rowIndex++) {
            Map<Integer, String> row = sheetDescriptor.sampleRows().get(rowIndex - 1);
            if (isBlankRow(row)) {
                if (currentCiRow != null) {
                    blocks.add(new CmdbBlock(currentCiRow, relationshipRows, rowIndex, sheetDescriptor.sheetName()));
                    currentCiRow = null;
                    relationshipRows = new ArrayList<>();
                }
                continue;
            }

            if (currentCiRow == null) {
                currentCiRow = rowIndex;
            } else {
                relationshipRows.add(rowIndex);
            }
        }

        if (currentCiRow != null) {
            blocks.add(new CmdbBlock(currentCiRow, relationshipRows, null, sheetDescriptor.sheetName()));
        }

        return blocks;
    }

    public boolean isBlankRow(Map<Integer, String> row) {
        return row == null || row.values().stream().allMatch(value -> value == null || value.trim().isEmpty());
    }
}
