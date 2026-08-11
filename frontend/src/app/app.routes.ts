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
  { path: 'escenarios/nuevo', loadComponent: () => import('./features/escenarios/escenario-form.component').then(m => m.EscenarioFormComponent), canActivate: [authGuard] },
  { path: 'escenarios/editar/:id', loadComponent: () => import('./features/escenarios/escenario-form.component').then(m => m.EscenarioFormComponent), canActivate: [authGuard] },
  { path: 'usuarios', loadComponent: () => import('./features/usuarios/usuario-list.component').then(m => m.UsuarioListComponent), canActivate: [authGuard] },
  { path: 'practicas', loadComponent: () => import('./features/practicas/practicas-list.component').then(m => m.PracticasListComponent), canActivate: [authGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
