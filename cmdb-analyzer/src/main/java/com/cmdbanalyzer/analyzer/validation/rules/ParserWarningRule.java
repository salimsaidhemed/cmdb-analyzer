package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.model.CmdbWorkbook;

import java.util.List;

/**
 * Converts parser diagnostics into validation issues so import quality is visible in one view.
 */
public class ParserWarningRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(CmdbWorkbook workbook) {
        return workbook.getParserWarnings().stream()
                .map(ValidationIssueFactory::parserWarningIssue)
                .toList();
    }
}
