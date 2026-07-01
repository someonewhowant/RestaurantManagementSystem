import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { UiButtonComponent } from '../../../core/ui/button/button.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  email = '';
  password = '';
  
  private authService = inject(AuthService);
  private router = inject(Router);

  onSubmit(e: Event) {
    e.preventDefault();
    if (this.email && this.password) {
      this.authService.login(this.email);
      // Если это официант, направим его в POS, иначе в админку
      if (this.authService.currentUser()?.role === 'waiter') {
        this.router.navigate(['/app/pos']);
      } else {
        this.router.navigate(['/app']);
      }
    }
  }
}
