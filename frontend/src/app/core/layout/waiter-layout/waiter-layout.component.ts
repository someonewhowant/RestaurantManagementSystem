import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { UiBadgeComponent } from '../../ui/badge/badge.component';

@Component({
  selector: 'app-waiter-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, UiBadgeComponent],
  templateUrl: './waiter-layout.component.html',
  styleUrl: './waiter-layout.component.scss'
})
export class WaiterLayoutComponent {
  // Имя официанта в идеале берется из сервиса авторизации
  waiterName = 'Анна Попова'; 
}
