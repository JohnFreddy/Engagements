import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { EngagementUpdateStore, isUpdateAvailable } from './core/engagement-update.store';
import { PendingUpdateBadgeComponent } from './pending-update-badge.component';
import { ChangeSummaryPanelComponent } from './change-summary-panel.component';
import { UpdateAvailableStatus } from './core/template-update.contract';

/**
 * The list screen. Owns selection and delegates everything else: the badge renders a row's state,
 * the panel renders the review surface, the store owns the data.
 *
 * The list is intentionally flat and unfiltered; filtering, sorting and bulk actions are out of
 * scope for this exercise.
 */
@Component({
  selector: 'app-engagement-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PendingUpdateBadgeComponent, ChangeSummaryPanelComponent],
  template: `
    <h1>Engagements</h1>

    @if (store.loading()) {
      <p>Loading engagements...</p>
    } @else {
      <p>{{ store.pendingCount() }} of {{ store.statuses().length }} engagements have pending updates.</p>

      @if (store.projectionIsStale()) {
        <p>
          This list is {{ store.projectionLagSeconds() }} seconds behind and may not reflect the
          most recent template publications.
        </p>
      }

      <ul>
        @for (status of store.statuses(); track status.engagementId) {
          <li>
            <span>{{ status.name }}</span>
            <app-pending-update-badge [status]="status" />
            @if (isPending(status)) {
              <button type="button" style="margin-left: 10px;" (click)="store.select(status.engagementId)">Review</button>
            }
          </li>
        } @empty {
          <li>No engagements.</li>
        }
      </ul>

      @if (store.selectedPending(); as selected) {
        <app-change-summary-panel
          [pendingUpdate]="selected"
          [decision]="store.selectedDecision()"
          (apply)="onApply($event)"
          (decline)="onDecline($event)" />
        <button type="button" style="margin-left: 10px;" (click)="store.select(null)">Close</button>
      }
    }
  `,
})
export class EngagementListComponent implements OnInit {
  protected readonly store = inject(EngagementUpdateStore);
  protected readonly isPending = isUpdateAvailable;

  ngOnInit(): void {
    void this.store.load();
  }

  protected onApply(status: UpdateAvailableStatus): void {
    void this.store.decide(status, 'APPLY');
  }

  protected onDecline(status: UpdateAvailableStatus): void {
    void this.store.decide(status, 'DECLINE');
  }
}
