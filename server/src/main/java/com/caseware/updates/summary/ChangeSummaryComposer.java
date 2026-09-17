package com.caseware.updates.summary;

import com.caseware.updates.domain.ChangeOperation;
import com.caseware.updates.domain.TemplateDiff;
import com.caseware.updates.summary.ChangeSummary.ChangeCategory;
import com.caseware.updates.summary.ChangeSummary.ChangeItem;
import com.caseware.updates.summary.ChangeSummary.Impact;

import java.time.Clock;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Turns a raw diff into classified, user-facing change items. Deterministic and reproducible:
 * the same diff and the same template labels always yield the same summary.
 */
public final class ChangeSummaryComposer {

    public static final String GENERATOR_VERSION = "rules-1.0.0";

    private final Clock clock;

    public ChangeSummaryComposer(Clock clock) {
        this.clock = clock;
    }

    public ChangeSummary compose(TemplateDiff diff, TemplateLabels labels) {
        List<ChangeItem> items = diff.changes().stream()
                .map(change -> toItem(change, labels))
                .toList();

        Map<ChangeCategory, Integer> counts = new EnumMap<>(ChangeCategory.class);
        for (ChangeItem item : items) {
            counts.merge(item.category(), 1, Integer::sum);
        }

        long sections = items.stream()
                .map(ChangeItem::sectionLabel)
                .distinct()
                .count();

        return new ChangeSummary(
                diff.templateId(),
                diff.fromVersion(),
                diff.toVersion(),
                headline(items.size(), sections, counts),
                counts,
                items,
                clock.instant(),
                GENERATOR_VERSION);
    }

    private ChangeItem toItem(ChangeOperation change, TemplateLabels labels) {
        ChangeCategory category = classify(change);
        Impact impact = impactOf(change, category);
        String section = change.sectionKey()
                .map(key -> labels.sectionDisplayName(key).orElseGet(() -> humanise(key)))
                .orElse("Template details");
        String title = change.label().orElseGet(() -> humanise(change.leaf()));
        return new ChangeItem(category, impact, section, title, detail(change, category), change.path());
    }

    private static ChangeCategory classify(ChangeOperation change) {
        if (change.path().startsWith("/metadata/")) {
            return ChangeCategory.METADATA;
        }
        return switch (change.op()) {
            case ADD -> ChangeCategory.NEW_CONTENT;
            case REMOVE -> ChangeCategory.REMOVED_CONTENT;
            case REPLACE -> bothNumeric(change) ? ChangeCategory.THRESHOLD_CHANGE : ChangeCategory.WORDING_CHANGE;
        };
    }

    private static Impact impactOf(ChangeOperation change, ChangeCategory category) {
        return switch (category) {
            case NEW_CONTENT -> Impact.ADDS_WORK;
            case REMOVED_CONTENT -> Impact.REMOVES_WORK;
            case THRESHOLD_CHANGE -> {
                double before = ((Number) change.oldValue()).doubleValue();
                double after = ((Number) change.newValue()).doubleValue();
                // Every threshold in the sample data moves down, and down means a lower trigger
                // point, which means more work lands on the practitioner.
                yield after < before ? Impact.TIGHTENS : Impact.RELAXES;
            }
            case WORDING_CHANGE, METADATA -> Impact.NEUTRAL;
        };
    }

    private String detail(ChangeOperation change, ChangeCategory category) {
        return switch (category) {
            case THRESHOLD_CHANGE -> {
                String verb = impactOf(change, category) == Impact.TIGHTENS ? "Tightened" : "Relaxed";
                yield verb + " from " + format(change.oldValue(), change.leaf())
                        + " to " + format(change.newValue(), change.leaf());
            }
            case NEW_CONTENT -> "Newly added in this update.";
            case REMOVED_CONTENT -> "Removed; this no longer appears in the file.";
            case WORDING_CHANGE -> "Reworded to: " + truncate(String.valueOf(change.newValue()));
            case METADATA -> "Renamed to: " + truncate(String.valueOf(change.newValue()));
        };
    }

    private static boolean bothNumeric(ChangeOperation change) {
        return change.oldValue() instanceof Number && change.newValue() instanceof Number;
    }

    private static String format(Object value, String leaf) {
        String rendered = value instanceof Number n
                ? (n.doubleValue() == Math.rint(n.doubleValue()) && leaf.toLowerCase().contains("month")
                        ? String.valueOf(n.intValue())
                        : trimZeros(n.doubleValue()))
                : String.valueOf(value);
        if (leaf.toLowerCase().endsWith("percent")) {
            return rendered + "%";
        }
        if (leaf.toLowerCase().contains("month")) {
            return rendered + " months";
        }
        return rendered;
    }

    private static String trimZeros(double d) {
        String s = String.valueOf(d);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }

    private static String truncate(String text) {
        return text.length() <= 90 ? text : text.substring(0, 87) + "...";
    }

    /** {@code thresholdPercent} to "Threshold percent", {@code going-concern} to "Going concern". */
    static String humanise(String key) {
        String spaced = key.replace('-', ' ').replace('_', ' ').replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
        String lower = spaced.toLowerCase().trim();
        return lower.isEmpty() ? key : Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String headline(int total, long sections, Map<ChangeCategory, Integer> counts) {
        List<String> parts = new ArrayList<>();
        add(parts, counts.get(ChangeCategory.NEW_CONTENT), "addition", "additions");
        add(parts, counts.get(ChangeCategory.REMOVED_CONTENT), "removal", "removals");
        add(parts, counts.get(ChangeCategory.THRESHOLD_CHANGE), "threshold change", "threshold changes");
        add(parts, counts.get(ChangeCategory.WORDING_CHANGE), "wording update", "wording updates");
        add(parts, counts.get(ChangeCategory.METADATA), "naming change", "naming changes");
        String sectionPart = sections == 1 ? "1 section" : sections + " sections";
        String changePart = total == 1 ? "1 change" : total + " changes";
        return changePart + " across " + sectionPart + ": " + String.join(", ", parts) + ".";
    }

    private static void add(List<String> parts, Integer count, String singular, String plural) {
        if (count != null && count > 0) {
            parts.add(count + " " + (count == 1 ? singular : plural));
        }
    }
}
