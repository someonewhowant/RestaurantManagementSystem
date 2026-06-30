import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MenuService } from '../../core/services/menu.service';
import { UiCardComponent } from '../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../core/ui/badge/badge.component';

@Component({
  selector: 'app-recipes',
  standalone: true,
  imports: [CommonModule, UiCardComponent, UiBadgeComponent],
  templateUrl: './recipes.component.html',
  styleUrl: './recipes.component.scss'
})
export class RecipesComponent {
  public menuService = inject(MenuService);
}
