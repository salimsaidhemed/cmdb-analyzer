package com.cmdbanalyzer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Representation of one worksheet after sheet classification and structural extraction.
 */
public class CmdbSheet {

    private String name;
    private SheetType type;
    private Integer headerRowIndex;
    private Map<String, Integer> headerMap;
    private List<CmdbBlock> ciBlocks;
    private List<FlatRow> flatRows;
    private List<ParserWarning> warnings;

    public CmdbSheet() {
        this(null, SheetType.UNKNOWN, null, new HashMap<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public CmdbSheet(
            String name,
            SheetType type,
            Integer headerRowIndex,
            Map<String, Integer> headerMap,
            List<CmdbBlock> ciBlocks,
            List<FlatRow> flatRows,
            List<ParserWarning> warnings
    ) {
        this.name = name;
        this.type = type == null ? SheetType.UNKNOWN : type;
        this.headerRowIndex = headerRowIndex;
        setHeaderMap(headerMap);
        setCiBlocks(ciBlocks);
        setFlatRows(flatRows);
        setWarnings(warnings);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SheetType getType() {
        return type;
    }

    public void setType(SheetType type) {
        this.type = type == null ? SheetType.UNKNOWN : type;
    }

    public Integer getHeaderRowIndex() {
        return headerRowIndex;
    }

    public void setHeaderRowIndex(Integer headerRowIndex) {
        this.headerRowIndex = headerRowIndex;
    }

    public Map<String, Integer> getHeaderMap() {
        return headerMap;
    }

    public void setHeaderMap(Map<String, Integer> headerMap) {
        this.headerMap = new HashMap<>(safeMap(headerMap));
    }

    public List<CmdbBlock> getCiBlocks() {
        return ciBlocks;
    }

    public void setCiBlocks(List<CmdbBlock> ciBlocks) {
        this.ciBlocks = new ArrayList<>(safeList(ciBlocks));
    }

    public List<FlatRow> getFlatRows() {
        return flatRows;
    }

    public void setFlatRows(List<FlatRow> flatRows) {
        this.flatRows = new ArrayList<>(safeList(flatRows));
    }

    public List<ParserWarning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<ParserWarning> warnings) {
        this.warnings = new ArrayList<>(safeList(warnings));
    }

    public void addCiBlock(CmdbBlock ciBlock) {
        ciBlocks.add(ciBlock);
    }

    public void addFlatRow(FlatRow flatRow) {
        flatRows.add(flatRow);
    }

    public void addWarning(ParserWarning warning) {
        warnings.add(warning);
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static <K, V> Map<K, V> safeMap(Map<K, V> values) {
        return values == null ? Map.of() : values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CmdbSheet cmdbSheet)) {
            return false;
        }
        return Objects.equals(name, cmdbSheet.name)
                && type == cmdbSheet.type
                && Objects.equals(headerRowIndex, cmdbSheet.headerRowIndex)
                && Objects.equals(headerMap, cmdbSheet.headerMap)
                && Objects.equals(ciBlocks, cmdbSheet.ciBlocks)
                && Objects.equals(flatRows, cmdbSheet.flatRows)
                && Objects.equals(warnings, cmdbSheet.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, headerRowIndex, headerMap, ciBlocks, flatRows, warnings);
    }
}
