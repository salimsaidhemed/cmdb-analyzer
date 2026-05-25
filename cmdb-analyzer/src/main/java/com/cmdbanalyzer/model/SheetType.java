package com.cmdbanalyzer.model;

/**
 * High-level worksheet categories detected before parsing sheet content.
 */
public enum SheetType {
    CI_BLOCK,
    FLAT_CI_LIST,
    EVENT_BINDING,
    LOOKUP,
    METADATA,
    UNKNOWN
}
