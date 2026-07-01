import { Component, input } from '@angular/core';

export type ButtonVariant = 'primary' | 'danger' | 'success' | 'warning' | 'secondary';
export type ButtonSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-ui-button',
  standalone: true,
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss']
})
export class UiButtonComponent {
  variant = input<'primary' | 'danger' | 'success' | 'warning' | 'secondary'>('primary');
  size = input<'sm' | 'md'>('md');
  disabled = input<boolean>(false);
  type = input<'button' | 'submit' | 'reset'>('button');
}
