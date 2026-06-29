import { Component, input } from '@angular/core';

@Component({
  selector: 'app-ui-badge',
  standalone: true,
  templateUrl: './badge.component.html',
  styleUrls: ['./badge.component.scss']
})
export class UiBadgeComponent {
  color = input<'primary' | 'success' | 'warning' | 'danger'>('primary');
}
