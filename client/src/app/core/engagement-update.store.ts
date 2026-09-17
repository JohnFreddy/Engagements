import { Injectable, computed, inject, signal } from '@angular/core';
import {
  EngagementUpdateStatus,
  UpdateAvailableStatus,
} from './template-update.contract';
import { TEMPLATE_UPDATE_GATEWAY } from './template-update.gateway';

export type DecisionState =
  | { readonly kind: 'IDLE' }
  | { readonly kind: 'SUBMITTING'; readonly decision: 'APPLY' | 'DECLINE' }
  | { readonly kind: 'ACCEPTED'; readonly decision: 'APPLY' | 'DECLINE'; readonly toVersion: number }
  | { readonly kind: 'CONFLICT'; readonly reviewedToVersion: number; readonly currentToVersion: number }
  | { readonly kind: 'ERROR'; readonly message: string };

const IDLE: DecisionState = { kind: 'IDLE' };

export function isUpdateAvailable(status: EngagementUpdateStatus): status is UpdateAvailableStatus {
  return status.state === 'UPDATE_AVAILABLE';
}

/**
 * Single owner of engagement-update state. Components read signals and raise intents; none of
 * them talk to the gateway or hold their own copy of a status.
 *
 * Decision state is kept beside the statuses rather than folded into them, because a decision is
 * a local, in-flight concern and the status list is a server projection that will be refreshed.
 */
@Injectable({ providedIn: 'root' })
export class EngagementUpdateStore {
  private readonly gateway = inject(TEMPLATE_UPDATE_GATEWAY);

  private readonly _statuses = signal<readonly EngagementUpdateStatus[]>([]);
  private readonly _decisions = signal<Readonly<Record<string, DecisionState>>>({});
  private readonly _selectedId = signal<string | null>(null);
  private readonly _loading = signal(false);
  private readonly _projectionLagSeconds = signal<number | null>(null);
  private readonly _generatedAt = signal<string | null>(null);

  readonly statuses = this._statuses.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly generatedAt = this._generatedAt.asReadonly();
  readonly projectionLagSeconds = this._projectionLagSeconds.asReadonly();

  /** Engagements the user has to make a decision about. */
  readonly pending = computed(() => this._statuses().filter(isUpdateAvailable));
  readonly pendingCount = computed(() => this.pending().length);

  /**
   * True when the projection is far enough behind that the list should be treated with suspicion.
   * The threshold is a product decision; surfacing lag at all is the architectural one.
   */
  readonly projectionIsStale = computed(() => (this._projectionLagSeconds() ?? 0) > 120);

  readonly selected = computed(() => {
    const id = this._selectedId();
    return id === null ? null : (this._statuses().find((s) => s.engagementId === id) ?? null);
  });

  readonly selectedPending = computed(() => {
    const status = this.selected();
    return status !== null && isUpdateAvailable(status) ? status : null;
  });

  readonly selectedDecision = computed(() => this.decisionFor(this._selectedId()));

  decisionFor(engagementId: string | null): DecisionState {
    return engagementId === null ? IDLE : (this._decisions()[engagementId] ?? IDLE);
  }

  select(engagementId: string | null): void {
    this._selectedId.set(engagementId);
  }

  async load(firmId = 'FIRM-001'): Promise<void> {
    this._loading.set(true);
    try {
      const page = await this.gateway.loadStatuses(firmId);
      this._statuses.set(page.items);
      this._generatedAt.set(page.generatedAt);
      this._projectionLagSeconds.set(page.projectionLagSeconds);
    } finally {
      this._loading.set(false);
    }
  }

  /**
   * Applying is asynchronous and slow on the server, so a 202 does not mean the engagement is up
   * to date. The row stays visible in an ACCEPTED state until a later refresh of the projection
   * says otherwise. Nothing here optimistically marks the engagement as updated.
   */
  async decide(status: UpdateAvailableStatus, decision: 'APPLY' | 'DECLINE'): Promise<void> {
    const id = status.engagementId;
    this.setDecision(id, { kind: 'SUBMITTING', decision });

    try {
      const response = await this.gateway.submitDecision(id, {
        decision,
        // Echoed from what the user actually reviewed, so the server can reject a moved target.
        fromVersion: status.fromVersion,
        toVersion: status.toVersion,
        idempotencyKey: `${id}:${status.fromVersion}:${status.toVersion}:${decision}`,
      });

      if ('code' in response) {
        this.setDecision(id, {
          kind: 'CONFLICT',
          reviewedToVersion: response.reviewedToVersion,
          currentToVersion: response.currentToVersion,
        });
        return;
      }

      this.setDecision(id, { kind: 'ACCEPTED', decision, toVersion: response.toVersion });
    } catch (error) {
      this.setDecision(id, { kind: 'ERROR', message: messageOf(error) });
    }
  }

  private setDecision(engagementId: string, state: DecisionState): void {
    this._decisions.update((current) => ({ ...current, [engagementId]: state }));
  }
}

function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : 'Could not submit the decision.';
}
