import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, of, map, filter, take } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = '/api/auth';
  private readonly SESSION_MARKER = 'sbvia_session_active';

  // El token de acceso se almacena en memoria, no en localStorage (Regla de seguridad Entrega 1B)
  private accessToken = signal<string | null>(null);

  // Perfil del usuario autenticado
  public currentUser = signal<any>(null);

  /**
   * Indica si el intento de restauración de sesión al iniciar la app ya terminó
   * (tanto si tuvo éxito como si no). El guard espera a que esto sea `true`
   * antes de evaluar isAuthenticated(), evitando la condición de carrera con F5.
   */
  private sessionReadySubject = new BehaviorSubject<boolean>(false);
  public sessionReady$ = this.sessionReadySubject.asObservable();

  constructor(private http: HttpClient, private router: Router) { }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.API_URL}/login`, credentials, { withCredentials: true }).pipe(
      tap((response: any) => {
        this.accessToken.set(response.accessToken);
        this.currentUser.set(response.usuario);
        localStorage.setItem(this.SESSION_MARKER, 'true');
        // Tras un login manual también marcamos la sesión como lista
        this.sessionReadySubject.next(true);
      })
    );
  }

  registro(data: any): Observable<any> {
    return this.http.post(`${this.API_URL}/registro`, data, { withCredentials: true }).pipe(
      tap((response: any) => {
        this.accessToken.set(response.accessToken);
        this.currentUser.set(response.usuario);
        localStorage.setItem(this.SESSION_MARKER, 'true');
        this.sessionReadySubject.next(true);
      })
    );
  }

  /**
   * Restaura la sesión únicamente cuando el navegador registra un inicio previo.
   * La marca no contiene credenciales; el refresh token permanece en su cookie HttpOnly.
   */
  initializeSession(): Observable<boolean> {
    if (localStorage.getItem(this.SESSION_MARKER) !== 'true') {
      this.sessionReadySubject.next(true);
      return of(false);
    }

    return this.refreshSession();
  }

  /**
   * Intenta restaurar la sesión usando la cookie HttpOnly de refresh token.
   * Llamado por APP_INITIALIZER al arrancar la aplicación.
   * Al terminar (con éxito o error) emite en sessionReady$ para desbloquear el authGuard.
   */
  refreshSession(): Observable<boolean> {
    return this.http.post(`${this.API_URL}/refresh`, {}, { withCredentials: true }).pipe(
      tap((response: any) => {
        this.accessToken.set(response.accessToken);
        this.currentUser.set(response.usuario);
      }),
      map(() => true),
      catchError(() => {
        // No hay cookie válida: el usuario no estaba logueado. Es normal.
        this.accessToken.set(null);
        this.currentUser.set(null);
        localStorage.removeItem(this.SESSION_MARKER);
        return of(false);
      }),
      tap(() => {
        // Siempre marcamos la sesión como resuelta al terminar,
        // sin importar si el refresh tuvo éxito o no.
        this.sessionReadySubject.next(true);
      })
    );
  }

  logout(callApi = true): void {
    if (callApi && this.accessToken()) {
      // Hacemos el llamado a la API para enviar el token a la blacklist de Redis
      this.http.post(`${this.API_URL}/logout`, {}, {
        headers: { Authorization: `Bearer ${this.accessToken()}` },
        withCredentials: true
      }).subscribe({
        next: () => this.clearSession(),
        error: () => this.clearSession()
      });
    } else {
      this.clearSession();
    }
  }

  private clearSession(navigate = true): void {
    this.accessToken.set(null);
    this.currentUser.set(null);
    localStorage.removeItem(this.SESSION_MARKER);
    // Al cerrar sesión, reseteamos la señal de ready para el próximo ciclo
    this.sessionReadySubject.next(false);
    if (navigate) {
      this.router.navigate(['/login']);
    }
  }

  getAccessToken(): string | null {
    return this.accessToken();
  }

  isAuthenticated(): boolean {
    return this.accessToken() !== null;
  }

  /**
   * Espera a que la sesión esté resuelta y luego devuelve si el usuario
   * está autenticado. Usado por el authGuard para evitar race conditions.
   */
  waitForSessionAndCheck(): Observable<boolean> {
    return this.sessionReady$.pipe(
      filter(ready => ready === true),
      take(1),
      map(() => this.isAuthenticated())
    );
  }
}
