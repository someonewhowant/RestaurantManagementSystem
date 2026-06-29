import { Component, inject, signal, computed } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { InventoryService, InventoryItem } from '../../../core/services/inventory.service';
import { BudgetService } from '../../../core/services/budget.service';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';
import { UiButtonComponent } from '../../../core/ui/button/button.component';
import { UiModalComponent } from '../../../core/ui/modal/modal.component';

@Component({
  selector: 'app-admin-inventory',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, UiBadgeComponent, UiButtonComponent, UiModalComponent],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss'
})
export class AdminInventoryComponent {
  public inventoryService = inject(InventoryService);
  public budgetService = inject(BudgetService);
  private fb = inject(FormBuilder);

  public showAddForm = signal(false);
  public selectedRestockItem = signal<InventoryItem | null>(null);

  public searchQuery = signal('');
  public selectedCategory = signal('Все категории');

  public filteredItems = computed(() => {
    const query = this.searchQuery().toLowerCase();
    const cat = this.selectedCategory();
    let result = this.inventoryService.items();
    
    if (query) {
      result = result.filter(item => item.name.toLowerCase().includes(query));
    }
    
    if (cat !== 'Все категории') {
      result = result.filter(item => item.category === cat);
    }
    
    return result;
  });

  public addForm = this.fb.group({
    name: ['', Validators.required],
    category: ['Овощи', Validators.required],
    currentStock: [0, [Validators.required, Validators.min(0)]],
    minStock: [0, [Validators.required, Validators.min(0)]],
    unit: ['кг', Validators.required]
  });

  public restockForm = this.fb.nonNullable.group({
    amount: [1, [Validators.required, Validators.min(0.01)]],
    cost: [0, [Validators.required, Validators.min(0)]]
  });

  toggleAddForm() {
    this.showAddForm.update(v => !v);
  }

  onSubmitAdd() {
    if (this.addForm.valid) {
      this.inventoryService.addItem(this.addForm.value as any);
      this.addForm.reset({ category: 'Овощи', currentStock: 0, minStock: 0, unit: 'кг' });
      this.showAddForm.set(false);
    }
  }

  openRestockModal(item: InventoryItem) {
    this.selectedRestockItem.set(item);
    this.restockForm.reset({ amount: 1, cost: 0 });
  }

  closeRestockModal() {
    this.selectedRestockItem.set(null);
  }

  submitRestock() {
    const item = this.selectedRestockItem();
    if (item && this.restockForm.valid) {
      const { amount, cost } = this.restockForm.getRawValue();
      
      // 1. Пополняем склад
      this.inventoryService.restock(item.id, amount);

      // 2. Списываем средства из бюджета, если стоимость > 0
      if (cost > 0) {
        this.budgetService.addTransaction({
          date: new Date().toISOString(),
          amount: cost,
          type: 'Расход',
          category: 'Закупки',
          description: `Закупка: ${item.name} (${amount} ${item.unit})`
        });
      }

      this.closeRestockModal();
    }
  }
}
