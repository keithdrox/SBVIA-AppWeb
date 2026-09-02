import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = '/api/auth';
  
  // El token de acceso se almacena en memoria, no en localStorage (Regla de seguridad Entrega 1B)
  private accessToken = signal<string | null>(null);
  
  // Perfil del usuario autenticado
  public currentUser = signal<any>(null);

  constructor(private http: HttpClient, private router: Router) { }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.API_URL}/login`, credentials, { withCredentials: true }).pipe(
      tap((response: any) => {
        this.accessToken.set(response.accessToken);
        this.currentUser.set(response.usuario);
      })
    );
  }

  registro(data: any): Observable<any> {
    return this.http.post(`${this.API_URL}/registro`, data, { withCredentials: true }).pipe(
      tap((response: any) => {
        this.accessToken.set(response.accessToken);
        this.currentUser.set(response.usuario);
      })
    );
  }

  refreshSession(): Observable<boolean> {
    return this.http.post(`${this.API_URL}/refresh`, {}, { withCredentials: true }).pipe(
      tap((response: any) => {
        this.accessToken.set(response.accessToken);
        this.currentUser.set(response.usuario);
      }),
      // Si el refresh falla (por ejemplo, porque el usuario no tiene sesión guardada),
      // simplemente atrapamos el error y devolvemos false silenciosamente.
      catchError(() => {
        this.clearSession(false);
        return of(false);
      }),
      // Mapeamos a true en caso de éxito
      tap(() => true)
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
}
