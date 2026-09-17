package com.caseware.updates.domain;

import com.caseware.updates.summary.ChangeSummary;

import java.time.Instant;

/**
 * Freshness of the human-readable summary for a pending update, modelled in the type system
 * rather than as a nullable field. Maps one-to-one onto {@code SummaryEnvelope.status} in the
 * client contract, so "not yet computed" is a state the UI must handle, not a null it may ignore.
 */
public sealed interface SummaryState {

    record Ready(ChangeSummary summary, Instant computedAt) implements SummaryState {
    }

    record Computing(Instant requestedAt) implements SummaryState {
    }

    record Failed(String reason, Instant failedAt) implements SummaryState {
    }

    /** A newer version landed after this summary was computed; it is usable but no longer complete. */
    record Stale(ChangeSummary summary, Instant computedAt, int newerVersionAvailable) implements SummaryState {
    }
}
