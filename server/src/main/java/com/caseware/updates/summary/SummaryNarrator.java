package com.caseware.updates.summary;

/**
 * Optional prose layer over an already-classified summary.
 *
 * <p>The LLM implementation lives behind this interface, and the contract is deliberately narrow:
 * it may rephrase the items it is handed and nothing else. It does not decide what changed, does
 * not reclassify, and does not add or drop items. Output is validated against
 * {@link ChangeSummary#items()} before it is cached, and the model id is recorded alongside
 * {@code generatorVersion}.
 *
 * <p>In this domain a fluent sentence that misstates a change is worse than no sentence at all,
 * so the deterministic classifier in {@link ChangeSummaryComposer} is the part that is allowed
 * to be authoritative.
 */
public interface SummaryNarrator {

    String narrate(ChangeSummary summary);

    /** Default: the deterministic headline, used when narration is unavailable or rejected. */
    SummaryNarrator DETERMINISTIC = ChangeSummary::headline;
}
