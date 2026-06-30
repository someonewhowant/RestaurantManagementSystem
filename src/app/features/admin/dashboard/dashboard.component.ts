import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UiCardComponent } from '../../../core/ui/card/card.component';
import { BudgetService } from '../../../core/services/budget.service';
import { TablesService } from '../../../core/services/tables.service';
import { OrderService } from '../../../core/services/order.service';
import { StaffService } from '../../../core/services/staff.service';
import { InventoryService } from '../../../core/services/inventory.service';
import { MenuService } from '../../../core/services/menu.service';

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

  // KPI Metrics for Dashboard
  public averageCheck = computed(() => {
    const orders = this.budgetService.transactions().filter(t => t.type === 'Доход');
    if (orders.length === 0) return 0;
    return this.totalRevenue() / orders.length;
  });

  public guestCount = computed(() => {
    // Mocked based on revenue since we don't track guests in budget transactions right now
    return Math.round(this.totalRevenue() / 850);
  });

  public foodCost = computed(() => {
    if (this.totalRevenue() === 0) return 0;
    return (this.totalExpenses() / this.totalRevenue()) * 100;
  });

  // Mock Trends (vs previous week/day)
  public trends = {
    revenue: { value: 12.5, isPositive: true },
    averageCheck: { value: 4.2, isPositive: true },
    guestCount: { value: 8.1, isPositive: true },
    foodCost: { value: -2.3, isPositive: true }, // Negative food cost is positive for business
  };

  // Generate SVG Sparklines (mock data)
  public sparklineRevenue = 'M 0 20 Q 10 25, 20 15 T 40 10 T 60 18 T 80 5 T 100 0';
  public sparklineCheck = 'M 0 15 Q 15 20, 30 10 T 60 5 T 80 12 T 100 2';
  public sparklineGuests = 'M 0 25 Q 10 15, 25 18 T 50 10 T 75 15 T 100 5';
  public sparklineFoodCost = 'M 0 5 Q 15 15, 30 10 T 60 20 T 80 15 T 100 25'; // Trending down

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

  public topWaiters = computed(() => {
    const transactions = this.budgetService.transactions().filter(t => t.type === 'Доход' && t.description.includes('Официант:'));

    const revenuePerWaiter: Record<string, number> = {};
    for (const t of transactions) {
      const match = t.description.match(/Официант:\s*(.+)$/);
      if (match && match[1]) {
        const name = match[1].trim();
        revenuePerWaiter[name] = (revenuePerWaiter[name] || 0) + t.amount;
      }
    }

    const sortedWaiters = Object.entries(revenuePerWaiter)
      .map(([name, revenue]) => ({ name, revenue }))
      .sort((a, b) => b.revenue - a.revenue);

    return sortedWaiters.slice(0, 3);
  });

  private inventoryService = inject(InventoryService);
  public lowStockItems = this.inventoryService.lowStockItems;
  public expiringItems = this.inventoryService.expiringItems;

  public recentTransactions = computed(() => {
    return this.budgetService.transactions().slice(0, 5);
  });

  private menuService = inject(MenuService);

  public abcAnalysis = computed(() => {
    const transactions = this.budgetService.transactions();
    const oneWeekAgo = new Date();
    oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);

    const dishStats: Record<string, { count: number, revenue: number }> = {};
    const menu = this.menuService.menu();

    transactions.forEach(t => {
      if (t.type === 'Доход' && t.category === 'Оплата заказа' && t.items) {
        const txDate = new Date(t.date);
        if (txDate >= oneWeekAgo) {
          t.items.forEach(item => {
            if (!dishStats[item.dishId]) dishStats[item.dishId] = { count: 0, revenue: 0 };
            dishStats[item.dishId].count += item.quantity;
            const dish = menu.find(d => d.id === item.dishId);
            const price = dish ? dish.price : 0;
            dishStats[item.dishId].revenue += price * item.quantity;
          });
        }
      }
    });
    

    const sortedDishes = Object.entries(dishStats)
      .map(([dishId, stats]) => {
        const dish = menu.find(d => d.id === dishId);
        return {
          name: dish ? dish.name : 'Удаленное блюдо',
          count: stats.count,
          revenue: stats.revenue,
          category: dish ? dish.category : 'Разное',
          icon: dish ? dish.imageIcon : '🍽️'
        };
      })
      .sort((a, b) => b.revenue - a.revenue);

    // Group into A (Hits) and C (Outsiders)
    const hits = sortedDishes.slice(0, 3); // Top 3
    const outsiders = sortedDishes.slice(-2).reverse(); // Bottom 2, lowest first

    return { hits, outsiders };
  });

  public weeklyChartData = computed(() => {
    // Generate last 7 days names
    const days = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
    const today = new Date().getDay();
    const orderedDays = [];
    for (let i = 6; i >= 0; i--) {
      let d = today - i;
      if (d <= 0) d += 7;
      orderedDays.push(days[d - 1]);
    }
    
    return orderedDays.map((day, i) => {
      // Mock realistic looking data for the week
      const rev = 45000 + (Math.sin(i) * 12000) + (i * 6000);
      const exp = 18000 + (Math.cos(i) * 4000) + (i * 1500);
      return {
        label: day,
        revenue: Math.round(rev),
        expense: Math.round(exp)
      };
    });
  });

  public chartMax = computed(() => {
    const data = this.weeklyChartData();
    const maxRev = Math.max(...data.map(d => d.revenue));
    const maxExp = Math.max(...data.map(d => d.expense));
    return Math.max(maxRev, maxExp) * 1.1; // 10% headroom
  });

  public peakHoursData = computed(() => {
    const hours = [];
    for (let i = 10; i <= 23; i++) {
      let label = `${i}:00`;
      // Mock some typical restaurant distribution: peaks around 13:00-14:00 and 19:00-21:00
      let load = 20; // Base load
      if (i >= 12 && i <= 15) {
        load += (15 - Math.abs(13.5 - i)) * 15; // Lunch peak
      } else if (i >= 18 && i <= 22) {
        load += (22 - Math.abs(20 - i)) * 12; // Dinner peak
      } else {
        load += Math.random() * 10;
      }
      
      // Clamp between 5 and 100
      load = Math.max(5, Math.min(100, load));
      
      hours.push({ label, load: Math.round(load) });
    }
    return hours;
  });
}
