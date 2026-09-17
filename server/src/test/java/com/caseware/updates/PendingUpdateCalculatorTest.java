package com.caseware.updates;

import com.caseware.updates.domain.ChangeSummaryStore;
import com.caseware.updates.domain.SummaryState;
import com.caseware.updates.domain.TemplateVersion;
import com.caseware.updates.domain.UpdateStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PendingUpdateCalculatorTest {

    private final RecordingSummaryStore summaries = new RecordingSummaryStore();
    private final PendingUpdateCalculator calculator =
            new PendingUpdateCalculator(Fixtures.catalog(), summaries);

    @Test
    @DisplayName("an engagement two releases behind yields one pending update spanning both")
    void accumulatedVersionsCollapseIntoOnePendingUpdate() {
        UpdateStatus status = calculator.statusFor(Fixtures.ENG_1007);

        assertThat(status).isInstanceOf(UpdateStatus.UpdateAvailable.class);
        var pending = ((UpdateStatus.UpdateAvailable) status).pendingUpdate();

        assertThat(pending.fromVersion()).isEqualTo(6);
        assertThat(pending.toVersion()).isEqualTo(8);
        assertThat(pending.versionsBehind()).isEqualTo(2);
        assertThat(pending.hasAccumulated()).isTrue();
        assertThat(pending.pendingVersions()).extracting(TemplateVersion::number).containsExactly(7, 8);
        // One decision for the whole gap, keyed on one version pair.
        assertThat(summaries.keys).containsExactly("REVIEW-CA:6:8");
    }

    @Test
    @DisplayName("an engagement on the latest version is up to date and costs no summary lookup")
    void upToDateEngagementDoesNotRequestASummary() {
        UpdateStatus status = calculator.statusFor(Fixtures.ENG_1001);

        assertThat(status).isEqualTo(new UpdateStatus.UpToDate("AUDIT-CA", 5));
        assertThat(summaries.keys).isEmpty();
    }

    @Test
    @DisplayName("a summary that has not been computed yet is a state, not a missing field")
    void pendingSummaryIsRepresentedExplicitly() {
        summaries.answer = new SummaryState.Computing(Instant.parse("2026-08-11T13:10:00Z"));

        UpdateStatus status = calculator.statusFor(Fixtures.ENG_1010);

        var pending = ((UpdateStatus.UpdateAvailable) status).pendingUpdate();
        assertThat(pending.versionsBehind()).isEqualTo(1);
        assertThat(pending.summary()).isInstanceOf(SummaryState.Computing.class);
    }

    /** Records lookups so the tests can assert on cache-key shape and on lookups not happening. */
    private static final class RecordingSummaryStore implements ChangeSummaryStore {
        private final List<String> keys = new ArrayList<>();
        private SummaryState answer = new SummaryState.Computing(Instant.parse("2026-09-01T00:00:00Z"));

        @Override
        public SummaryState lookup(String templateId, int fromVersion, int toVersion) {
            keys.add(templateId + ":" + fromVersion + ":" + toVersion);
            return answer;
        }
    }
}
