/**
 * Fixture standing in for GET /v1/firms/{firmId}/engagements/update-status.
 *
 * Derived from data/engagements.json and data/templates.json. All twelve engagements are present,
 * and the six that are behind cover every summary state in the contract so the UI is exercised
 * end to end without a backend: READY, COMPUTING, FAILED and STALE.
 *
 * The summary text is what the server's ChangeSummaryComposer produces for these diffs.
 */
import { ChangeItem, EngagementUpdateStatus, EngagementUpdateStatusPage } from './template-update.contract';

const upToDate = (
  engagementId: string,
  name: string,
  templateId: string,
  templateDisplayName: string,
  currentVersion: number,
): EngagementUpdateStatus => ({
  state: 'UP_TO_DATE',
  engagementId,
  name,
  templateId,
  templateDisplayName,
  currentVersion,
  checkedAt: '2026-09-17T08:55:00Z',
});

const NEW_QUESTION: ChangeItem = {
  category: 'NEW_CONTENT',
  impact: 'ADDS_WORK',
  sectionLabel: 'Inquiries',
  title: 'Describe any events after the reporting date that may require adjustment or disclosure.',
  detail: 'Newly added in this update.',
  sourcePath: '/sections/inquiries/questions/12',
};

const GOING_CONCERN: ChangeItem = {
  category: 'NEW_CONTENT',
  impact: 'ADDS_WORK',
  sectionLabel: 'Completion',
  title: 'Going concern evaluation',
  detail: 'Newly added in this update.',
  sourcePath: '/sections/completion/checklists/going-concern',
};

const REMOVED_HELP: ChangeItem = {
  category: 'REMOVED_CONTENT',
  impact: 'REMOVES_WORK',
  sectionLabel: 'Inquiries',
  title: 'Help text',
  detail: 'Removed; this no longer appears in the file.',
  sourcePath: '/sections/inquiries/questions/4/helpText',
};

const RENAME: ChangeItem = {
  category: 'METADATA',
  impact: 'NEUTRAL',
  sectionLabel: 'Template details',
  title: 'Display name',
  detail: 'Renamed to: Canadian Review Engagement 2026',
  sourcePath: '/metadata/displayName',
};

export const ENGAGEMENT_UPDATE_PAGE: EngagementUpdateStatusPage = {
  firmId: 'FIRM-001',
  generatedAt: '2026-09-17T09:00:00Z',
  projectionLagSeconds: 4,
  items: [
    upToDate('ENG-1001', 'Northstar Manufacturing 2026', 'AUDIT-CA', 'Canadian Audit Engagement', 5),
    {
      state: 'UPDATE_AVAILABLE',
      engagementId: 'ENG-1002',
      name: 'Maple Ridge Foods 2026',
      templateId: 'AUDIT-CA',
      templateDisplayName: 'Canadian Audit Engagement',
      checkedAt: '2026-09-17T08:55:00Z',
      fromVersion: 4,
      toVersion: 5,
      versionsBehind: 1,
      latestPublishedAt: '2026-08-18T13:00:00Z',
      pendingVersions: [{ version: 5, publishedAt: '2026-08-18T13:00:00Z' }],
      summary: {
        status: 'READY',
        computedAt: '2026-08-18T13:06:00Z',
        generatorVersion: 'rules-1.0.0',
        summary: {
          templateId: 'AUDIT-CA',
          fromVersion: 4,
          toVersion: 5,
          headline: '3 changes across 3 sections: 1 addition, 1 threshold change, 1 wording update.',
          countsByCategory: { NEW_CONTENT: 1, THRESHOLD_CHANGE: 1, WORDING_CHANGE: 1 },
          items: [
            {
              category: 'WORDING_CHANGE',
              impact: 'NEUTRAL',
              sectionLabel: 'Planning',
              title: 'Label',
              detail:
                'Reworded to: Has management identified significant accounting estimates and related estimation...',
              sourcePath: '/sections/planning/questions/3/label',
            },
            {
              category: 'THRESHOLD_CHANGE',
              impact: 'TIGHTENS',
              sectionLabel: 'Materiality',
              title: 'Threshold percent',
              detail: 'Tightened from 4.5% to 4%',
              sourcePath: '/sections/materiality/guidance/thresholdPercent',
            },
            {
              category: 'NEW_CONTENT',
              impact: 'ADDS_WORK',
              sectionLabel: 'Completion',
              title: 'Subsequent events review',
              detail: 'Newly added in this update.',
              sourcePath: '/sections/completion/checklists/subsequent-events',
            },
          ],
        },
      },
    },
    {
      state: 'UPDATE_AVAILABLE',
      engagementId: 'ENG-1003',
      name: 'Harbourview Logistics 2026',
      templateId: 'AUDIT-CA',
      templateDisplayName: 'Canadian Audit Engagement',
      checkedAt: '2026-09-17T08:55:00Z',
      fromVersion: 3,
      toVersion: 5,
      versionsBehind: 2,
      latestPublishedAt: '2026-08-18T13:00:00Z',
      pendingVersions: [
        { version: 4, publishedAt: '2026-07-07T13:00:00Z' },
        { version: 5, publishedAt: '2026-08-18T13:00:00Z' },
      ],
      summary: {
        status: 'READY',
        computedAt: '2026-08-18T13:07:00Z',
        generatorVersion: 'rules-1.0.0',
        summary: {
          templateId: 'AUDIT-CA',
          fromVersion: 3,
          toVersion: 5,
          // Materiality moved 5.0 -> 4.5 -> 4.0 across two releases. Reported once, net.
          headline:
            '4 changes across 3 sections: 2 additions, 1 removal, 1 threshold change, 1 wording update.',
          countsByCategory: { NEW_CONTENT: 2, REMOVED_CONTENT: 1, THRESHOLD_CHANGE: 1, WORDING_CHANGE: 1 },
          items: [
            {
              category: 'THRESHOLD_CHANGE',
              impact: 'TIGHTENS',
              sectionLabel: 'Materiality',
              title: 'Threshold percent',
              detail: 'Tightened from 5% to 4%',
              sourcePath: '/sections/materiality/guidance/thresholdPercent',
            },
            {
              category: 'NEW_CONTENT',
              impact: 'ADDS_WORK',
              sectionLabel: 'Planning',
              title: 'Were any new fraud risk factors identified during planning?',
              detail: 'Newly added in this update.',
              sourcePath: '/sections/planning/questions/7',
            },
            {
              category: 'NEW_CONTENT',
              impact: 'ADDS_WORK',
              sectionLabel: 'Completion',
              title: 'Subsequent events review',
              detail: 'Newly added in this update.',
              sourcePath: '/sections/completion/checklists/subsequent-events',
            },
            {
              category: 'REMOVED_CONTENT',
              impact: 'REMOVES_WORK',
              sectionLabel: 'Planning',
              title: 'Confirm legacy risk classification',
              detail: 'Removed; this no longer appears in the file.',
              sourcePath: '/sections/planning/procedures/legacy-risk-confirmation',
            },
          ],
        },
      },
    },
    upToDate('ENG-1004', 'Pinecrest Holdings 2026', 'AUDIT-CA', 'Canadian Audit Engagement', 5),
    upToDate('ENG-1005', 'Cedar Peak Services 2026', 'REVIEW-CA', 'Canadian Review Engagement', 8),
    {
      state: 'UPDATE_AVAILABLE',
      engagementId: 'ENG-1006',
      name: 'Westmount Consulting 2026',
      templateId: 'REVIEW-CA',
      templateDisplayName: 'Canadian Review Engagement',
      checkedAt: '2026-09-17T08:55:00Z',
      fromVersion: 7,
      toVersion: 8,
      versionsBehind: 1,
      latestPublishedAt: '2026-08-25T13:00:00Z',
      pendingVersions: [{ version: 8, publishedAt: '2026-08-25T13:00:00Z' }],
      summary: {
        status: 'READY',
        computedAt: '2026-08-25T13:06:00Z',
        generatorVersion: 'rules-1.0.0',
        summary: {
          templateId: 'REVIEW-CA',
          fromVersion: 7,
          toVersion: 8,
          headline: '3 changes across 3 sections: 1 addition, 1 threshold change, 1 naming change.',
          countsByCategory: { NEW_CONTENT: 1, THRESHOLD_CHANGE: 1, METADATA: 1 },
          items: [
            RENAME,
            {
              category: 'THRESHOLD_CHANGE',
              impact: 'TIGHTENS',
              sectionLabel: 'Analytics',
              title: 'Tolerance',
              detail: 'Tightened from 0.12 to 0.1',
              sourcePath: '/sections/analytics/procedures/2/tolerance',
            },
            GOING_CONCERN,
          ],
        },
      },
    },
    {
      state: 'UPDATE_AVAILABLE',
      engagementId: 'ENG-1007',
      name: 'Bluewater Hospitality 2026',
      templateId: 'REVIEW-CA',
      templateDisplayName: 'Canadian Review Engagement',
      checkedAt: '2026-09-17T08:55:00Z',
      fromVersion: 6,
      toVersion: 8,
      versionsBehind: 2,
      latestPublishedAt: '2026-08-25T13:00:00Z',
      pendingVersions: [
        { version: 7, publishedAt: '2026-07-21T13:00:00Z' },
        { version: 8, publishedAt: '2026-08-25T13:00:00Z' },
      ],
      summary: {
        status: 'READY',
        computedAt: '2026-08-25T13:08:00Z',
        generatorVersion: 'rules-1.0.0',
        summary: {
          templateId: 'REVIEW-CA',
          fromVersion: 6,
          toVersion: 8,
          // The coalesced case. Tolerance went 0.15 -> 0.12 -> 0.10; the user sees one change,
          // 0.15 -> 0.1, never the 0.12 their file has never held.
          headline:
            '5 changes across 4 sections: 2 additions, 1 removal, 1 threshold change, 1 naming change.',
          countsByCategory: { NEW_CONTENT: 2, REMOVED_CONTENT: 1, THRESHOLD_CHANGE: 1, METADATA: 1 },
          items: [
            NEW_QUESTION,
            {
              category: 'THRESHOLD_CHANGE',
              impact: 'TIGHTENS',
              sectionLabel: 'Analytics',
              title: 'Tolerance',
              detail: 'Tightened from 0.15 to 0.1',
              sourcePath: '/sections/analytics/procedures/2/tolerance',
            },
            REMOVED_HELP,
            RENAME,
            GOING_CONCERN,
          ],
        },
      },
    },
    upToDate('ENG-1008', 'Summit Property Group 2026', 'REVIEW-CA', 'Canadian Review Engagement', 8),
    upToDate('ENG-1009', 'Northern Grid Energy 2026', 'RISK-CA', 'Canadian Risk Assessment', 12),
    {
      // Summary still being generated. The UI must show a distinct state, not an empty panel.
      state: 'UPDATE_AVAILABLE',
      engagementId: 'ENG-1010',
      name: 'Greenfield Health Services 2026',
      templateId: 'RISK-CA',
      templateDisplayName: 'Canadian Risk Assessment',
      checkedAt: '2026-09-17T08:55:00Z',
      fromVersion: 11,
      toVersion: 12,
      versionsBehind: 1,
      latestPublishedAt: '2026-08-11T13:00:00Z',
      pendingVersions: [{ version: 12, publishedAt: '2026-08-11T13:00:00Z' }],
      summary: { status: 'COMPUTING', requestedAt: '2026-09-17T08:59:40Z' },
    },
    {
      // Summary generation failed. The pending update is still true and must still be actionable.
      state: 'UPDATE_AVAILABLE',
      engagementId: 'ENG-1011',
      name: 'Stonebridge Construction 2026',
      templateId: 'RISK-CA',
      templateDisplayName: 'Canadian Risk Assessment',
      checkedAt: '2026-09-17T08:55:00Z',
      fromVersion: 10,
      toVersion: 12,
      versionsBehind: 2,
      latestPublishedAt: '2026-08-11T13:00:00Z',
      pendingVersions: [
        { version: 11, publishedAt: '2026-06-30T13:00:00Z' },
        { version: 12, publishedAt: '2026-08-11T13:00:00Z' },
      ],
      summary: {
        status: 'FAILED',
        reason: 'Template v10 could not be retrieved from the registry.',
        failedAt: '2026-09-17T08:58:00Z',
        retryAfterSeconds: 300,
      },
    },
    upToDate('ENG-1012', 'Prairie Star Investments 2026', 'RISK-CA', 'Canadian Risk Assessment', 12),
  ],
};

/** Engagements the fixture gateway answers with 409 VERSION_MOVED, to exercise the stale path. */
export const CONFLICTING_ENGAGEMENTS = new Set(['ENG-1006']);
