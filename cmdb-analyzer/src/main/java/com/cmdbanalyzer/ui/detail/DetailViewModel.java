package com.cmdbanalyzer.ui.detail;

import java.util.List;

/**
 * UI-neutral model for the right-side selection inspector.
 */
public record DetailViewModel(
        String title,
        String subtitle,
        String badge,
        DetailTone tone,
        List<DetailNotice> notices,
        List<DetailSection> sections
) {

    public DetailViewModel {
        notices = List.copyOf(notices == null ? List.of() : notices);
        sections = List.copyOf(sections == null ? List.of() : sections);
    }

    public static DetailViewModel empty() {
        return new DetailViewModel(
                "Nothing selected",
                "Select a CI, relationship, sheet, or issue to inspect its details.",
                "Ready",
                DetailTone.NEUTRAL,
                List.of(),
                List.of()
        );
    }

    public record DetailSection(String title, List<DetailField> fields) {
        public DetailSection {
            fields = List.copyOf(fields == null ? List.of() : fields);
        }
    }

    public record DetailField(String label, String value) {
    }

    public record DetailNotice(DetailTone tone, String message) {
    }

    public enum DetailTone {
        NEUTRAL,
        SUCCESS,
        WARNING,
        ERROR
    }
}
