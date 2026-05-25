package com.cmdbanalyzer.parser.poi;

import com.cmdbanalyzer.model.CmdbBlock;
import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.FlatRow;
import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.model.Relationship;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.model.SheetType;
import com.cmdbanalyzer.model.WarningSeverity;
import com.cmdbanalyzer.parser.BlockExtractor;
import com.cmdbanalyzer.parser.HeaderDetectionResult;
import com.cmdbanalyzer.parser.HeaderDetector;
import com.cmdbanalyzer.parser.ParseResult;
import com.cmdbanalyzer.parser.SheetClassifier;
import com.cmdbanalyzer.parser.SheetDescriptor;
import com.cmdbanalyzer.parser.WorkbookParser;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic Apache POI-backed workbook parser for CMDB Excel files.
 */
public class PoiWorkbookParser implements WorkbookParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoiWorkbookParser.class);

    private final SheetClassifier sheetClassifier;
    private final HeaderDetector headerDetector;
    private final BlockExtractor blockExtractor;

    public PoiWorkbookParser() {
        this(new BasicSheetClassifier(), new BasicHeaderDetector(), new BasicBlockExtractor());
    }

    public PoiWorkbookParser(
            SheetClassifier sheetClassifier,
            HeaderDetector headerDetector,
            BlockExtractor blockExtractor
    ) {
        this.sheetClassifier = sheetClassifier;
        this.headerDetector = headerDetector;
        this.blockExtractor = blockExtractor;
    }

    @Override
    public ParseResult<CmdbWorkbook> parse(Path workbookPath) {
        List<ParserWarning> warnings = new ArrayList<>();

        try (InputStream inputStream = Files.newInputStream(workbookPath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            String sourceFile = workbookPath.toString();
            String workbookName = workbookPath.getFileName().toString();
            CmdbWorkbook cmdbWorkbook = new CmdbWorkbook(sourceFile, Instant.now(), new ArrayList<>(), warnings);

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                SheetDescriptor descriptor = PoiSheetDescriptors.fromSheet(workbookName, sheet);
                HeaderDetectionResult headers = headerDetector.detectHeaders(descriptor);
                SheetType sheetType = sheetClassifier.classify(descriptor);

                CmdbSheet cmdbSheet = new CmdbSheet();
                cmdbSheet.setName(sheet.getSheetName());
                cmdbSheet.setType(sheetType);
                cmdbSheet.setHeaderRowIndex(headers.headerRowIndex());
                cmdbSheet.setHeaderMap(headers.headerMap());
                headers.warnings().forEach(cmdbSheet::addWarning);
                warnings.addAll(headers.warnings());

                if (sheetType == SheetType.CI_BLOCK) {
                    parseBlockSheet(sourceFile, descriptor, headers, cmdbSheet, warnings);
                } else if (sheetType == SheetType.FLAT_CI_LIST || sheetType == SheetType.EVENT_BINDING || sheetType == SheetType.LOOKUP) {
                    ParserWarning warning = warning(
                            WarningSeverity.INFO,
                            "Deep flat sheet parsing is deferred",
                            sourceFile,
                            sheet.getSheetName(),
                            headers.headerRowIndex(),
                            null,
                            sheetType.name()
                    );
                    cmdbSheet.addWarning(warning);
                    warnings.add(warning);
                    parseFlatRows(descriptor, headers, cmdbSheet);
                } else if (sheetType == SheetType.UNKNOWN) {
                    ParserWarning warning = warning(
                            WarningSeverity.WARNING,
                            "Unknown sheet type",
                            sourceFile,
                            sheet.getSheetName(),
                            headers.headerRowIndex(),
                            null,
                            null
                    );
                    cmdbSheet.addWarning(warning);
                    warnings.add(warning);
                }

                cmdbWorkbook.addSheet(cmdbSheet);
            }

            cmdbWorkbook.setParserWarnings(warnings);
            return ParseResult.success(cmdbWorkbook, warnings);
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Could not parse workbook {}: {}", workbookPath, exception.getMessage());
            return ParseResult.failure("Could not open or read workbook: " + exception.getMessage(), warnings);
        }
    }

    private void parseBlockSheet(
            String sourceFile,
            SheetDescriptor descriptor,
            HeaderDetectionResult headers,
            CmdbSheet cmdbSheet,
            List<ParserWarning> warnings
    ) {
        List<CmdbBlock> blocks = blockExtractor.extractBlocks(descriptor, headers);
        if (blocks.isEmpty()) {
            ParserWarning warning = warning(
                    WarningSeverity.WARNING,
                    "No CI blocks found in CI block sheet",
                    sourceFile,
                    descriptor.sheetName(),
                    headers.headerRowIndex(),
                    null,
                    null
            );
            cmdbSheet.addWarning(warning);
            warnings.add(warning);
            return;
        }

        blocks.forEach(cmdbSheet::addCiBlock);
        for (CmdbBlock block : blocks) {
            ConfigurationItem item = createConfigurationItem(sourceFile, descriptor, headers, block.getCiRowIndex());
            if (isBlank(item.getName()) || isBlank(item.getCiClass())) {
                ParserWarning warning = warning(
                        WarningSeverity.WARNING,
                        "CI row is missing name or class",
                        sourceFile,
                        descriptor.sheetName(),
                        block.getCiRowIndex(),
                        null,
                        null
                );
                cmdbSheet.addWarning(warning);
                warnings.add(warning);
            }
            cmdbSheet.addConfigurationItem(item);

            for (Integer relationshipRowIndex : block.getRelationshipRowIndexes()) {
                Relationship relationship = createRelationship(sourceFile, descriptor, headers, item, relationshipRowIndex);
                cmdbSheet.addRelationship(relationship);
                if (relationship.getStatus() == RelationshipStatus.MALFORMED) {
                    ParserWarning warning = warning(
                            WarningSeverity.WARNING,
                            "Malformed relationship row",
                            sourceFile,
                            descriptor.sheetName(),
                            relationshipRowIndex,
                            null,
                            relationship.getRawRelationshipType()
                    );
                    cmdbSheet.addWarning(warning);
                    warnings.add(warning);
                }
            }
        }
    }

    private ConfigurationItem createConfigurationItem(
            String sourceFile,
            SheetDescriptor descriptor,
            HeaderDetectionResult headers,
            int rowIndex
    ) {
        Map<String, String> values = valuesForRow(descriptor, headers, rowIndex);
        String name = values.get("name");
        String ciClass = values.get("class");
        String description = values.get("description");
        Map<String, String> attributes = new HashMap<>(values);
        attributes.remove("name");
        attributes.remove("class");
        attributes.remove("description");
        attributes.remove("parent ci");
        attributes.remove("relationship");

        return new ConfigurationItem(
                null,
                name,
                ciClass,
                description,
                attributes,
                sourceFile,
                descriptor.sheetName(),
                rowIndex,
                null
        );
    }

    private Relationship createRelationship(
            String sourceFile,
            SheetDescriptor descriptor,
            HeaderDetectionResult headers,
            ConfigurationItem sourceItem,
            int rowIndex
    ) {
        Map<String, String> values = valuesForRow(descriptor, headers, rowIndex);
        String targetName = values.get("parent ci");
        String rawRelationshipType = values.get("relationship");
        String relationshipType = normalizeRelationshipType(rawRelationshipType);
        RelationshipStatus status = isBlank(targetName) || isBlank(rawRelationshipType)
                ? RelationshipStatus.MALFORMED
                : RelationshipStatus.UNRESOLVED;

        return new Relationship(
                null,
                sourceItem.getId(),
                null,
                targetName,
                relationshipType,
                rawRelationshipType,
                sourceFile,
                descriptor.sheetName(),
                rowIndex,
                status
        );
    }

    private void parseFlatRows(SheetDescriptor descriptor, HeaderDetectionResult headers, CmdbSheet cmdbSheet) {
        if (headers.headerRowIndex() == null) {
            return;
        }
        for (int rowIndex = headers.headerRowIndex() + 1; rowIndex <= descriptor.sampleRows().size(); rowIndex++) {
            Map<String, String> values = valuesForRow(descriptor, headers, rowIndex);
            if (!values.isEmpty()) {
                cmdbSheet.addFlatRow(new FlatRow(descriptor.sheetName(), rowIndex, values));
            }
        }
    }

    private Map<String, String> valuesForRow(SheetDescriptor descriptor, HeaderDetectionResult headers, int rowIndex) {
        if (rowIndex < 1 || rowIndex > descriptor.sampleRows().size()) {
            return Map.of();
        }
        Map<Integer, String> row = descriptor.sampleRows().get(rowIndex - 1);
        Map<String, String> values = new HashMap<>();
        headers.headerMap().forEach((header, columnIndex) -> {
            String value = row.get(columnIndex);
            if (!isBlank(value)) {
                values.put(header, value.trim());
            }
        });
        return values;
    }

    private static String normalizeRelationshipType(String rawRelationshipType) {
        if (isBlank(rawRelationshipType)) {
            return null;
        }
        return rawRelationshipType.trim().replaceAll("\\s+", " ");
    }

    private static ParserWarning warning(
            WarningSeverity severity,
            String message,
            String workbook,
            String sheet,
            Integer row,
            String column,
            String rawValue
    ) {
        return new ParserWarning(severity, message, workbook, sheet, row, column, rawValue);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
