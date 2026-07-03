import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { SettingsService } from '../../services/settings.service';
import { NotificationService } from '../../services/notification.service';
import { CommonModule } from '@angular/common';
import { NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { signal, computed, DestroyRef, HostListener } from '@angular/core';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent {
  public authService = inject(AuthService);
  public settingsService = inject(SettingsService);
  public notificationService = inject(NotificationService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  public pageTitle = signal<string>('Управление');
  public currentTime = signal<Date>(new Date());
  public isProfileMenuOpen = signal<boolean>(false);
  public isNotificationsOpen = signal<boolean>(false);

  private routeTitleMap: { [key: string]: string } = {
    '/app/dashboard': 'Сводка',
    '/app/inventory': 'Склад',
    '/app/staff': 'Персонал',
    '/app/budget': 'Финансы',
    '/app/pos': 'POS-Терминал',
    '/app/kitchen': 'Экран повара',
    '/app/recipes': 'Карта рецептов'
  };

  constructor() {
    // Setup clock
    const timer = setInterval(() => {
      this.currentTime.set(new Date());
    }, 1000);
    this.destroyRef.onDestroy(() => clearInterval(timer));

    // Setup dynamic title
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.updateTitle(event.urlAfterRedirects);
    });

    // Initial title
    setTimeout(() => this.updateTitle(this.router.url), 0);
  }

  private updateTitle(url: string) {
    const matchedRoute = Object.keys(this.routeTitleMap).find(route => url.includes(route));
    if (matchedRoute) {
      this.pageTitle.set(`Управление — ${this.routeTitleMap[matchedRoute]}`);
    } else {
      this.pageTitle.set('Управление');
    }
  }

  toggleProfileMenu() {
    this.isProfileMenuOpen.update(val => !val);
    if (this.isProfileMenuOpen()) {
      this.isNotificationsOpen.set(false);
    }
  }

  toggleNotificationsMenu() {
    this.isNotificationsOpen.update(val => !val);
    if (this.isNotificationsOpen()) {
      this.isProfileMenuOpen.set(false);
    }
  }

  onCurrencyChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.settingsService.setCurrency(select.value);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
