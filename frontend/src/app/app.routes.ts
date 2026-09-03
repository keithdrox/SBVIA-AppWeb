import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login.component';
import { RegisterComponent } from './auth/register.component';
import { ShellComponent } from './shared/components/shell/shell.component';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegisterComponent },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'escenarios', loadComponent: () => import('./features/escenarios/escenario-list.component').then(m => m.EscenarioListComponent) },
      { path: 'escenarios/nuevo', loadComponent: () => import('./features/escenarios/escenario-form.component').then(m => m.EscenarioFormComponent) },
      { path: 'escenarios/editar/:id', loadComponent: () => import('./features/escenarios/escenario-form.component').then(m => m.EscenarioFormComponent) },
      { path: 'usuarios', loadComponent: () => import('./features/usuarios/usuario-list.component').then(m => m.UsuarioListComponent) },
      { path: 'practicas', loadComponent: () => import('./features/practicas/practicas-list.component').then(m => m.PracticasListComponent) },
      { path: 'simulacion/:idEscenario', loadComponent: () => import('./features/practicas/simulacion.component').then(m => m.SimulacionComponent) },
      { path: '', redirectTo: '/dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '/login' }
];
