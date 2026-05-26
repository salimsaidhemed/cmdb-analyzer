package com.cmdbanalyzer.analyzer.validation;

import com.cmdbanalyzer.model.CmdbWorkbook;

import java.util.List;

/**
 * Contract for one focused CMDB validation rule.
 */
public interface ValidationRule {

    default List<ValidationIssue> validate(CmdbWorkbook workbook) {
        return validate(ValidationContext.forWorkbook(workbook));
    }

    List<ValidationIssue> validate(ValidationContext context);
}
