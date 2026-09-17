import { ApplicationConfig, provideZonelessChangeDetection } from '@angular/core';
import { TEMPLATE_UPDATE_GATEWAY, FixtureTemplateUpdateGateway } from './core/template-update.gateway';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),
    // Swap this one provider for an HttpClient-backed gateway and nothing else changes.
    { provide: TEMPLATE_UPDATE_GATEWAY, useClass: FixtureTemplateUpdateGateway },
  ],
};
