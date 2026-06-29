import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { UiButtonComponent } from './core/ui/button/button.component';
import { UiCardComponent } from './core/ui/card/card.component';
import { UiBadgeComponent } from './core/ui/badge/badge.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, UiButtonComponent, UiCardComponent, UiBadgeComponent],
  templateUrl: './app.component.html'
})
export class AppComponent {}
