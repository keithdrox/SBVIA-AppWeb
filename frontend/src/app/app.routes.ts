import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login.component';
import { RegisterComponent } from './auth/register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { EscenarioListComponent } from './features/escenarios/escenario-list.component';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'escenarios', component: EscenarioListComponent, canActivate: [authGuard] },
  { path: 'practicas', loadComponent: () => import('./features/practicas/practicas-list.component').then(m => m.PracticasListComponent), canActivate: [authGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
