package com.caseware.updates.domain;

import java.util.List;

/**
 * The accumulated gap between an engagement's baseline and the latest published version.
 *
 * <p>A single pending update, not one per release. {@code pendingVersions} keeps the releases the
 * user has not taken so the UI can offer a per-release breakdown, but the decision the user makes
 * is always fromVersion to toVersion.
 */
public record PendingUpdate(
        String templateId,
        int fromVersion,
        int toVersion,
        List<TemplateVersion> pendingVersions,
        SummaryState summary) {

    public PendingUpdate {
        pendingVersions = List.copyOf(pendingVersions);
    }

    public int versionsBehind() {
        return pendingVersions.size();
    }

    public boolean hasAccumulated() {
        return versionsBehind() > 1;
    }
}
