import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EngagementListComponent } from './engagement-list.component';

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EngagementListComponent],
  template: `<app-engagement-list />`,
})
export class AppComponent {}
