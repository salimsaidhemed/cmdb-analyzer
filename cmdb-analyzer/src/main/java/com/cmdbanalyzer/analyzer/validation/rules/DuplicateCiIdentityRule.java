package com.cmdbanalyzer.analyzer.validation.rules;

import com.cmdbanalyzer.analyzer.validation.ValidationIssue;
import com.cmdbanalyzer.analyzer.validation.ValidationIssueType;
import com.cmdbanalyzer.analyzer.validation.ValidationRule;
import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Detects duplicate configuration item identity keys.
 */
public class DuplicateCiIdentityRule implements ValidationRule {

    @Override
    public List<ValidationIssue> validate(CmdbWorkbook workbook) {
        Map<String, List<ConfigurationItem>> itemsByIdentity = workbook.getSheets().stream()
                .flatMap(sheet -> sheet.getConfigurationItems().stream())
                .filter(item -> !isBlank(item.getIdentityKey()))
                .collect(Collectors.groupingBy(item -> item.getIdentityKey().trim().toLowerCase()));

        return itemsByIdentity.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .flatMap(entry -> entry.getValue().stream()
                        .map(item -> ValidationIssueFactory.ciIssue(
                                ValidationSeverity.ERROR,
                                ValidationIssueType.DUPLICATE_CI_IDENTITY,
                                "Duplicate CI identity detected: " + item.getIdentityKey(),
                                item,
                                "Ensure each CI identity is unique across the imported workbook."
                        )))
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
