package com.caseware.updates.domain;

import java.util.List;
import java.util.Optional;

/** Read-only view of the shared product template registry. */
public interface TemplateCatalog {

    Optional<TemplateVersion> latest(String templateId);

    boolean contains(String templateId, int version);

    /**
     * Versions published after {@code baselineVersion}, in publication order.
     * Empty when the baseline is already the latest, or is unknown to the catalog.
     */
    List<TemplateVersion> versionsAfter(String templateId, int baselineVersion);
}
