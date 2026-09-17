package com.caseware.updates.summary;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The human-readable form of a diff, produced on the server and cached under
 * (templateId, fromVersion, toVersion) so every firm sitting on the same pair reuses it.
 *
 * <p>{@code generatorVersion} is carried so any summary a practitioner saw can be reproduced
 * and explained later. That is a requirement in an audit product, not a nicety.
 */
public record ChangeSummary(
        String templateId,
        int fromVersion,
        int toVersion,
        String headline,
        Map<ChangeCategory, Integer> countsByCategory,
        List<ChangeItem> items,
        Instant generatedAt,
        String generatorVersion) {

    public ChangeSummary {
        countsByCategory = Map.copyOf(countsByCategory);
        items = List.copyOf(items);
    }

    /** What kind of change this is, for grouping and for deciding what to lead with. */
    public enum ChangeCategory {
        NEW_CONTENT,
        REMOVED_CONTENT,
        THRESHOLD_CHANGE,
        WORDING_CHANGE,
        METADATA
    }

    /** What the change means for the practitioner's workload or bar. */
    public enum Impact {
        ADDS_WORK,
        REMOVES_WORK,
        TIGHTENS,
        RELAXES,
        NEUTRAL
    }

    /**
     * One user-facing change. {@code sourcePath} is retained so the UI can always drill from a
     * sentence back to the raw diff entry it came from.
     */
    public record ChangeItem(
            ChangeCategory category,
            Impact impact,
            String sectionLabel,
            String title,
            String detail,
            String sourcePath) {
    }
}
