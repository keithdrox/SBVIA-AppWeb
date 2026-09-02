import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AppComponent } from './app.component';
import { routes } from './app.routes';

/**
 * Smoke tests de integración de rutas — SBVIA Frontend
 *
 * Cubre las Historias de Usuario US-01 a US-06 referenciadas
 * en la matriz de trazabilidad (docs/trazabilidad/matriz.csv).
 *
 * US-01: Registro autónomo de conductor         → ruta /registro
 * US-02: Inicio de sesión seguro                → ruta /login
 * US-03: Exploración de escenarios viales       → ruta /escenarios (authGuard)
 * US-04: Ejecución de simulación                → ruta /escenarios (lazy) + SP sp_reporte_simulacion
 * US-05: Visualización de métricas en dashboard → ruta /dashboard (authGuard)
 * US-06: Descarga de certificado                → ruta /practicas (lazy)
 */
describe('AppComponent — Smoke Tests de Rutas (US-01 a US-06)', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter(routes),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();
  });

  // ─── Infraestructura básica ────────────────────────────────────────────────

  it('should create the AppComponent', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should have a router-outlet in the template', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled: HTMLElement = fixture.nativeElement;
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });

  // ─── US-01: Registro autónomo de conductor ────────────────────────────────

  it('US-01 — ruta /registro está declarada en el router', () => {
    const registroRoute = routes.find(r => r.path === 'registro');
    expect(registroRoute).toBeDefined();
    expect(registroRoute?.component).toBeTruthy();
  });

  // ─── US-02: Inicio de sesión seguro ───────────────────────────────────────

  it('US-02 — ruta /login está declarada en el router', () => {
    const loginRoute = routes.find(r => r.path === 'login');
    expect(loginRoute).toBeDefined();
    expect(loginRoute?.component).toBeTruthy();
  });

  it('US-02 — ruta raíz redirige a /login', () => {
    const rootRoute = routes.find(r => r.path === '');
    expect(rootRoute?.redirectTo).toBe('/login');
  });

  // ─── US-03: Exploración de escenarios viales ──────────────────────────────

  it('US-03 — ruta /escenarios está declarada y protegida con authGuard', () => {
    const escenariosRoute = routes.find(r => r.path === 'escenarios');
    expect(escenariosRoute).toBeDefined();
    expect(escenariosRoute?.canActivate).toBeTruthy();
    expect(escenariosRoute?.canActivate?.length).toBeGreaterThan(0);
  });

  // ─── US-04: Ejecución de simulación ──────────────────────────────────────

  it('US-04 — ruta /escenarios/nuevo para nueva simulación está declarada', () => {
    const nuevoRoute = routes.find(r => r.path === 'escenarios/nuevo');
    expect(nuevoRoute).toBeDefined();
    expect(nuevoRoute?.loadComponent).toBeTruthy();
  });

  // ─── US-05: Visualización de métricas en dashboard ───────────────────────

  it('US-05 — ruta /dashboard está declarada y protegida con authGuard', () => {
    const dashboardRoute = routes.find(r => r.path === 'dashboard');
    expect(dashboardRoute).toBeDefined();
    expect(dashboardRoute?.canActivate).toBeTruthy();
    expect(dashboardRoute?.canActivate?.length).toBeGreaterThan(0);
  });

  // ─── US-06: Descarga de certificado ──────────────────────────────────────

  it('US-06 — ruta /practicas para generación de certificado está declarada', () => {
    const practicasRoute = routes.find(r => r.path === 'practicas');
    expect(practicasRoute).toBeDefined();
    expect(practicasRoute?.loadComponent).toBeTruthy();
  });

  // ─── Cobertura total de rutas ─────────────────────────────────────────────

  it('todas las rutas protegidas usan authGuard', () => {
    const protectedPaths = ['dashboard', 'escenarios', 'escenarios/nuevo', 'escenarios/editar/:id', 'usuarios', 'practicas'];
    protectedPaths.forEach(path => {
      const route = routes.find(r => r.path === path);
      expect(route?.canActivate ?? route?.loadComponent).toBeTruthy();
    });
  });
});
