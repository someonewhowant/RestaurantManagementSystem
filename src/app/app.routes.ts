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
    path: 'app',
    loadComponent: () => import('./core/layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./features/admin/dashboard/dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'inventory', loadComponent: () => import('./features/admin/inventory/inventory.component').then(m => m.AdminInventoryComponent) },
      { path: 'staff', loadComponent: () => import('./features/admin/staff/staff.component').then(m => m.AdminStaffComponent) },
      { path: 'budget', loadComponent: () => import('./features/admin/budget/budget.component').then(m => m.AdminBudgetComponent) },
      
      { path: 'pos', redirectTo: 'pos/tables', pathMatch: 'full' },
      { path: 'pos/tables', loadComponent: () => import('./features/waiter/tables/tables.component').then(m => m.WaiterTablesComponent) },
      { path: 'pos/terminal', loadComponent: () => import('./features/waiter/terminal/terminal.component').then(m => m.WaiterTerminalComponent) },
      
      { path: 'kitchen', loadComponent: () => import('./features/waiter/kitchen/kitchen.component').then(m => m.KitchenComponent) },
      { path: 'recipes', loadComponent: () => import('./features/recipes/recipes.component').then(m => m.RecipesComponent) }
    ]
  },
  // Redirect old routes just in case
  { path: 'admin', redirectTo: 'app', pathMatch: 'prefix' },
  { path: 'waiter', redirectTo: 'app/pos', pathMatch: 'prefix' }
];
