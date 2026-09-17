package com.caseware.updates.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Catalog backed by an ordered version list per template, mirroring data/templates.json. */
public final class InMemoryTemplateCatalog implements TemplateCatalog {

    private final Map<String, List<TemplateVersion>> byTemplate = new LinkedHashMap<>();

    public InMemoryTemplateCatalog(List<TemplateVersion> versions) {
        for (TemplateVersion v : versions) {
            byTemplate.computeIfAbsent(v.templateId(), k -> new ArrayList<>()).add(v);
        }
        byTemplate.values().forEach(list -> list.sort(Comparator.comparing(TemplateVersion::publishedAt)));
    }

    @Override
    public Optional<TemplateVersion> latest(String templateId) {
        List<TemplateVersion> ordered = byTemplate.getOrDefault(templateId, List.of());
        return ordered.isEmpty() ? Optional.empty() : Optional.of(ordered.getLast());
    }

    @Override
    public boolean contains(String templateId, int version) {
        return indexOf(templateId, version) >= 0;
    }

    @Override
    public List<TemplateVersion> versionsAfter(String templateId, int baselineVersion) {
        List<TemplateVersion> ordered = byTemplate.getOrDefault(templateId, List.of());
        int idx = indexOf(templateId, baselineVersion);
        if (idx < 0) {
            return List.of();
        }
        return List.copyOf(ordered.subList(idx + 1, ordered.size()));
    }

    private int indexOf(String templateId, int version) {
        List<TemplateVersion> ordered = byTemplate.getOrDefault(templateId, List.of());
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).number() == version) {
                return i;
            }
        }
        return -1;
    }
}
