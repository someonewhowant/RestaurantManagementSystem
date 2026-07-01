import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: '../login/login.component.scss' // Используем те же стили, что и для логина
})
export class RegisterComponent {
  firstName = '';
  lastName = '';
  restaurantName = '';
  email = '';
  password = '';
  
  private authService = inject(AuthService);
  private router = inject(Router);

  onSubmit(e: Event) {
    e.preventDefault();
    if (this.firstName && this.lastName && this.restaurantName && this.email && this.password) {
      this.authService.register(this.email, this.firstName, this.lastName, this.restaurantName, this.password).subscribe({
        next: () => {
          this.router.navigate(['/app']);
        },
        error: (err) => {
          console.error('Registration failed', err);
          alert('Ошибка регистрации. Возможно, такой email уже используется.');
        }
      });
    }
  }
}
