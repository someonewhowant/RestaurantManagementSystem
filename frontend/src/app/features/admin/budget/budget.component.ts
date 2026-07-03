import { Component, inject, signal, OnInit, OnDestroy, computed } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
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
  imports: [CommonModule, ReactiveFormsModule, FormsModule, UiCardComponent, UiBadgeComponent, UiButtonComponent, UiModalComponent],
  templateUrl: './budget.component.html',
  styleUrl: './budget.component.scss'
})
export class AdminBudgetComponent {
  public budgetService = inject(BudgetService);
  public settingsService = inject(SettingsService);
  private fb = inject(FormBuilder);

  public showAddForm = signal(false);
  public selectedTransaction = signal<Transaction | null>(null);

  private intervalId: any;

  ngOnInit() {
    this.intervalId = setInterval(() => {
      this.budgetService.fetchSummary();
      // Auto-refresh the first page only if the user hasn't loaded more pages
      if (this.budgetService.currentPage() === 0) {
        this.budgetService.fetchTransactions(0);
      }
    }, 5000);
  }

  ngOnDestroy() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  openTransactionDetails(transaction: Transaction) {
    this.selectedTransaction.set(transaction);
  }

  onStartDateChange(date: string) {
    this.budgetService.setDateRange(date, this.budgetService.dateRange().end);
  }

  onEndDateChange(date: string) {
    this.budgetService.setDateRange(this.budgetService.dateRange().start, date);
  }

  clearFilters() {
    this.budgetService.setDateRange(null, null);
  }

  hasExpenses = computed(() => {
    return this.budgetService.totalExpense() > 0;
  });

  expenseCategories = computed(() => {
    const expensesMap = this.budgetService.expenseByCategory();
    const total = this.budgetService.totalExpense();
    if (total === 0) return [];
    
    return Object.entries(expensesMap).map(([name, amount]) => {
      return {
        name,
        amount,
        percent: Math.round((amount / total) * 100)
      };
    }).sort((a, b) => b.amount - a.amount);
  });

  getCategoryColor(category: string): string {
    const colors: Record<string, string> = {
      'Продажи': '#4CAF50',
      'Закупки': '#FF9800',
      'Зарплата': '#F44336',
      'Коммуналка': '#2196F3',
      'Прочее': '#9E9E9E'
    };
    return colors[category] || '#9E9E9E';
  }

  closeModal() {
    this.selectedTransaction.set(null);
  }

  refundTransaction(transactionId: string) {
    if (confirm('Вы уверены, что хотите оформить возврат? Эта операция вернет ингредиенты на склад.')) {
      this.budgetService.refundTransaction(transactionId);
      this.closeModal();
    }
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
