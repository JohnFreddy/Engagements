package com.caseware.updates;

import com.caseware.updates.summary.ChangeSummary;
import com.caseware.updates.summary.ChangeSummary.ChangeCategory;
import com.caseware.updates.summary.ChangeSummary.Impact;
import com.caseware.updates.summary.ChangeSummaryComposer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeSummaryComposerTest {

    private final ChangeSummaryComposer composer = new ChangeSummaryComposer(
            Clock.fixed(Instant.parse("2026-09-17T09:00:00Z"), ZoneOffset.UTC));

    @Test
    @DisplayName("a lowered threshold reads as tightened, with the section's display name")
    void classifiesThresholdTightening() {
        ChangeSummary summary = composer.compose(Fixtures.auditV4toV5(), Fixtures.auditLabels());

        assertThat(summary.items())
                .filteredOn(item -> item.category() == ChangeCategory.THRESHOLD_CHANGE)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.impact()).isEqualTo(Impact.TIGHTENS);
                    assertThat(item.sectionLabel()).isEqualTo("Materiality");
                    assertThat(item.title()).isEqualTo("Threshold percent");
                    assertThat(item.detail()).isEqualTo("Tightened from 4.5% to 4%");
                    assertThat(item.sourcePath()).isEqualTo("/sections/materiality/guidance/thresholdPercent");
                });
        assertThat(summary.generatorVersion()).isEqualTo(ChangeSummaryComposer.GENERATOR_VERSION);
    }

    @Test
    @DisplayName("a renamed template is demoted to metadata rather than headlined as a change")
    void separatesMetadataFromSubstantiveChange() {
        ChangeSummary summary = composer.compose(Fixtures.reviewV6toV8Collapsed(),
                com.caseware.updates.summary.TemplateLabels.none("REVIEW-CA", 8));

        assertThat(summary.countsByCategory())
                .containsEntry(ChangeCategory.NEW_CONTENT, 2)
                .containsEntry(ChangeCategory.REMOVED_CONTENT, 1)
                .containsEntry(ChangeCategory.THRESHOLD_CHANGE, 1)
                .containsEntry(ChangeCategory.METADATA, 1);
        assertThat(summary.items())
                .filteredOn(item -> item.category() == ChangeCategory.NEW_CONTENT)
                .extracting(ChangeSummary.ChangeItem::title)
                .contains(Fixtures.NEW_QUESTION_LABEL, "Going concern evaluation");
        assertThat(summary.headline()).contains("5 changes");
    }
}
