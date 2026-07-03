import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { UiBadgeComponent } from '../../ui/badge/badge.component';
import { AuthService } from '../../services/auth.service';
import { SettingsService } from '../../services/settings.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, UiBadgeComponent],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.scss'
})
export class AdminLayoutComponent {
  public authService = inject(AuthService);
  public settingsService = inject(SettingsService);

  onCurrencyChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.settingsService.setCurrency(select.value);
  }
}
