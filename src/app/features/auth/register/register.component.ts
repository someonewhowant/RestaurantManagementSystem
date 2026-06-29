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
  name = '';
  email = '';
  password = '';
  
  private authService = inject(AuthService);
  private router = inject(Router);

  onSubmit(e: Event) {
    e.preventDefault();
    if (this.name && this.email && this.password) {
      this.authService.register(this.email, this.name);
      this.router.navigate(['/admin']);
    }
  }
}
