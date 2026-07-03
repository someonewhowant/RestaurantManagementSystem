import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { BudgetService, Transaction } from '../../../core/services/budget.service';
import { SettingsService } from '../../../core/services/settings.service';
import { UiCardComponent } from '../../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';
import { UiButtonComponent } from '../../../core/ui/button/button.component';
import { UiModalComponent } from '../../../core/ui/modal/modal.component';

@Component({
  selector: 'app-admin-budget',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, UiCardComponent, UiBadgeComponent, UiButtonComponent, UiModalComponent],
  templateUrl: './budget.component.html',
  styleUrl: './budget.component.scss'
})
export class AdminBudgetComponent {
  public budgetService = inject(BudgetService);
  public settingsService = inject(SettingsService);
  private fb = inject(FormBuilder);

  public showAddForm = signal(false);
  public selectedTransaction = signal<Transaction | null>(null);

  openTransactionDetails(transaction: Transaction) {
    this.selectedTransaction.set(transaction);
  }

  closeModal() {
    this.selectedTransaction.set(null);
  }

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
