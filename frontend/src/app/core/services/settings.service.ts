import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  // Store currency setting, defaulting to RUB. In a real app, this would be persisted to localStorage or DB.
  private currencySignal = signal<string>(localStorage.getItem('app_currency') || 'RUB');
  
  public currency = this.currencySignal.asReadonly();

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
}
