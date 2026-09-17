import { InjectionToken } from '@angular/core';
import {
  EngagementUpdateStatusPage,
  TemplateUpdateDecisionAccepted,
  TemplateUpdateDecisionConflict,
  TemplateUpdateDecisionRequest,
} from './template-update.contract';
import { CONFLICTING_ENGAGEMENTS, ENGAGEMENT_UPDATE_PAGE } from './engagement-updates.fixture';

/**
 * The only seam between the store and the network. Swapping the fixture for an HttpClient
 * implementation is one class and no change to the store or the components.
 */
export interface TemplateUpdateGateway {
  loadStatuses(firmId: string): Promise<EngagementUpdateStatusPage>;
  submitDecision(
    engagementId: string,
    request: TemplateUpdateDecisionRequest,
  ): Promise<TemplateUpdateDecisionAccepted | TemplateUpdateDecisionConflict>;
}

export const TEMPLATE_UPDATE_GATEWAY = new InjectionToken<TemplateUpdateGateway>('TemplateUpdateGateway');

/** In-memory gateway backed by the sample data. No HTTP, per the exercise scope. */
export class FixtureTemplateUpdateGateway implements TemplateUpdateGateway {
  async loadStatuses(): Promise<EngagementUpdateStatusPage> {
    return structuredClone(ENGAGEMENT_UPDATE_PAGE);
  }

  async submitDecision(
    engagementId: string,
    request: TemplateUpdateDecisionRequest,
  ): Promise<TemplateUpdateDecisionAccepted | TemplateUpdateDecisionConflict> {
    if (CONFLICTING_ENGAGEMENTS.has(engagementId)) {
      return {
        code: 'VERSION_MOVED',
        engagementId,
        reviewedToVersion: request.toVersion,
        currentToVersion: request.toVersion + 1,
        message: 'A newer template version was published while you were reviewing this update.',
      };
    }
    return {
      decisionId: `dec-${engagementId}-${request.toVersion}`,
      engagementId,
      decision: request.decision,
      fromVersion: request.fromVersion,
      toVersion: request.toVersion,
      status: 'ACCEPTED',
      acceptedAt: new Date().toISOString(),
    };
  }
}
