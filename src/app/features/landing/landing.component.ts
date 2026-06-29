import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { UiButtonComponent } from '../../core/ui/button/button.component';
import { UiCardComponent } from '../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../core/ui/badge/badge.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [UiButtonComponent, UiCardComponent, UiBadgeComponent, RouterLink],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingPageComponent {}
