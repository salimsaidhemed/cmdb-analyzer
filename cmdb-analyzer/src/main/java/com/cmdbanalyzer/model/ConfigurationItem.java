package com.cmdbanalyzer.model;

import java.util.Map;

/**
 * Framework-independent representation of a parsed Configuration Item.
 *
 * @param id internal stable identifier assigned by the parser/import session
 * @param name CMDB CI name
 * @param ciClass source CMDB class
 * @param description source description
 * @param attributes normalized non-core attributes keyed by source header
 * @param sourceWorkbook source workbook name or path
 * @param sourceSheet source worksheet name
 * @param sourceRow one-based source row number
 * @param identityKey parser-generated identity key for duplicate-aware lookup
 */
public record ConfigurationItem(
        String id,
        String name,
        String ciClass,
        String description,
        Map<String, String> attributes,
        String sourceWorkbook,
        String sourceSheet,
        int sourceRow,
        String identityKey
) {
    public ConfigurationItem {
        attributes = Map.copyOf(attributes);
    }
}
