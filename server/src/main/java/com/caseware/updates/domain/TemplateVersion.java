package com.caseware.updates.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * One published version of a product template.
 *
 * <p>{@code number} is an identifier, not an ordinal. The fixtures start at v3, v6 and v10,
 * so ordering comes from the catalog's published sequence, never from arithmetic on this field.
 */
public record TemplateVersion(String templateId, int number, Instant publishedAt) {
    public TemplateVersion {
        Objects.requireNonNull(templateId, "templateId");
        Objects.requireNonNull(publishedAt, "publishedAt");
    }
}
