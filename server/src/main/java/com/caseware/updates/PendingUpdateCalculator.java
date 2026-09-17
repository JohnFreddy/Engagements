package com.caseware.updates;

import com.caseware.updates.domain.ChangeSummaryStore;
import com.caseware.updates.domain.Engagement;
import com.caseware.updates.domain.PendingUpdate;
import com.caseware.updates.domain.SummaryState;
import com.caseware.updates.domain.TemplateCatalog;
import com.caseware.updates.domain.TemplateVersion;
import com.caseware.updates.domain.UpdateStatus;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Core read-path logic: given a projection row, decide whether the engagement is behind and by
 * how much. Never loads an engagement file; everything here comes from the projection and the
 * template catalog.
 */
public final class PendingUpdateCalculator {

    private final TemplateCatalog catalog;
    private final ChangeSummaryStore summaries;

    public PendingUpdateCalculator(TemplateCatalog catalog, ChangeSummaryStore summaries) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.summaries = Objects.requireNonNull(summaries, "summaries");
    }

    public UpdateStatus statusFor(Engagement engagement) {
        Optional<TemplateVersion> latest = catalog.latest(engagement.templateId());
        if (latest.isEmpty()) {
            return new UpdateStatus.Unknown(UpdateStatus.Reason.TEMPLATE_NOT_IN_CATALOG,
                    "no versions published for " + engagement.templateId());
        }
        if (!catalog.contains(engagement.templateId(), engagement.baselineVersion())) {
            // The engagement records a version the shared registry does not know about. Reporting
            // "up to date" here would be a silent lie, so it surfaces as unknown instead.
            return new UpdateStatus.Unknown(UpdateStatus.Reason.BASELINE_NOT_IN_CATALOG,
                    engagement.templateId() + " v" + engagement.baselineVersion() + " is not in the catalog");
        }

        List<TemplateVersion> pending = catalog.versionsAfter(engagement.templateId(), engagement.baselineVersion());
        if (pending.isEmpty()) {
            return new UpdateStatus.UpToDate(engagement.templateId(), engagement.baselineVersion());
        }

        int target = pending.getLast().number();
        SummaryState summary = summaries.lookup(engagement.templateId(), engagement.baselineVersion(), target);
        return new UpdateStatus.UpdateAvailable(new PendingUpdate(
                engagement.templateId(), engagement.baselineVersion(), target, pending, summary));
    }

    public List<UpdateStatus> statusFor(List<Engagement> engagements) {
        return engagements.stream().map(this::statusFor).toList();
    }
}
