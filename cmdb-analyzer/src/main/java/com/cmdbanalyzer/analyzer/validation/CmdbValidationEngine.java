package com.cmdbanalyzer.analyzer.validation;

import com.cmdbanalyzer.analyzer.validation.rules.DuplicateCiIdentityRule;
import com.cmdbanalyzer.analyzer.validation.rules.MalformedRelationshipRule;
import com.cmdbanalyzer.analyzer.validation.rules.MissingCiClassRule;
import com.cmdbanalyzer.analyzer.validation.rules.MissingCiNameRule;
import com.cmdbanalyzer.analyzer.validation.rules.ParserWarningRule;
import com.cmdbanalyzer.analyzer.validation.rules.SelfReferenceRelationshipRule;
import com.cmdbanalyzer.analyzer.validation.rules.UnknownSheetTypeRule;
import com.cmdbanalyzer.analyzer.validation.rules.UnresolvedRelationshipRule;
import com.cmdbanalyzer.model.CmdbWorkbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Runs CMDB validation rules against a parsed workbook.
 */
public class CmdbValidationEngine {

    private final List<ValidationRule> rules;

    public CmdbValidationEngine() {
        this(List.of(
                new MissingCiNameRule(),
                new MissingCiClassRule(),
                new DuplicateCiIdentityRule(),
                new UnresolvedRelationshipRule(),
                new MalformedRelationshipRule(),
                new SelfReferenceRelationshipRule(),
                new UnknownSheetTypeRule(),
                new ParserWarningRule()
        ));
    }

    public CmdbValidationEngine(List<ValidationRule> rules) {
        this.rules = List.copyOf(rules == null ? List.of() : rules);
    }

    public ValidationResult validate(CmdbWorkbook workbook) {
        Objects.requireNonNull(workbook, "workbook must not be null");
        List<ValidationIssue> issues = new ArrayList<>();
        rules.forEach(rule -> issues.addAll(rule.validate(workbook)));
        return new ValidationResult(issues);
    }
}
