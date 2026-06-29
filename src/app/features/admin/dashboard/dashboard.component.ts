import { Component } from '@angular/core';
import { UiCardComponent } from '../../../core/ui/card/card.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [UiCardComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class AdminDashboardComponent {
}
