package com.caseware.updates.summary;

import java.util.Map;
import java.util.Optional;

/**
 * Section display names read from the target template version.
 *
 * <p>Diffs carry keys ({@code /sections/inquiries/...}) but not display names; the template
 * content carries {@code sections.planning.displayName = "Planning"}. Resolving a key to a name
 * therefore requires a read of the template itself, which is one of the reasons the summary is
 * built on the server rather than in the client.
 */
public record TemplateLabels(String templateId, int version, Map<String, String> sectionDisplayNames) {

    public TemplateLabels {
        sectionDisplayNames = Map.copyOf(sectionDisplayNames);
    }

    public static TemplateLabels none(String templateId, int version) {
        return new TemplateLabels(templateId, version, Map.of());
    }

    public Optional<String> sectionDisplayName(String sectionKey) {
        return Optional.ofNullable(sectionDisplayNames.get(sectionKey));
    }
}
