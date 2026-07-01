import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

export interface User {
  id: string;
  name: string;
  firstName?: string;
  lastName?: string;
  restaurantName?: string;
  email: string;
  role: 'OWNER' | 'WAITER' | 'MANAGER';
}

export interface AuthResponse {
  token: string;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  
  public currentUser = signal<User | null>(null);
  public isAuthenticated = computed(() => this.currentUser() !== null);
  
  constructor() {
    // Try to load user from local storage token on init
    const token = localStorage.getItem('token');
    if (token) {
      this.fetchCurrentUser();
    }
  }

  login(email: string, password: string = '123456') {
    return this.http.post<AuthResponse>('/api/auth/login', { email, password }).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        this.currentUser.set(res.user);
      })
    );
  }

  register(email: string, firstName: string, lastName: string, restaurantName: string, password: string = '123456') {
    return this.http.post<AuthResponse>('/api/auth/register', { 
      email, firstName, lastName, restaurantName, password 
    }).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        this.currentUser.set(res.user);
      })
    );
  }

  private fetchCurrentUser() {
    this.http.get<User>('/api/auth/me').subscribe({
      next: (user) => this.currentUser.set(user),
      error: () => this.logout() // Token might be expired
    });
  }

  logout() {
    localStorage.removeItem('token');
    this.currentUser.set(null);
  }
}
