import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UiCardComponent } from '../../../core/ui/card/card.component';
import { BudgetService } from '../../../core/services/budget.service';
import { TablesService } from '../../../core/services/tables.service';
import { OrderService } from '../../../core/services/order.service';
import { StaffService } from '../../../core/services/staff.service';
import { InventoryService } from '../../../core/services/inventory.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, UiCardComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class AdminDashboardComponent {
  private budgetService = inject(BudgetService);
  private tablesService = inject(TablesService);
  private orderService = inject(OrderService);

  public totalRevenue = computed(() => {
    return this.budgetService.transactions()
      .filter(t => t.type === 'Доход')
      .reduce((sum, t) => sum + t.amount, 0);
  });

  public totalExpenses = computed(() => {
    return this.budgetService.transactions()
      .filter(t => t.type === 'Расход')
      .reduce((sum, t) => sum + t.amount, 0);
  });

  public profit = computed(() => {
    return this.totalRevenue() - this.totalExpenses();
  });

  public occupiedTablesCount = computed(() => {
    return this.tablesService.tables().filter(t => t.status !== 'Свободен').length;
  });

  public totalTablesCount = computed(() => {
    return this.tablesService.tables().length;
  });

  public activeOrdersCount = computed(() => {
    return Object.values(this.orderService.orders()).filter(o => o.items && o.items.length > 0).length;
  });

  private staffService = inject(StaffService);

  public staffOnShift = computed(() => {
    return this.staffService.staff().filter(e => e.onShift);
  });

  public topWaiter = computed(() => {
    const transactions = this.budgetService.transactions().filter(t => t.type === 'Доход' && t.description.includes('Официант:'));

    const revenuePerWaiter: Record<string, number> = {};
    for (const t of transactions) {
      const match = t.description.match(/Официант:\s*(.+)$/);
      if (match && match[1]) {
        const name = match[1].trim();
        revenuePerWaiter[name] = (revenuePerWaiter[name] || 0) + t.amount;
      }
    }

    let topName = null;
    let maxRevenue = 0;
    for (const [name, rev] of Object.entries(revenuePerWaiter)) {
      if (rev > maxRevenue) {
        maxRevenue = rev;
        topName = name;
      }
    }

    return topName ? { name: topName, revenue: maxRevenue } : null;
  });

  private inventoryService = inject(InventoryService);
  public lowStockItems = this.inventoryService.lowStockItems;

  public recentTransactions = computed(() => {
    return this.budgetService.transactions().slice(0, 5);
  });

  public topDishes = [
    { name: 'Стейк Рибай', count: 42, trend: '+12%', category: 'Горячее' },
    { name: 'Капучино', count: 86, trend: '+5%', category: 'Напитки' },
    { name: 'Цезарь с курицей', count: 34, trend: '-2%', category: 'Закуски' },
    { name: 'Паста Карбонара', count: 28, trend: '+8%', category: 'Горячее' }
  ].sort((a, b) => b.count - a.count);
}
