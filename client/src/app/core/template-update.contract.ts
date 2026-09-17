/**
 * Client/server contract for pending template updates.
 *
 * This file is the single source of truth. The Angular types in
 * client/src/app/core/template-update.contract.ts are a copy of it, and the Java records in
 * com.caseware.updates.domain mirror the same shapes.
 *
 * Two things the contract is deliberate about:
 *   1. Freshness is explicit. Every payload carries when it was computed, and the read model's
 *      own lag is reported separately from the summary's.
 *   2. "Not yet computed" and "we don't know" are first-class states, not absent fields. A client
 *      can never mistake a missing summary for "no changes".
 */

export type Iso8601 = string;

/* ------------------------------------------------------------------ *
 * GET /v1/firms/{firmId}/engagements/update-status?cursor=&limit=
 * ------------------------------------------------------------------ */

export interface EngagementUpdateStatusPage {
  firmId: string;
  /** When this page was read out of the projection. */
  generatedAt: Iso8601;
  /**
   * How far the projection is behind the event stream, in seconds. Surfaced so the UI can warn
   * rather than silently show a stale list after an outage.
   */
  projectionLagSeconds: number;
  items: EngagementUpdateStatus[];
  nextCursor?: string;
}

export type EngagementUpdateStatus = UpToDateStatus | UpdateAvailableStatus | UnknownStatus;

interface StatusBase {
  engagementId: string;
  name: string;
  templateId: string;
  templateDisplayName: string;
  /** When this engagement's row was last recomputed. */
  checkedAt: Iso8601;
}

export interface UpToDateStatus extends StatusBase {
  state: 'UP_TO_DATE';
  currentVersion: number;
}

export interface UpdateAvailableStatus extends StatusBase {
  state: 'UPDATE_AVAILABLE';
  /** The engagement's baseline. One decision covers fromVersion -> toVersion, however wide the gap. */
  fromVersion: number;
  toVersion: number;
  versionsBehind: number;
  latestPublishedAt: Iso8601;
  /** The releases not yet taken, for the optional per-release breakdown. */
  pendingVersions: PublishedVersion[];
  summary: SummaryEnvelope;
}

/**
 * The projection could not answer for this engagement. Distinct from UP_TO_DATE so the UI never
 * shows a reassuring green state when the truth is that we do not know.
 */
export interface UnknownStatus extends StatusBase {
  state: 'UNKNOWN';
  reason: 'TEMPLATE_NOT_IN_CATALOG' | 'BASELINE_NOT_IN_CATALOG' | 'PROJECTION_UNAVAILABLE';
  detail: string;
}

export interface PublishedVersion {
  version: number;
  publishedAt: Iso8601;
}

/* ------------------------------------------------------------------ *
 * Summary freshness
 * ------------------------------------------------------------------ */

export type SummaryEnvelope = SummaryReady | SummaryComputing | SummaryFailed | SummaryStale;

export interface SummaryReady {
  status: 'READY';
  summary: ChangeSummary;
  computedAt: Iso8601;
  /** Ruleset (and model, when narration ran) that produced this text, for reproducibility. */
  generatorVersion: string;
}

export interface SummaryComputing {
  status: 'COMPUTING';
  requestedAt: Iso8601;
}

export interface SummaryFailed {
  status: 'FAILED';
  reason: string;
  failedAt: Iso8601;
  retryAfterSeconds?: number;
}

/** A newer version landed after this was computed. Usable, but no longer the whole picture. */
export interface SummaryStale {
  status: 'STALE';
  summary: ChangeSummary;
  computedAt: Iso8601;
  newerVersionAvailable: number;
}

/* ------------------------------------------------------------------ *
 * The human-readable summary itself
 * ------------------------------------------------------------------ */

export type ChangeCategory =
  | 'NEW_CONTENT'
  | 'REMOVED_CONTENT'
  | 'THRESHOLD_CHANGE'
  | 'WORDING_CHANGE'
  | 'METADATA';

export type ChangeImpact = 'ADDS_WORK' | 'REMOVES_WORK' | 'TIGHTENS' | 'RELAXES' | 'NEUTRAL';

export interface ChangeSummary {
  templateId: string;
  fromVersion: number;
  toVersion: number;
  /** One sentence, already written for a non-technical reader. */
  headline: string;
  countsByCategory: Partial<Record<ChangeCategory, number>>;
  items: ChangeItem[];
}

export interface ChangeItem {
  category: ChangeCategory;
  impact: ChangeImpact;
  sectionLabel: string;
  title: string;
  detail: string;
  /** The raw diff path this sentence came from, so the UI can always drill back to the source. */
  sourcePath: string;
}

/* ------------------------------------------------------------------ *
 * POST /v1/engagements/{engagementId}/template-update/decision
 * ------------------------------------------------------------------ */

export interface TemplateUpdateDecisionRequest {
  decision: 'APPLY' | 'DECLINE';
  /** Echoed from the status the user actually reviewed. The server rejects a stale pair. */
  fromVersion: number;
  toVersion: number;
  idempotencyKey: string;
}

/** 202 Accepted. Applying is asynchronous and slow; the row stays pending until a later poll. */
export interface TemplateUpdateDecisionAccepted {
  decisionId: string;
  engagementId: string;
  decision: 'APPLY' | 'DECLINE';
  fromVersion: number;
  toVersion: number;
  status: 'ACCEPTED';
  acceptedAt: Iso8601;
}

/** 409 Conflict. A new version was published while the user was reading the summary. */
export interface TemplateUpdateDecisionConflict {
  code: 'VERSION_MOVED';
  engagementId: string;
  reviewedToVersion: number;
  currentToVersion: number;
  message: string;
}
