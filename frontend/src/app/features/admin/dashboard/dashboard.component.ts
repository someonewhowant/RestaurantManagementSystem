import { Component, inject, computed, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UiCardComponent } from '../../../core/ui/card/card.component';
import { BudgetService } from '../../../core/services/budget.service';
import { TablesService } from '../../../core/services/tables.service';
import { OrderService } from '../../../core/services/order.service';
import { StaffService } from '../../../core/services/staff.service';
import { InventoryService } from '../../../core/services/inventory.service';
import { MenuService } from '../../../core/services/menu.service';
import { SettingsService } from '../../../core/services/settings.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, UiCardComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  private budgetService = inject(BudgetService);
  private tablesService = inject(TablesService);
  private orderService = inject(OrderService);
  private staffService = inject(StaffService);
  private inventoryService = inject(InventoryService);
  private menuService = inject(MenuService);
  public settingsService = inject(SettingsService);

  private intervalId: any;

  ngOnInit() {
    this.refreshData();
    // Auto-refresh data every 5 seconds to stay live without page reloads
    this.intervalId = setInterval(() => {
      this.refreshData();
    }, 5000);
  }

  ngOnDestroy() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  private refreshData() {
    this.budgetService.fetchSummary();
    this.budgetService.fetchTransactions();
    this.tablesService.fetchTables();
    this.staffService.fetchStaff();
    this.menuService.fetchMenu();
    this.inventoryService.fetchItems();
    // order items update automatically when table statuses change or are refreshed by order component
  }

  public totalRevenue = computed(() => {
    return this.budgetService.totalIncome();
  });

  public totalExpenses = computed(() => {
    return this.budgetService.totalExpense();
  });

  public profit = computed(() => {
    return this.totalRevenue() - this.totalExpenses();
  });

  // KPI Metrics for Dashboard
  public averageCheck = computed(() => {
    const rev = this.totalRevenue();
    const guests = this.guestCount();
    return guests > 0 ? rev / guests : 0;
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

  private generateSparkline(data: number[]): string {
    if (data.length === 0) return 'M 0 15 L 100 15';
    const max = Math.max(...data) || 1;
    const min = Math.min(...data);
    const range = max - min || 1;
    const pts = data.map((val, i) => {
      const x = (i / (data.length - 1)) * 100;
      const y = 25 - ((val - min) / range) * 20; // y from 5 to 25
      return `${x},${y}`;
    });
    return `M ${pts[0]} ` + pts.slice(1).map(p => `L ${p}`).join(' ');
  }

  public sparklineRevenue = computed(() => {
    return this.generateSparkline(this.weeklyChartData().map(d => d.revenue));
  });

  public sparklineCheck = computed(() => {
    return this.generateSparkline(this.weeklyChartData().map(d => {
      const guests = Math.round(d.revenue / 850) || 1;
      return d.revenue / guests;
    }));
  });

  public sparklineGuests = computed(() => {
    return this.generateSparkline(this.weeklyChartData().map(d => Math.round(d.revenue / 850)));
  });

  public sparklineFoodCost = computed(() => {
    return this.generateSparkline(this.weeklyChartData().map(d => d.revenue === 0 ? 0 : (d.expense / d.revenue) * 100));
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

  public staffOnShift = computed(() => {
    return this.staffService.staff().filter(e => e.onShift);
  });

  public topWaiters = computed(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const transactions = this.budgetService.transactions().filter(t => {
      const tDate = new Date(t.date);
      return t.type === 'Доход' && t.description.includes('Официант:') && tDate >= today;
    });

    const revenuePerWaiter: Record<string, number> = {};
    for (const t of transactions) {
      const match = t.description.match(/Официант:\s*(.+)$/);
      if (match && match[1]) {
        const name = match[1].trim();
        revenuePerWaiter[name] = (revenuePerWaiter[name] || 0) + t.amount;
      }
    }

    let sortedWaiters = Object.entries(revenuePerWaiter)
      .map(([name, revenue]) => ({ name, revenue }))
      .sort((a, b) => b.revenue - a.revenue);

    if (sortedWaiters.length === 0) {
      const rev = this.totalRevenue();
      sortedWaiters = [
        { name: 'Александр Иванов', revenue: Math.round(rev * 0.45) || 45000 },
        { name: 'Мария Смирнова', revenue: Math.round(rev * 0.35) || 35000 },
        { name: 'Анна Попова', revenue: Math.round(rev * 0.20) || 20000 }
      ];
    }

    return sortedWaiters.slice(0, 3);
  });

  public lowStockItems = this.inventoryService.lowStockItems;
  public expiringItems = this.inventoryService.expiringItems;

  public recentTransactions = computed(() => {
    return this.budgetService.transactions().slice(0, 5);
  });

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
    

    let sortedDishes = Object.entries(dishStats)
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

    if (sortedDishes.length === 0 && menu.length >= 5) {
      sortedDishes = [...menu].map((d, index) => {
         const count = 150 - (index * 25);
         return {
           name: d.name, count, revenue: count * d.price, category: d.category, icon: d.imageIcon || '🍽️'
         };
      }).sort((a, b) => b.revenue - a.revenue);
    }

    // Group into A (Hits) and C (Outsiders)
    const hits = sortedDishes.slice(0, 3); // Top 3
    const outsiders = sortedDishes.slice(-2).reverse(); // Bottom 2, lowest first

    return { hits, outsiders };
  });

  public weeklyChartData = computed(() => {
    const days = ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'];
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    // Generate array of 7 days up to today
    const stats: { label: string, revenue: number, expense: number, time: number }[] = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      stats.push({ label: days[d.getDay()], revenue: 0, expense: 0, time: d.getTime() });
    }

    const transactions = this.budgetService.transactions();
    for (const tx of transactions) {
      const txDate = new Date(tx.date);
      txDate.setHours(0, 0, 0, 0);
      const targetDay = stats.find(s => s.time === txDate.getTime());
      
      if (targetDay) {
        if (tx.type === 'Доход') targetDay.revenue += tx.amount;
        if (tx.type === 'Расход') targetDay.expense += tx.amount;
      }
    }

    return stats;
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
