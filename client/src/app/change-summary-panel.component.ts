import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { ChangeItem, UpdateAvailableStatus } from './core/template-update.contract';
import { DecisionState } from './core/engagement-update.store';

/**
 * The review surface: what changes, grouped by section, plus Apply and Decline.
 *
 * It renders whatever the server sent and formats nothing about the changes themselves. Every
 * sentence here was written once on the server, cached per version pair, and carries the
 * generator version that produced it. Doing this transformation in the client would mean
 * recomputing it per user, losing the section display names that only the template content has,
 * and making the text a practitioner relied on impossible to reproduce later.
 */
@Component({
  selector: 'app-change-summary-panel',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let status = pendingUpdate();

    <h2>{{ status.name }}</h2>
    <p>
      {{ status.templateDisplayName }} &mdash; v{{ status.fromVersion }} to v{{ status.toVersion }}
      @if (status.versionsBehind > 1) {
        <span> (combines {{ status.versionsBehind }} releases into one decision)</span>
      }
    </p>

    @switch (status.summary.status) {
      @case ('COMPUTING') {
        <p>Preparing the summary of these changes. The update itself is confirmed.</p>
      }
      @case ('FAILED') {
        <p>The summary could not be prepared: {{ failureReason() }}</p>
        <p>You can still apply or decline, or wait and retry.</p>
      }
      @default {
        @if (summaryBody(); as body) {
          @if (isStale()) {
            <p>A newer version has been published since this summary was prepared.</p>
          }
          <p>{{ body.headline }}</p>

          @for (group of groups(); track group.section) {
            <section>
              <h3>{{ group.section }}</h3>
              <ul>
                @for (item of group.items; track item.sourcePath) {
                  <li>
                    <strong>{{ item.title }}</strong>
                    <span> &mdash; {{ item.detail }}</span>
                    @if (item.impact === 'TIGHTENS') {
                      <span> (stricter than before)</span>
                    }
                  </li>
                }
              </ul>
            </section>
          }
        }
      }
    }

    @switch (decision().kind) {
      @case ('SUBMITTING') {
        <p>Submitting your decision...</p>
      }
      @case ('ACCEPTED') {
        <p>Decision recorded. The engagement will update shortly.</p>
      }
      @case ('CONFLICT') {
        <p>
          A newer version was published while you were reviewing. Reload to see the change up to
          v{{ conflictVersion() }} before deciding.
        </p>
      }
      @case ('ERROR') {
        <p>Your decision was not recorded. Please try again.</p>
      }
      @default {
        <button type="button" style="margin-left: 10px;" (click)="apply.emit(status)">Apply update</button>
        <button type="button" style="margin-left: 10px;" (click)="decline.emit(status)">Decline update</button>
      }
    }
  `,
})
export class ChangeSummaryPanelComponent {
  readonly pendingUpdate = input.required<UpdateAvailableStatus>();
  readonly decision = input.required<DecisionState>();

  readonly apply = output<UpdateAvailableStatus>();
  readonly decline = output<UpdateAvailableStatus>();

  readonly isStale = computed(() => this.pendingUpdate().summary.status === 'STALE');

  readonly summaryBody = computed(() => {
    const envelope = this.pendingUpdate().summary;
    return envelope.status === 'READY' || envelope.status === 'STALE' ? envelope.summary : null;
  });

  readonly failureReason = computed(() => {
    const envelope = this.pendingUpdate().summary;
    return envelope.status === 'FAILED' ? envelope.reason : '';
  });

  readonly conflictVersion = computed(() => {
    const decision = this.decision();
    return decision.kind === 'CONFLICT' ? decision.currentToVersion : null;
  });

  /** Grouped by section because that is how a practitioner navigates their file. */
  readonly groups = computed<ReadonlyArray<{ section: string; items: ChangeItem[] }>>(() => {
    const body = this.summaryBody();
    if (body === null) {
      return [];
    }
    const bySection = new Map<string, ChangeItem[]>();
    for (const item of body.items) {
      const existing = bySection.get(item.sectionLabel);
      if (existing) {
        existing.push(item);
      } else {
        bySection.set(item.sectionLabel, [item]);
      }
    }
    return [...bySection].map(([section, items]) => ({ section, items }));
  });
}
