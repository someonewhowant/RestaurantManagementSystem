import { Injectable, signal } from '@angular/core';

export type MenuCategory = 'Популярное' | 'Горячее' | 'Закуски' | 'Напитки' | 'Десерты';

export interface Dish {
  id: string;
  name: string;
  category: MenuCategory;
  price: number;
  weight: string;
  imageIcon: string;
}

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private menuSignal = signal<Dish[]>([
    { id: 'm1', name: 'Стейк Рибай', category: 'Горячее', price: 2500, weight: '350г', imageIcon: '🥩' },
    { id: 'm2', name: 'Паста Карбонара', category: 'Горячее', price: 650, weight: '300г', imageIcon: '🍝' },
    { id: 'm3', name: 'Бургер классический', category: 'Горячее', price: 550, weight: '400г', imageIcon: '🍔' },
    { id: 'm4', name: 'Цезарь с курицей', category: 'Закуски', price: 480, weight: '250г', imageIcon: '🥗' },
    { id: 'm5', name: 'Сырная тарелка', category: 'Закуски', price: 850, weight: '200г', imageIcon: '🧀' },
    { id: 'm6', name: 'Лимонад', category: 'Напитки', price: 250, weight: '400мл', imageIcon: '🍹' },
    { id: 'm7', name: 'Капучино', category: 'Напитки', price: 220, weight: '250мл', imageIcon: '☕' },
    { id: 'm8', name: 'Чизкейк', category: 'Десерты', price: 380, weight: '150г', imageIcon: '🍰' },
    { id: 'm9', name: 'Тирамису', category: 'Десерты', price: 420, weight: '180г', imageIcon: '🍮' },
  ]);

  public menu = this.menuSignal.asReadonly();
  public categories = signal<MenuCategory[]>(['Популярное', 'Горячее', 'Закуски', 'Напитки', 'Десерты']).asReadonly();

  getDishesByCategory(category: MenuCategory) {
    if (category === 'Популярное') {
      return this.menuSignal().slice(0, 4);
    }
    return this.menuSignal().filter(d => d.category === category);
  }
}
