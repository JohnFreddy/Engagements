package com.caseware.updates;

import com.caseware.updates.domain.ChangeOperation;
import com.caseware.updates.domain.Engagement;
import com.caseware.updates.domain.InMemoryTemplateCatalog;
import com.caseware.updates.domain.TemplateCatalog;
import com.caseware.updates.domain.TemplateDiff;
import com.caseware.updates.domain.TemplateVersion;
import com.caseware.updates.summary.TemplateLabels;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Transcription of the supplied sample data in data/. Kept as Java literals rather than parsed
 * JSON so the domain module carries no parser dependency; the values match the fixture files
 * byte for byte in meaning.
 */
final class Fixtures {

    private Fixtures() {
    }

    // data/templates.json. Note the version numbers do not start at 1 and are not ordinals.
    static TemplateCatalog catalog() {
        return new InMemoryTemplateCatalog(List.of(
                new TemplateVersion("AUDIT-CA", 3, Instant.parse("2026-05-12T13:00:00Z")),
                new TemplateVersion("AUDIT-CA", 4, Instant.parse("2026-07-07T13:00:00Z")),
                new TemplateVersion("AUDIT-CA", 5, Instant.parse("2026-08-18T13:00:00Z")),
                new TemplateVersion("REVIEW-CA", 6, Instant.parse("2026-05-20T13:00:00Z")),
                new TemplateVersion("REVIEW-CA", 7, Instant.parse("2026-07-21T13:00:00Z")),
                new TemplateVersion("REVIEW-CA", 8, Instant.parse("2026-08-25T13:00:00Z")),
                new TemplateVersion("RISK-CA", 10, Instant.parse("2026-04-28T13:00:00Z")),
                new TemplateVersion("RISK-CA", 11, Instant.parse("2026-06-30T13:00:00Z")),
                new TemplateVersion("RISK-CA", 12, Instant.parse("2026-08-11T13:00:00Z"))));
    }

    // data/engagements.json
    static final Engagement ENG_1001 = new Engagement("ENG-1001", "Northstar Manufacturing 2026", "AUDIT-CA", 5);
    static final Engagement ENG_1003 = new Engagement("ENG-1003", "Harbourview Logistics 2026", "AUDIT-CA", 3);
    static final Engagement ENG_1007 = new Engagement("ENG-1007", "Bluewater Hospitality 2026", "REVIEW-CA", 6);
    static final Engagement ENG_1010 = new Engagement("ENG-1010", "Greenfield Health Services 2026", "RISK-CA", 11);

    static final String NEW_QUESTION_LABEL =
            "Describe any events after the reporting date that may require adjustment or disclosure.";
    static final String REMOVED_HELP_TEXT =
            "Ask management to describe changes in accounting policies since the prior year.";
    static final String RENAMED_DISPLAY_NAME = "Canadian Review Engagement 2026";

    private static final Map<String, Object> Q_INQ_012 =
            Map.of("id", "Q-INQ-012", "type", "text", "label", NEW_QUESTION_LABEL);

    private static final Map<String, Object> CHK_GC_01 = Map.of(
            "id", "CHK-GC-01",
            "label", "Going concern evaluation",
            "items", List.of("Document management's assessment",
                    "Evaluate contradictory evidence",
                    "Record the practitioner's conclusion"));

    // data/template-diff-review-ca-v6-v7.json
    static TemplateDiff reviewV6toV7() {
        return new TemplateDiff("REVIEW-CA", 6, 7, Instant.parse("2026-07-21T13:03:02Z"), List.of(
                ChangeOperation.add("/sections/inquiries/questions/12", Q_INQ_012),
                ChangeOperation.replace("/sections/analytics/procedures/2/tolerance", 0.15, 0.12),
                ChangeOperation.remove("/sections/inquiries/questions/4/helpText", REMOVED_HELP_TEXT)));
    }

    // data/template-diff-review-ca-v7-v8.json
    static TemplateDiff reviewV7toV8() {
        return new TemplateDiff("REVIEW-CA", 7, 8, Instant.parse("2026-08-25T13:02:27Z"), List.of(
                ChangeOperation.replace("/metadata/displayName",
                        "Canadian Review Engagement", RENAMED_DISPLAY_NAME),
                ChangeOperation.replace("/sections/analytics/procedures/2/tolerance", 0.12, 0.1),
                ChangeOperation.add("/sections/completion/checklists/going-concern", CHK_GC_01)));
    }

    // data/template-diff-review-ca-v6-v8.json, the collapsed diff supplied alongside the chain
    static TemplateDiff reviewV6toV8Collapsed() {
        return new TemplateDiff("REVIEW-CA", 6, 8, Instant.parse("2026-08-25T13:04:55Z"), List.of(
                ChangeOperation.replace("/metadata/displayName",
                        "Canadian Review Engagement", RENAMED_DISPLAY_NAME),
                ChangeOperation.add("/sections/inquiries/questions/12", Q_INQ_012),
                ChangeOperation.replace("/sections/analytics/procedures/2/tolerance", 0.15, 0.1),
                ChangeOperation.remove("/sections/inquiries/questions/4/helpText", REMOVED_HELP_TEXT),
                ChangeOperation.add("/sections/completion/checklists/going-concern", CHK_GC_01)));
    }

    // data/template-diff-audit-ca-v4-v5.json, used for the threshold classification test
    static TemplateDiff auditV4toV5() {
        return new TemplateDiff("AUDIT-CA", 4, 5, Instant.parse("2026-08-18T13:04:41Z"), List.of(
                ChangeOperation.replace("/sections/planning/questions/3/label",
                        "Has management identified significant estimates?",
                        "Has management identified significant accounting estimates and related estimation uncertainty?"),
                ChangeOperation.replace("/sections/materiality/guidance/thresholdPercent", 4.5, 4.0),
                ChangeOperation.add("/sections/completion/checklists/subsequent-events",
                        Map.of("id", "CHK-SE-01", "label", "Subsequent events review"))));
    }

    // Section display names from data/template-fragment-audit-ca-v5.json
    static TemplateLabels auditLabels() {
        return new TemplateLabels("AUDIT-CA", 5, Map.of(
                "planning", "Planning",
                "materiality", "Materiality",
                "completion", "Completion"));
    }
}
