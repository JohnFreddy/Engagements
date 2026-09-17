import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { EngagementUpdateStatus } from './core/template-update.contract';

/**
 * The at-a-glance state for one engagement row. Presentational: takes a status, renders a phrase.
 *
 * It renders UNKNOWN distinctly rather than folding it into "up to date", because telling a
 * practitioner their file is current when we do not know is the worst failure this screen has.
 */
@Component({
  selector: 'app-pending-update-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span [attr.data-state]="status().state">{{ text() }}</span>`,
})
export class PendingUpdateBadgeComponent {
  readonly status = input.required<EngagementUpdateStatus>();

  readonly text = computed(() => {
    const status = this.status();
    switch (status.state) {
      case 'UP_TO_DATE':
        return `Up to date (v${status.currentVersion})`;
      case 'UPDATE_AVAILABLE':
        return status.versionsBehind === 1
          ? `1 update pending (v${status.fromVersion} to v${status.toVersion})`
          : `${status.versionsBehind} updates pending (v${status.fromVersion} to v${status.toVersion})`;
      case 'UNKNOWN':
        return 'Status unavailable';
    }
  });
}
