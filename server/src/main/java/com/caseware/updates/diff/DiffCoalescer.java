package com.caseware.updates.diff;

import com.caseware.updates.domain.ChangeOperation;
import com.caseware.updates.domain.TemplateDiff;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Folds a chain of consecutive diffs into the net change set between its endpoints.
 *
 * <p>This exists because concatenating consecutive diffs shows the user values that never
 * existed in their file. REVIEW-CA moves the analytics tolerance 0.15 to 0.12 in v6 to v7 and
 * 0.12 to 0.10 in v7 to v8. Concatenated, a practitioner sitting on v6 sees the threshold change
 * twice and sees 0.12, a value their engagement has never held. The net effect is one change,
 * 0.15 to 0.10, which is exactly what the supplied collapsed v6 to v8 diff reports.
 *
 * <p>The registry's direct from-to diff remains the source of truth for what will be applied.
 * Coalescing is what lets the UI offer a per-release breakdown without double counting, and it
 * is the cross-check that the two representations agree.
 */
public final class DiffCoalescer {

    private DiffCoalescer() {
    }

    public static TemplateDiff coalesce(List<TemplateDiff> chain) {
        if (chain.isEmpty()) {
            throw new IllegalArgumentException("chain must not be empty");
        }
        if (chain.size() == 1) {
            return chain.getFirst();
        }
        requireContiguous(chain);

        Map<String, ChangeOperation> net = new LinkedHashMap<>();
        for (TemplateDiff diff : chain) {
            for (ChangeOperation incoming : diff.changes()) {
                ChangeOperation folded = fold(net.get(incoming.path()), incoming);
                if (folded == null) {
                    net.remove(incoming.path());
                } else {
                    net.put(incoming.path(), folded);
                }
            }
        }

        TemplateDiff first = chain.getFirst();
        TemplateDiff last = chain.getLast();
        return new TemplateDiff(first.templateId(), first.fromVersion(), last.toVersion(),
                last.generatedAt(), List.copyOf(net.values()));
    }

    /**
     * Folds one incoming operation onto whatever is already recorded for the same path.
     * Returns null when the operations cancel out and the path should disappear entirely.
     */
    static ChangeOperation fold(ChangeOperation prior, ChangeOperation incoming) {
        if (prior == null) {
            return incoming;
        }
        return switch (prior.op()) {
            case ADD -> switch (incoming.op()) {
                // A node added then edited is still, net, an addition of its final value.
                case REPLACE -> ChangeOperation.add(prior.path(), incoming.newValue());
                // Added then removed within the window: the user never sees it.
                case REMOVE -> null;
                case ADD -> incoming;
            };
            case REPLACE -> switch (incoming.op()) {
                // Keep the value the engagement actually holds; drop the intermediate.
                case REPLACE -> Objects.equals(prior.oldValue(), incoming.newValue())
                        ? null
                        : ChangeOperation.replace(prior.path(), prior.oldValue(), incoming.newValue());
                case REMOVE -> ChangeOperation.remove(prior.path(), prior.oldValue());
                case ADD -> incoming;
            };
            case REMOVE -> switch (incoming.op()) {
                case ADD -> Objects.equals(prior.oldValue(), incoming.newValue())
                        ? null
                        : ChangeOperation.replace(prior.path(), prior.oldValue(), incoming.newValue());
                default -> incoming;
            };
        };
    }

    private static void requireContiguous(List<TemplateDiff> chain) {
        for (int i = 1; i < chain.size(); i++) {
            TemplateDiff prev = chain.get(i - 1);
            TemplateDiff next = chain.get(i);
            if (!prev.templateId().equals(next.templateId())) {
                throw new IllegalArgumentException(
                        "chain mixes templates: " + prev.templateId() + " and " + next.templateId());
            }
            if (prev.toVersion() != next.fromVersion()) {
                throw new IllegalArgumentException(
                        "chain is not contiguous: v" + prev.toVersion() + " then v" + next.fromVersion());
            }
        }
    }
}
