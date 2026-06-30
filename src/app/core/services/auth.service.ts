import { Injectable, signal, computed } from '@angular/core';

export interface User {
  id: string;
  name: string;
  firstName?: string;
  lastName?: string;
  restaurantName?: string;
  email: string;
  role: 'owner' | 'waiter' | 'manager';
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  public currentUser = signal<User | null>(null);
  
  public isAuthenticated = computed(() => this.currentUser() !== null);

  login(email: string) {
    // Имитация успешного входа (в реальном приложении здесь будет HTTP-запрос)
    this.currentUser.set({
      id: Math.random().toString(36).substr(2, 9),
      name: email === 'waiter@restaurant.com' ? 'Иван Официант' : 'Владелец Ресторана',
      email,
      role: email === 'waiter@restaurant.com' ? 'waiter' : 'owner'
    });
  }

  register(email: string, firstName: string, lastName: string, restaurantName: string) {
    this.currentUser.set({
      id: Math.random().toString(36).substr(2, 9),
      name: `${firstName} ${lastName}`,
      firstName,
      lastName,
      restaurantName,
      email,
      role: 'owner' // Регистрируется по умолчанию владелец
    });
  }

  logout() {
    this.currentUser.set(null);
  }
}
