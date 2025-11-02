package com.cmdb.analyzer.core;

import java.time.Instant;
import java.util.Objects;

/**
 * Thread-safe metadata describing the provenance of a CMDB record
 * parsed from an Excel sheet.
 * <p>
 * Each instance of {@code ImportMetadata} captures contextual
 * information such as the source file, sheet name, and row indices
 * corresponding to a single Configuration Item (CI) and its
 * associated relationship definition.
 * <p>
 * This class is thread-safe: all mutator methods are synchronized
 * to prevent concurrent updates from importer or validator threads.
 *
 * <pre>
 * Example:
 *     ImportMetadata meta = new ImportMetadata();
 *     meta.setSourceFile("CD CMDB - Factories.xlsx");
 *     meta.setSheetName("Servers");
 *     meta.setRowIndexEntity(42);
 *     meta.setRowIndexRelation(43);
 * </pre>
 */
public class ImportMetadata {

    /** The Excel file name or path from which the record was imported. */
    private String sourceFile;

    /** The name of the worksheet in the Excel file. */
    private String sheetName;

    /**
     * Row index containing the CI definition (0-based or 1-based depending on
     * parser).
     */
    private int rowIndexEntity = -1;

    /** Row index containing the relationship definition, if any. */
    private int rowIndexRelation = -1;

    /** Timestamp when this metadata record was created. */
    private Instant importedAt = Instant.now();

    /**
     * Returns the source file name or path.
     *
     * @return the Excel file name, or {@code null} if not set
     */
    public synchronized String getSourceFile() {
        return sourceFile;
    }

    /**
     * Sets the source file name or path.
     *
     * @param sourceFile the Excel file name
     */
    public synchronized void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Returns the worksheet name from which this record was parsed.
     *
     * @return the worksheet name, or {@code null} if not set
     */
    public synchronized String getSheetName() {
        return sheetName;
    }

    /**
     * Sets the worksheet name from which this record was parsed.
     *
     * @param sheetName the worksheet name
     */
    public synchronized void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * Returns the row index of the entity (main CI record).
     *
     * @return the entity row index, or {@code -1} if undefined
     */
    public synchronized int getRowIndexEntity() {
        return rowIndexEntity;
    }

    /**
     * Sets the row index of the entity (main CI record).
     *
     * @param rowIndexEntity the row index
     */
    public synchronized void setRowIndexEntity(int rowIndexEntity) {
        this.rowIndexEntity = rowIndexEntity;
    }

    /**
     * Returns the row index of the related relationship entry.
     *
     * @return the relationship row index, or {@code -1} if none
     */
    public synchronized int getRowIndexRelation() {
        return rowIndexRelation;
    }

    /**
     * Sets the row index of the relationship entry associated with this CI.
     *
     * @param rowIndexRelation the relationship row index
     */
    public synchronized void setRowIndexRelation(int rowIndexRelation) {
        this.rowIndexRelation = rowIndexRelation;
    }

    /**
     * Returns the timestamp when this metadata was created or last updated.
     *
     * @return creation timestamp
     */
    public synchronized Instant getImportedAt() {
        return importedAt;
    }

    /**
     * Sets the import timestamp. A defensive copy is made to protect immutability.
     *
     * @param importedAt timestamp of import
     */
    public synchronized void setImportedAt(Instant importedAt) {
        this.importedAt = (importedAt != null ? Instant.from(importedAt) : Instant.now());
    }

    @Override
    public synchronized String toString() {
        return String.format(
                "ImportMetadata[file=%s, sheet=%s, entityRow=%d, relationRow=%d, importedAt=%s]",
                sourceFile, sheetName, rowIndexEntity, rowIndexRelation, importedAt);
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImportMetadata))
            return false;
        ImportMetadata that = (ImportMetadata) o;
        return rowIndexEntity == that.rowIndexEntity
                && rowIndexRelation == that.rowIndexRelation
                && Objects.equals(sourceFile, that.sourceFile)
                && Objects.equals(sheetName, that.sheetName)
                && Objects.equals(importedAt, that.importedAt);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(sourceFile, sheetName, rowIndexEntity, rowIndexRelation, importedAt);
    }
}
