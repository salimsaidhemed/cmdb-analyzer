package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationContext;

import java.util.List;

/**
 * Converts parser diagnostics into validation issues so import quality is visible in one view.
 */
public class ParserWarningRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        var workbook = context.workbook();
        return workbook.getParserWarnings().stream()
                .map(ValidationIssueFactory::parserWarningIssue)
                .toList();
    }
}
