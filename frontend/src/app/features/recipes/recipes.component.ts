import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MenuService, Dish, MenuCategory } from '../../core/services/menu.service';
import { InventoryService } from '../../core/services/inventory.service';
import { UiBadgeComponent } from '../../core/ui/badge/badge.component';
import { UiButtonComponent } from '../../core/ui/button/button.component';

@Component({
  selector: 'app-recipes',
  standalone: true,
  imports: [CommonModule, FormsModule, UiBadgeComponent, UiButtonComponent],
  templateUrl: './recipes.component.html',
  styleUrl: './recipes.component.scss'
})
export class RecipesComponent {
  public menuService = inject(MenuService);
  public inventoryService = inject(InventoryService);
  
  public isBuilderOpen = signal(false);
  
  // Emoji Modal State
  public isEmojiModalOpen = signal(false);
  public emojiList = ['🍲', '🥗', '🍔', '🍟', '🍕', '🌭', '🥪', '🌮', '🌯', '🍜', '🍣', '🍱', '🍛', '🍳', '🥩', '🍗', '🍖', '🥟', '🍤', '🥞', '🥐', '🍞', '🥯', '🍰', '🎂', '🧁', '🍦', '🍨', '🍧', '🍩', '🍪', '🍮', '🍷', '🍸', '🍹', '🍺', '☕', '🍵', '🥤', '🧃'];

  // Ingredient Modal State
  public isIngredientModalOpen = signal(false);
  public ingredientSearchQuery = signal('');
  
  public filteredInventory = computed(() => {
    const query = this.ingredientSearchQuery().toLowerCase();
    const items = this.inventoryService.items();
    if (!query) return items;
    return items.filter((item: any) => 
      item.name.toLowerCase().includes(query) || 
      item.category.toLowerCase().includes(query)
    );
  });

  getInvItem(id: string): any {
    return this.inventoryService.items().find(i => i.id === id);
  }

  openIngredientModal() {
    this.ingredientSearchQuery.set('');
    this.isIngredientModalOpen.set(true);
  }

  closeIngredientModal() {
    this.isIngredientModalOpen.set(false);
  }

  addIngredientFromModal(invItem: any) {
    if (!this.selectedIngredients().find(i => i.ingredientId === invItem.id)) {
      this.selectedIngredients.update(list => [...list, { ingredientId: invItem.id, amount: 1 }]);
    }
    this.closeIngredientModal();
  }

  // Emojis for dish icons
  public availableEmojis = ['🍲', '🥗', '🥩', '🍷', '🍺', '🍔', '🍟', '🍕', '🍣', '🍤', '🥐', '🥖', '🥪', '🌮', '🌯', '🍜', '🍝', '🍵', '☕', '🍰', '🧁', '🍨', '🍳', '🥞', '🥓', '🍗', '🥬', '🍄', '🥑', '🍆', '🍅', '🧅'];

  // Filters
  public searchQuery = signal('');
  public selectedCategory = signal<MenuCategory | 'Все'>('Все');
  
  public editingDishId = signal<string | null>(null);

  public filteredMenu = computed(() => {
    let list = this.menuService.menu();
    
    if (this.selectedCategory() !== 'Все') {
      list = list.filter(d => d.category === this.selectedCategory());
    }
    
    const query = this.searchQuery().toLowerCase().trim();
    if (query) {
      list = list.filter(d => {
        const nameMatch = d.name.toLowerCase().includes(query);
        const ingredientMatch = d.recipe?.some(ing => {
          const invName = this.getIngredientName(ing.ingredientId).toLowerCase();
          return invName.includes(query);
        });
        return nameMatch || ingredientMatch;
      });
    }
    
    return list;
  });

  public getDishMargin(dish: Dish): number {
    let cost = 0;
    const inv = this.inventoryService.items();
    if (dish.recipe) {
      for (const item of dish.recipe) {
        const invItem = inv.find(i => i.id === item.ingredientId);
        if (invItem && invItem.pricePerUnit) {
          cost += invItem.pricePerUnit * item.amount;
        }
      }
    }
    if (dish.price <= 0) return 0;
    return ((dish.price - cost) / dish.price) * 100;
  }

  // Form state
  public newDishName = signal('');
  public newDishCategory = signal<MenuCategory>('Горячее');
  public newDishPrice = signal(0);
  public newDishWeight = signal('');
  public newDishIcon = signal('🍲');
  
  public newDishSteps = signal<{text: string, timerMin: number}[]>([]);
  public newDishAllergens = signal<string[]>([]);
  public newDishMacros = signal({ proteins: 0, fats: 0, carbs: 0, calories: 0 });
  
  public selectedIngredients = signal<{ingredientId: string, amount: number}[]>([]);

  // Known allergens
  public knownAllergens = ['Глютен', 'Лактоза', 'Орехи', 'Морепродукты', 'Яйца', 'Соя', 'Мед'];

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

  openBuilder(dish?: Dish) {
    this.isBuilderOpen.set(true);
    if (dish) {
      this.editingDishId.set(dish.id);
      this.newDishName.set(dish.name);
      this.newDishCategory.set(dish.category);
      this.newDishPrice.set(dish.price);
      this.newDishWeight.set(dish.weight);
      this.newDishIcon.set(dish.imageIcon);
      try {
        const steps = JSON.parse(dish.instructions || '[]');
        this.newDishSteps.set(Array.isArray(steps) ? steps : [{text: dish.instructions || '', timerMin: 0}]);
      } catch {
        this.newDishSteps.set(dish.instructions ? [{text: dish.instructions, timerMin: 0}] : []);
      }
      this.newDishAllergens.set(dish.allergens || []);
      this.newDishMacros.set(dish.macros || { proteins: 0, fats: 0, carbs: 0, calories: 0 });
      this.selectedIngredients.set(dish.recipe ? [...dish.recipe] : []);
    } else {
      this.editingDishId.set(null);
      this.newDishName.set('');
      this.newDishCategory.set('Горячее');
      this.newDishPrice.set(0);
      this.newDishWeight.set('');
      this.newDishIcon.set('🍲');
      this.newDishSteps.set([]);
      this.newDishAllergens.set([]);
      this.newDishMacros.set({ proteins: 0, fats: 0, carbs: 0, calories: 0 });
      this.selectedIngredients.set([]);
    }
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

  addStep() {
    this.newDishSteps.update(steps => [...steps, { text: '', timerMin: 0 }]);
  }

  removeStep(index: number) {
    this.newDishSteps.update(steps => steps.filter((_, i) => i !== index));
  }

  updateStep(index: number, field: 'text' | 'timerMin', value: any) {
    this.newDishSteps.update(steps => {
      const newSteps = [...steps];
      newSteps[index] = { ...newSteps[index], [field]: value };
      return newSteps;
    });
  }

  updateMacros(field: 'proteins' | 'fats' | 'carbs' | 'calories', value: any) {
    this.newDishMacros.update(m => ({ ...m, [field]: Number(value) }));
  }

  toggleAllergen(allergen: string) {
    const current = this.newDishAllergens();
    if (current.includes(allergen)) {
      this.newDishAllergens.set(current.filter(a => a !== allergen));
    } else {
      this.newDishAllergens.set([...current, allergen]);
    }
  }

  saveDish() {
    if (!this.newDishName()) return;
    
    const validIngredients = this.selectedIngredients().filter(i => i.ingredientId && i.amount > 0);
    
    const dishData = {
      name: this.newDishName(),
      category: this.newDishCategory(),
      price: this.newDishPrice(),
      weight: this.newDishWeight() || '0г',
      imageIcon: this.newDishIcon(),
      recipe: validIngredients,
      instructions: JSON.stringify(this.newDishSteps()),
      allergens: this.newDishAllergens(),
      macros: this.newDishMacros()
    };

    if (this.editingDishId()) {
      this.menuService.updateDish(this.editingDishId()!, dishData);
    } else {
      this.menuService.addDish(dishData);
    }
    
    this.closeBuilder();
  }

  deleteDish() {
    if (this.editingDishId() && confirm('Вы уверены, что хотите удалить эту ТТК?')) {
      this.menuService.deleteDish(this.editingDishId()!);
      this.closeBuilder();
    }
  }

  getIngredientName(id: string): string {
    const inv = this.inventoryService.items().find(i => i.id === id);
    return inv ? inv.name : 'Неизвестно';
  }

  getIngredientUnit(id: string): string {
    const inv = this.inventoryService.items().find(i => i.id === id);
    return inv ? inv.unit : '';
  }

  openEmojiModal() {
    this.isEmojiModalOpen.set(true);
  }

  closeEmojiModal() {
    this.isEmojiModalOpen.set(false);
  }

  selectEmoji(emoji: string) {
    this.newDishIcon.set(emoji);
    this.closeEmojiModal();
  }
}
