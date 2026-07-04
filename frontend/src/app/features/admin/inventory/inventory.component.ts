import { Component, inject, signal, computed } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { InventoryService, InventoryItem } from '../../../core/services/inventory.service';
import { BudgetService } from '../../../core/services/budget.service';
import { MenuService, Dish } from '../../../core/services/menu.service';
import { SettingsService } from '../../../core/services/settings.service';
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
  public menuService = inject(MenuService);
  public settingsService = inject(SettingsService);
  private fb = inject(FormBuilder);

  public showAddForm = signal(false);
  public selectedRestockItem = signal<InventoryItem | null>(null);
  public selectedEditItem = signal<InventoryItem | null>(null);
  public selectedViewItem = signal<InventoryItem | null>(null);

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
    unit: ['кг', Validators.required],
    pricePerUnit: [0, [Validators.min(0)]],
    expiresInDays: [0, [Validators.min(0)]]
  });

  public restockForm = this.fb.nonNullable.group({
    amount: [1, [Validators.required, Validators.min(0.01)]],
    cost: [0, [Validators.required, Validators.min(0)]]
  });

  public editForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    category: ['Овощи', Validators.required],
    minStock: [0, [Validators.required, Validators.min(0)]],
    unit: ['кг', Validators.required],
    pricePerUnit: [0, [Validators.min(0)]],
    expiresInDays: [0, [Validators.min(0)]]
  });

  public consumeForm = this.fb.nonNullable.group({
    amount: [1, [Validators.required, Validators.min(0.01)]]
  });

  toggleAddForm() {
    this.showAddForm.update(v => !v);
  }

  onSubmitAdd() {
    if (this.addForm.valid) {
      this.inventoryService.addItem(this.addForm.value as any);
      this.addForm.reset({ category: 'Овощи', currentStock: 0, minStock: 0, unit: 'кг', pricePerUnit: 0, expiresInDays: 0 });
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

  getPercentage(item: InventoryItem): number {
    const max = Math.max(item.minStock * 4, 10);
    const p = (item.currentStock / max) * 100;
    return Math.min(Math.max(p, 0), 100);
  }

  getProgressBarClass(item: InventoryItem): string {
    if (item.currentStock <= item.minStock) return 'danger';
    if (item.currentStock <= item.minStock * 1.5) return 'warning';
    return 'success';
  }

  openEditModal(item: InventoryItem) {
    this.selectedEditItem.set(item);
    this.editForm.reset({
      name: item.name,
      category: item.category,
      minStock: item.minStock,
      unit: item.unit,
      pricePerUnit: item.pricePerUnit || 0,
      expiresInDays: item.expiresInDays || 0
    });
  }

  closeEditModal() {
    this.selectedEditItem.set(null);
  }

  submitEdit() {
    const item = this.selectedEditItem();
    if (item && this.editForm.valid) {
      this.inventoryService.updateItem(item.id, this.editForm.getRawValue() as any);
      this.closeEditModal();
    }
  }

  openViewModal(item: InventoryItem) {
    this.selectedViewItem.set(item);
    this.consumeForm.reset({ amount: 1 });
  }

  closeViewModal() {
    this.selectedViewItem.set(null);
  }

  submitConsume() {
    const item = this.selectedViewItem();
    if (item && this.consumeForm.valid) {
      const { amount } = this.consumeForm.getRawValue();
      this.inventoryService.consume(item.id, amount);
      this.closeViewModal();
    }
  }

  getRelatedDishes(item: InventoryItem): Dish[] {
    const dishes: Dish[] = [];
    for (const dish of this.menuService.menu()) {
      if (dish.recipe && dish.recipe.some((r: any) => r.ingredientId === item.id)) {
        dishes.push(dish);
      }
    }
    return dishes;
  }
}
