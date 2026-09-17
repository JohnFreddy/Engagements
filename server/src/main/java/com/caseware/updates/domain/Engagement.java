package com.caseware.updates.domain;

import java.util.Objects;

/**
 * The projection row for an engagement file. Sourced from create/apply/decline events,
 * never by loading the engagement itself (~1 minute per file).
 *
 * <p>{@code baselineVersion} is the template version this engagement currently sits on.
 */
public record Engagement(String engagementId, String name, String templateId, int baselineVersion) {
    public Engagement {
        Objects.requireNonNull(engagementId, "engagementId");
        Objects.requireNonNull(templateId, "templateId");
    }
}
