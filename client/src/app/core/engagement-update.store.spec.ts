import { TestBed } from '@angular/core/testing';
import { EngagementUpdateStore } from './engagement-update.store';
import { FixtureTemplateUpdateGateway, TEMPLATE_UPDATE_GATEWAY } from './template-update.gateway';
import { UpdateAvailableStatus } from './template-update.contract';

describe('EngagementUpdateStore', () => {
  let store: EngagementUpdateStore;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [{ provide: TEMPLATE_UPDATE_GATEWAY, useClass: FixtureTemplateUpdateGateway }],
    });
    store = TestBed.inject(EngagementUpdateStore);
    await store.load();
  });

  it('counts only engagements that actually need a decision', () => {
    expect(store.statuses().length).toBe(12);
    expect(store.pendingCount()).toBe(6);

    const behindByTwo = store.pending().filter((s) => s.versionsBehind > 1);
    expect(behindByTwo.map((s) => s.engagementId)).toEqual(['ENG-1003', 'ENG-1007', 'ENG-1011']);
  });

  it('surfaces a version conflict instead of recording the decision', async () => {
    // ENG-1006 is the fixture's stale case: a new version lands mid-review.
    const status = store.pending().find((s) => s.engagementId === 'ENG-1006') as UpdateAvailableStatus;

    await store.decide(status, 'APPLY');

    const decision = store.decisionFor('ENG-1006');
    expect(decision.kind).toBe('CONFLICT');
    if (decision.kind === 'CONFLICT') {
      expect(decision.reviewedToVersion).toBe(8);
      expect(decision.currentToVersion).toBe(9);
    }
    // And a normal engagement still goes through.
    const clean = store.pending().find((s) => s.engagementId === 'ENG-1002') as UpdateAvailableStatus;
    await store.decide(clean, 'APPLY');
    expect(store.decisionFor('ENG-1002').kind).toBe('ACCEPTED');
  });
});
