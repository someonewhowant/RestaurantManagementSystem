import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

export type MenuCategory = 'Популярное' | 'Горячее' | 'Закуски' | 'Салаты' | 'Напитки' | 'Десерты';

export interface Dish {
  id: string;
  name: string;
  category: MenuCategory;
  price: number;
  weight: string;
  imageIcon: string;
  recipe?: { ingredientId: string; amount: number }[];
  instructions?: string;
  allergens?: string[];
  macros?: {
    proteins: number;
    fats: number;
    carbs: number;
    calories: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private http = inject(HttpClient);
  
  private menuSignal = signal<Dish[]>([]);
  public menu = this.menuSignal.asReadonly();
  
  public categories = signal<MenuCategory[]>(['Популярное', 'Горячее', 'Закуски', 'Салаты', 'Напитки', 'Десерты']).asReadonly();

  constructor() {
    this.fetchMenu();
  }

  fetchMenu() {
    this.http.get<Dish[]>('/api/menu').subscribe({
      next: (dishes) => this.menuSignal.set(dishes),
      error: (err) => console.error('Failed to fetch menu', err)
    });
  }

  getDishesByCategory(category: MenuCategory) {
    // Return synchronous computed-like data for UI from the loaded state
    if (category === 'Популярное') {
      return this.menuSignal().slice(0, 4);
    }
    return this.menuSignal().filter(d => d.category === category);
  }

  addDish(dish: Omit<Dish, 'id'>) {
    this.http.post<Dish>('/api/menu', dish).subscribe({
      next: (created) => this.menuSignal.update(menu => [created, ...menu]),
      error: (err) => console.error('Failed to create dish', err)
    });
  }

  updateDish(id: string, updates: Partial<Dish>) {
    this.http.put<Dish>(`/api/menu/${id}`, updates).subscribe({
      next: (updated) => this.menuSignal.update(menu => menu.map(d => d.id === id ? updated : d)),
      error: (err) => console.error('Failed to update dish', err)
    });
  }

  deleteDish(id: string) {
    this.http.delete(`/api/menu/${id}`).subscribe({
      next: () => this.menuSignal.update(menu => menu.filter(d => d.id !== id)),
      error: (err) => console.error('Failed to delete dish', err)
    });
  }
}
