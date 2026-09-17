package com.caseware.updates.domain;

/**
 * What the engagement list needs to know about one engagement. Exhaustive by construction, so the
 * client contract cannot grow a fourth case without this failing to compile.
 */
public sealed interface UpdateStatus {

    record UpToDate(String templateId, int currentVersion) implements UpdateStatus {
    }

    record UpdateAvailable(PendingUpdate pendingUpdate) implements UpdateStatus {
    }

    /**
     * The projection could not answer. Distinct from up to date: the UI must not show a green
     * state when the real answer is that we do not know.
     */
    record Unknown(Reason reason, String detail) implements UpdateStatus {
    }

    enum Reason {
        TEMPLATE_NOT_IN_CATALOG,
        BASELINE_NOT_IN_CATALOG
    }
}
