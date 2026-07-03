import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { UiBadgeComponent } from '../../ui/badge/badge.component';
import { AuthService } from '../../services/auth.service';
import { SettingsService } from '../../services/settings.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, UiBadgeComponent, CommonModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent {
  public authService = inject(AuthService);
  public settingsService = inject(SettingsService);
  private router = inject(Router);

  onCurrencyChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.settingsService.setCurrency(select.value);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
