import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login.component';
import { RegisterComponent } from './auth/register.component';
import { ShellComponent } from './shared/components/shell/shell.component';
import { authGuard } from './auth/auth.guard';
import { roleGuard } from './auth/role.guard';

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
      { path: 'escenarios/nuevo', loadComponent: () => import('./features/escenarios/escenario-form.component').then(m => m.EscenarioFormComponent), canActivate: [roleGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'escenarios/editar/:id', loadComponent: () => import('./features/escenarios/escenario-form.component').then(m => m.EscenarioFormComponent), canActivate: [roleGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'usuarios', loadComponent: () => import('./features/usuarios/usuario-list.component').then(m => m.UsuarioListComponent), canActivate: [roleGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'reglas-transito', loadComponent: () => import('./features/reglas/regla-transito.component').then(m => m.ReglaTransitoComponent), canActivate: [roleGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'practicas', loadComponent: () => import('./features/practicas/practicas-list.component').then(m => m.PracticasListComponent) },
      { path: 'simulador', loadComponent: () => import('./features/simulador/simulador-conduccion.component').then(m => m.SimuladorConduccionComponent) },
      { path: 'simulacion/:idEscenario', loadComponent: () => import('./features/practicas/simulacion.component').then(m => m.SimulacionComponent) },
      { path: 'instructor', loadComponent: () => import('./features/supervision/supervision.component').then(m => m.SupervisionComponent), canActivate: [roleGuard], data: { roles: ['INSTRUCTOR', 'ADMINISTRADOR'] } },
      { path: 'auditoria', loadComponent: () => import('./features/supervision/supervision.component').then(m => m.SupervisionComponent), canActivate: [roleGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: '', redirectTo: '/dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '/login' }
];
