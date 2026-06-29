import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InventoryService } from '../../../core/services/inventory.service';
import { UiCardComponent } from '../../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';
import { UiButtonComponent } from '../../../core/ui/button/button.component';

@Component({
  selector: 'app-admin-inventory',
  standalone: true,
  imports: [UiBadgeComponent, UiButtonComponent, ReactiveFormsModule],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss'
})
export class AdminInventoryComponent {
  public inventoryService = inject(InventoryService);
  private fb = inject(FormBuilder);

  public showAddForm = signal(false);

  public addForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    category: ['Овощи', Validators.required],
    currentStock: [0, [Validators.required, Validators.min(0)]],
    minStock: [5, [Validators.required, Validators.min(0)]],
    unit: ['кг', Validators.required]
  });

  onRestock(id: string) {
    this.inventoryService.restock(id, 10);
  }

  toggleAddForm() {
    this.showAddForm.update(v => !v);
  }

  onSubmitAdd() {
    if (this.addForm.valid) {
      this.inventoryService.addItem(this.addForm.getRawValue());
      this.addForm.reset({ category: 'Овощи', currentStock: 0, minStock: 5, unit: 'кг' });
      this.showAddForm.set(false);
    }
  }
}
