import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { BudgetService, Transaction } from '../../../core/services/budget.service';
import { UiCardComponent } from '../../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';
import { UiButtonComponent } from '../../../core/ui/button/button.component';
import { DatePipe, CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-admin-budget',
  standalone: true,
  imports: [UiCardComponent, UiBadgeComponent, UiButtonComponent, ReactiveFormsModule, DatePipe, CurrencyPipe],
  templateUrl: './budget.component.html',
  styleUrl: './budget.component.scss'
})
export class AdminBudgetComponent {
  public budgetService = inject(BudgetService);
  private fb = inject(FormBuilder);

  public showAddForm = signal(false);

  public txForm = this.fb.nonNullable.group({
    amount: [0, [Validators.required, Validators.min(0.01)]],
    type: ['Расход' as Transaction['type'], Validators.required],
    category: ['Закупки', Validators.required],
    description: ['', Validators.required],
    date: [new Date().toISOString().split('T')[0], Validators.required]
  });

  toggleAddForm() {
    this.showAddForm.update(v => !v);
  }

  onSubmitTx() {
    if (this.txForm.valid) {
      const val = this.txForm.getRawValue();
      this.budgetService.addTransaction({
        ...val,
        date: new Date(val.date).toISOString()
      });
      this.txForm.reset({ 
        type: 'Расход', 
        category: 'Закупки', 
        date: new Date().toISOString().split('T')[0],
        amount: 0,
        description: ''
      });
      this.showAddForm.set(false);
    }
  }
}
