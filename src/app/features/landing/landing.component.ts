import { Component, signal, computed, AfterViewInit, ElementRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UiButtonComponent } from '../../core/ui/button/button.component';
import { UiCardComponent } from '../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../core/ui/badge/badge.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, UiButtonComponent, UiBadgeComponent, RouterLink],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingPageComponent implements AfterViewInit, OnDestroy {
  public revenue = signal<number>(1500000);
  private observer: IntersectionObserver | null = null;
  
  constructor(private el: ElementRef) {}

  public savings = computed(() => {
    return Math.round(this.revenue() * 0.08); // roughly 8% savings on average
  });

  public onRevenueChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.revenue.set(Number(input.value));
  }

  ngAfterViewInit() {
    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
        }
      });
    }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' });

    const selectors = '.feature-item, .section-heading, .showcase-block-animated, .premium-showcase, .pricing-card, .pricing-card-pro';
    const animatedElements = this.el.nativeElement.querySelectorAll(selectors);
    animatedElements.forEach((el: Element) => this.observer?.observe(el));
  }

  ngOnDestroy() {
    if (this.observer) {
      this.observer.disconnect();
    }
  }
}
