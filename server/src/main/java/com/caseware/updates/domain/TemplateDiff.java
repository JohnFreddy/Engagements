package com.caseware.updates.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** A raw diff between two versions of one template. */
public record TemplateDiff(
        String templateId,
        int fromVersion,
        int toVersion,
        Instant generatedAt,
        List<ChangeOperation> changes) {

    public TemplateDiff {
        Objects.requireNonNull(templateId, "templateId");
        changes = List.copyOf(changes);
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }
}
