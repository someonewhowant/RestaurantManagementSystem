import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingPageComponent)
  },
  {
    path: 'admin',
    loadComponent: () => import('./core/layout/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./features/admin/dashboard/dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'inventory', loadComponent: () => import('./features/admin/inventory/inventory.component').then(m => m.AdminInventoryComponent) },
      { path: 'staff', loadComponent: () => import('./features/admin/staff/staff.component').then(m => m.AdminStaffComponent) }
    ]
  }
];
