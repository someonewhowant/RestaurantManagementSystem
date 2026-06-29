import { Component } from '@angular/core';
import { UiCardComponent } from '../../../core/ui/card/card.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [UiCardComponent],
  template: `
    <div class="dashboard-grid">
      <app-ui-card class="stat-card">
        <h3>Выручка за сегодня</h3>
        <div class="value">$1,240.50</div>
        <div class="trend positive">↑ 12% со вчера</div>
      </app-ui-card>
      
      <app-ui-card class="stat-card">
        <h3>Активных столов</h3>
        <div class="value">8 / 15</div>
        <div class="trend">Средняя загрузка</div>
      </app-ui-card>
      
      <app-ui-card class="stat-card">
        <h3>Дефицит склада</h3>
        <div class="value">3 позиции</div>
        <div class="trend negative">Требуется закупка</div>
      </app-ui-card>
    </div>
  `,
  styles: [`
    .dashboard-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 24px;
    }
    .stat-card h3 {
      font-size: 16px;
      color: var(--color-text-muted);
      margin-bottom: 12px;
      font-weight: 500;
    }
    .stat-card .value {
      font-size: 36px;
      font-weight: 700;
      color: var(--color-text-main);
      font-family: var(--font-secondary);
      margin-bottom: 8px;
    }
    .trend {
      font-size: 14px;
      color: var(--color-text-muted);
    }
    .trend.positive { color: var(--color-success); }
    .trend.negative { color: var(--color-danger); }
  `]
})
export class AdminDashboardComponent {
}
