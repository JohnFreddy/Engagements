package com.caseware.updates;

import com.caseware.updates.diff.DiffCoalescer;
import com.caseware.updates.domain.ChangeOperation;
import com.caseware.updates.domain.TemplateDiff;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiffCoalescerTest {

    /**
     * The golden test. The fixtures supply both the consecutive chain and a collapsed v6 to v8
     * diff; coalescing the chain must reproduce the collapsed one. Comparison is keyed by path
     * because the order of entries in a diff carries no meaning.
     */
    @Test
    @DisplayName("coalescing the REVIEW-CA chain reproduces the supplied collapsed v6 to v8 diff")
    void coalescedChainMatchesCollapsedFixture() {
        TemplateDiff coalesced = DiffCoalescer.coalesce(
                List.of(Fixtures.reviewV6toV7(), Fixtures.reviewV7toV8()));

        assertThat(coalesced.templateId()).isEqualTo("REVIEW-CA");
        assertThat(coalesced.fromVersion()).isEqualTo(6);
        assertThat(coalesced.toVersion()).isEqualTo(8);
        assertThat(byPath(coalesced)).isEqualTo(byPath(Fixtures.reviewV6toV8Collapsed()));
    }

    /**
     * The reason coalescing exists. Concatenating the chain yields six entries and shows the
     * analytics tolerance changing twice, including an intermediate 0.12 that an engagement
     * sitting on v6 has never held.
     */
    @Test
    @DisplayName("an accumulated threshold change is reported once, without the intermediate value")
    void doesNotSurfaceIntermediateThresholdValues() {
        int concatenated = Fixtures.reviewV6toV7().changes().size() + Fixtures.reviewV7toV8().changes().size();

        TemplateDiff coalesced = DiffCoalescer.coalesce(
                List.of(Fixtures.reviewV6toV7(), Fixtures.reviewV7toV8()));

        assertThat(concatenated).isEqualTo(6);
        assertThat(coalesced.changes()).hasSize(5);
        assertThat(coalesced.changes())
                .filteredOn(c -> c.path().endsWith("/tolerance"))
                .singleElement()
                .satisfies(c -> {
                    assertThat(c.oldValue()).isEqualTo(0.15);
                    assertThat(c.newValue()).isEqualTo(0.1);
                });
        assertThat(coalesced.changes()).noneMatch(c -> Double.valueOf(0.12).equals(c.newValue()));
    }

    @Test
    @DisplayName("content added and then removed inside the window disappears entirely")
    void addFollowedByRemoveCancelsOut() {
        String path = "/sections/planning/questions/9";
        Map<String, Object> node = Map.of("id", "Q-PLN-009", "label", "Temporary question");

        TemplateDiff first = new TemplateDiff("AUDIT-CA", 3, 4, Instant.parse("2026-07-07T13:00:00Z"),
                List.of(ChangeOperation.add(path, node)));
        TemplateDiff second = new TemplateDiff("AUDIT-CA", 4, 5, Instant.parse("2026-08-18T13:00:00Z"),
                List.of(ChangeOperation.remove(path, node)));

        assertThat(DiffCoalescer.coalesce(List.of(first, second)).changes()).isEmpty();
    }

    @Test
    @DisplayName("a chain with a gap is rejected rather than silently folded")
    void rejectsNonContiguousChain() {
        assertThatThrownBy(() -> DiffCoalescer.coalesce(
                List.of(Fixtures.reviewV6toV7(), Fixtures.auditV4toV5())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mixes templates");
    }

    private static Map<String, ChangeOperation> byPath(TemplateDiff diff) {
        return diff.changes().stream()
                .collect(Collectors.toMap(ChangeOperation::path, Function.identity()));
    }
}
