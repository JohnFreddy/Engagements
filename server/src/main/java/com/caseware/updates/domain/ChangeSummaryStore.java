package com.caseware.updates.domain;

/**
 * Lookup for cached summaries, keyed by version pair rather than by engagement.
 *
 * <p>This key is the reason the design scales: across the sample data, twelve engagements resolve
 * to six distinct pairs, and every firm sitting on the same pair reuses the same entry.
 */
public interface ChangeSummaryStore {

    SummaryState lookup(String templateId, int fromVersion, int toVersion);
}
