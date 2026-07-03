import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  // Store currency setting, defaulting to RUB. In a real app, this would be persisted to localStorage or DB.
  private currencySignal = signal<string>(localStorage.getItem('app_currency') || 'RUB');
  
  public currency = this.currencySignal.asReadonly();

  constructor() {
    this.applyTheme();
  }

  public setCurrency(newCurrency: string) {
    this.currencySignal.set(newCurrency);
    localStorage.setItem('app_currency', newCurrency);
  }
  
  public availableCurrencies = [
    { code: 'RUB', symbol: '₽' },
    { code: 'USD', symbol: '$' },
    { code: 'EUR', symbol: '€' },
    { code: 'GBP', symbol: '£' }
  ];

  private themeSignal = signal<'light' | 'dark'>((localStorage.getItem('app_theme') as 'light' | 'dark') || 'light');
  public theme = this.themeSignal.asReadonly();

  public toggleTheme() {
    const newTheme = this.themeSignal() === 'light' ? 'dark' : 'light';
    this.themeSignal.set(newTheme);
    localStorage.setItem('app_theme', newTheme);
    this.applyTheme(newTheme);
  }

  public applyTheme(theme: 'light' | 'dark' = this.themeSignal()) {
    if (theme === 'dark') {
      document.body.classList.add('dark-theme');
    } else {
      document.body.classList.remove('dark-theme');
    }
  }
}
