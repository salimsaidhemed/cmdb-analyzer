package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.ParserWarning;

import java.util.List;

/**
 * Generic result wrapper for parser contract methods that can succeed with warnings or fail safely.
 *
 * @param result parsed result, when successful
 * @param warnings parser warnings gathered during the operation
 * @param success whether the operation completed successfully
 * @param errorMessage failure message, when unsuccessful
 * @param <T> parsed result type
 */
public record ParseResult<T>(
        T result,
        List<ParserWarning> warnings,
        boolean success,
        String errorMessage
) {
    public ParseResult {
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }

    public static <T> ParseResult<T> success(T result, List<ParserWarning> warnings) {
        return new ParseResult<>(result, warnings, true, null);
    }

    public static <T> ParseResult<T> failure(String errorMessage, List<ParserWarning> warnings) {
        return new ParseResult<>(null, warnings, false, errorMessage);
    }
}
