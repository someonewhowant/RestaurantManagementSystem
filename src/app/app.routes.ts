import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingPageComponent)
  },
  {
    path: 'auth/login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'auth/register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'admin',
    loadComponent: () => import('./core/layout/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./features/admin/dashboard/dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'inventory', loadComponent: () => import('./features/admin/inventory/inventory.component').then(m => m.AdminInventoryComponent) },
      { path: 'staff', loadComponent: () => import('./features/admin/staff/staff.component').then(m => m.AdminStaffComponent) },
      { path: 'budget', loadComponent: () => import('./features/admin/budget/budget.component').then(m => m.AdminBudgetComponent) }
    ]
  },
  {
    path: 'waiter',
    loadComponent: () => import('./core/layout/waiter-layout/waiter-layout.component').then(m => m.WaiterLayoutComponent),
    children: [
      { path: '', redirectTo: 'tables', pathMatch: 'full' },
      { path: 'tables', loadComponent: () => import('./features/waiter/tables/tables.component').then(m => m.WaiterTablesComponent) },
      { path: 'terminal', loadComponent: () => import('./features/waiter/terminal/terminal.component').then(m => m.WaiterTerminalComponent) },
      { path: 'kitchen', loadComponent: () => import('./features/waiter/kitchen/kitchen.component').then(m => m.KitchenComponent) }
    ]
  }
];
