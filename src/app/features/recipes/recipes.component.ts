import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MenuService, Dish, MenuCategory } from '../../core/services/menu.service';
import { InventoryService } from '../../core/services/inventory.service';
import { UiCardComponent } from '../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../core/ui/badge/badge.component';
import { UiButtonComponent } from '../../core/ui/button/button.component';

@Component({
  selector: 'app-recipes',
  standalone: true,
  imports: [CommonModule, FormsModule, UiCardComponent, UiBadgeComponent, UiButtonComponent],
  templateUrl: './recipes.component.html',
  styleUrl: './recipes.component.scss'
})
export class RecipesComponent {
  public menuService = inject(MenuService);
  public inventoryService = inject(InventoryService);
  
  public isBuilderOpen = signal(false);

  // Filters
  public searchQuery = signal('');
  public selectedCategory = signal<MenuCategory | 'Все'>('Все');

  public filteredMenu = computed(() => {
    let list = this.menuService.menu();
    
    if (this.selectedCategory() !== 'Все') {
      list = list.filter(d => d.category === this.selectedCategory());
    }
    
    const query = this.searchQuery().toLowerCase().trim();
    if (query) {
      list = list.filter(d => d.name.toLowerCase().includes(query));
    }
    
    return list;
  });

  // Form state
  public newDishName = signal('');
  public newDishCategory = signal<MenuCategory>('Горячее');
  public newDishPrice = signal(0);
  public newDishWeight = signal('');
  public newDishIcon = signal('🍲');
  
  public selectedIngredients = signal<{ingredientId: string, amount: number}[]>([]);

  public foodCost = computed(() => {
    let cost = 0;
    const inv = this.inventoryService.items();
    for (const item of this.selectedIngredients()) {
      const invItem = inv.find(i => i.id === item.ingredientId);
      if (invItem && invItem.pricePerUnit) {
        cost += invItem.pricePerUnit * item.amount;
      }
    }
    return cost;
  });

  public margin = computed(() => {
    const cost = this.foodCost();
    const price = this.newDishPrice();
    if (price <= 0) return 0;
    return ((price - cost) / price) * 100;
  });

  openBuilder() {
    this.isBuilderOpen.set(true);
    // Reset form
    this.newDishName.set('');
    this.newDishCategory.set('Горячее');
    this.newDishPrice.set(0);
    this.newDishWeight.set('');
    this.newDishIcon.set('🍲');
    this.selectedIngredients.set([]);
  }

  closeBuilder() {
    this.isBuilderOpen.set(false);
  }

  addIngredient() {
    this.selectedIngredients.update(list => [...list, { ingredientId: '', amount: 0 }]);
  }

  removeIngredient(index: number) {
    this.selectedIngredients.update(list => list.filter((_, i) => i !== index));
  }

  updateIngredient(index: number, field: 'ingredientId' | 'amount', value: any) {
    this.selectedIngredients.update(list => {
      const newList = [...list];
      newList[index] = { ...newList[index], [field]: value };
      return newList;
    });
  }

  saveDish() {
    if (!this.newDishName()) return;
    
    const validIngredients = this.selectedIngredients().filter(i => i.ingredientId && i.amount > 0);
    
    this.menuService.addDish({
      name: this.newDishName(),
      category: this.newDishCategory(),
      price: this.newDishPrice(),
      weight: this.newDishWeight() || '0г',
      imageIcon: this.newDishIcon(),
      recipe: validIngredients.length > 0 ? validIngredients : undefined
    });
    
    this.closeBuilder();
  }

  getIngredientName(id: string): string {
    const inv = this.inventoryService.items().find(i => i.id === id);
    return inv ? inv.name : 'Неизвестно';
  }

  getIngredientUnit(id: string): string {
    const inv = this.inventoryService.items().find(i => i.id === id);
    return inv ? inv.unit : '';
  }
}
