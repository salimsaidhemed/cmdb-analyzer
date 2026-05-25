package com.cmdbanalyzer.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Top-level representation of one imported CMDB workbook.
 */
public class CmdbWorkbook {

    private String sourceFile;
    private Instant importedAt;
    private List<CmdbSheet> sheets;
    private List<ParserWarning> parserWarnings;

    public CmdbWorkbook() {
        this(null, Instant.now(), new ArrayList<>(), new ArrayList<>());
    }

    public CmdbWorkbook(
            String sourceFile,
            Instant importedAt,
            List<CmdbSheet> sheets,
            List<ParserWarning> parserWarnings
    ) {
        this.sourceFile = sourceFile;
        this.importedAt = importedAt;
        setSheets(sheets);
        setParserWarnings(parserWarnings);
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Instant getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(Instant importedAt) {
        this.importedAt = importedAt;
    }

    public List<CmdbSheet> getSheets() {
        return sheets;
    }

    public void setSheets(List<CmdbSheet> sheets) {
        this.sheets = new ArrayList<>(safeList(sheets));
    }

    public List<ParserWarning> getParserWarnings() {
        return parserWarnings;
    }

    public void setParserWarnings(List<ParserWarning> parserWarnings) {
        this.parserWarnings = new ArrayList<>(safeList(parserWarnings));
    }

    public void addSheet(CmdbSheet sheet) {
        sheets.add(sheet);
    }

    public void addParserWarning(ParserWarning parserWarning) {
        parserWarnings.add(parserWarning);
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CmdbWorkbook that)) {
            return false;
        }
        return Objects.equals(sourceFile, that.sourceFile)
                && Objects.equals(importedAt, that.importedAt)
                && Objects.equals(sheets, that.sheets)
                && Objects.equals(parserWarnings, that.parserWarnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFile, importedAt, sheets, parserWarnings);
    }
}
