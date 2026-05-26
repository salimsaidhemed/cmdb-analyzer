package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.analyzer.validation.ValidationContext;
import com.cmdbanalyzer.model.SheetType;

import java.util.List;

/**
 * Detects sheets that could not be classified by the parser.
 */
public class UnknownSheetTypeRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        var workbook = context.workbook();
        return workbook.getSheets().stream()
                .filter(sheet -> sheet.getType() == SheetType.UNKNOWN)
                .map(sheet -> new ValidationIssue(
                        null,
                        ValidationSeverity.INFO,
                        ValidationIssueType.UNKNOWN_SHEET_TYPE,
                        "Sheet type is unknown: " + sheet.getName(),
                        workbook.getSourceFile(),
                        sheet.getName(),
                        sheet.getHeaderRowIndex(),
                        null,
                        null,
                        "Review the sheet and decide whether it should be mapped to a supported CMDB sheet type."
                ))
                .toList();
    }
}
